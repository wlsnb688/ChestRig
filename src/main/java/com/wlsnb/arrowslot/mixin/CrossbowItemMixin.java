package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.equipment.BrokenGear;
import com.wlsnb.arrowslot.equipment.CrossbowEnchantments;
import com.wlsnb.arrowslot.equipment.CrossbowMagazine;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Multi-load crossbow behavior. An empty crossbow starts a server-timed reload session on
 * use; the session loads one bolt per interval after the charge, while a loaded crossbow
 * follows vanilla shooting. The fake client animation is driven by a payload.
 */
@SuppressWarnings("deprecation")
@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {
    private static final ThreadLocal<Boolean> FIRING = ThreadLocal.withInitial(() -> false);

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void arrowslot$instantShot(Level level, Player player, InteractionHand hand,
                                       CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (!CrossbowEnchantments.isCrossbow(stack)) {
            return;
        }
        if (BrokenGear.isBroken(stack)) {
            cir.setReturnValue(InteractionResultHolder.fail(stack));
            return;
        }
        CrossbowEnchantments.normalize(player, stack);
        if (CrossbowEnchantments.isInstantLoad(player, stack)) {
            if (level instanceof net.minecraft.server.level.ServerLevel && player instanceof ServerPlayer serverPlayer
                    && CrossbowMagazine.shootInstant(serverPlayer, stack, hand)) {
                cir.setReturnValue(InteractionResultHolder.consume(stack));
            } else if (level.isClientSide) {
                cir.setReturnValue(InteractionResultHolder.consume(stack));
            } else {
                cir.setReturnValue(InteractionResultHolder.fail(stack));
            }
            return;
        }

        if (CrossbowItem.isCharged(stack)) {
            return;
        }

        if (level instanceof net.minecraft.server.level.ServerLevel && player instanceof ServerPlayer serverPlayer) {
            CrossbowMagazine.manualReload(serverPlayer);
        }
        cir.setReturnValue(InteractionResultHolder.consume(stack));
    }

    @Inject(method = "performShooting", at = @At("HEAD"), cancellable = true)
    private void arrowslot$singleLoadedArrow(Level level, LivingEntity shooter, InteractionHand hand,
                                              ItemStack stack, float power, float uncertainty,
                                              LivingEntity target, CallbackInfo ci) {
        if (FIRING.get() || !(level instanceof ServerLevel serverLevel)
                || !(shooter instanceof Player player)) {
            return;
        }
        if (BrokenGear.isBroken(stack)) {
            ci.cancel();
            return;
        }

        ChargedProjectiles charged = stack.get(DataComponents.CHARGED_PROJECTILES);
        if (charged == null || charged.isEmpty()) {
            return;
        }
        List<ItemStack> loaded = charged.getItems();
        ItemStack first = loaded.get(0);
        List<ItemStack> remaining = new ArrayList<>(loaded.subList(1, loaded.size()));
        int projectileCount = Math.max(1, EnchantmentHelper.processProjectileCount(serverLevel, stack, shooter, 1));
        List<ItemStack> volley = new ArrayList<>(projectileCount);
        for (int i = 0; i < projectileCount; i++) {
            volley.add(first.copy());
        }

        FIRING.set(true);
        try {
            stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(volley));
            ((CrossbowItem) (Object) this).performShooting(level, shooter, hand, stack, power, uncertainty, target);
        } finally {
            FIRING.set(false);
            if (remaining.isEmpty()) {
                stack.remove(DataComponents.CHARGED_PROJECTILES);
            } else {
                stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(remaining));
            }
            player.inventoryMenu.broadcastChanges();
        }
        ci.cancel();
    }

    @Inject(method = "performShooting", at = @At("RETURN"))
    private void arrowslot$syncAfterVolley(Level level, LivingEntity shooter, InteractionHand hand,
                                           ItemStack stack, float power, float uncertainty,
                                           LivingEntity target, CallbackInfo ci) {
        if (shooter instanceof ServerPlayer player) {
            player.inventoryMenu.broadcastChanges();
        }
    }
}

package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.equipment.CrossbowEnchantments;
import com.wlsnb.arrowslot.equipment.CrossbowMagazine;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Multi-load crossbow behavior. The first bolt is loaded at the normal charge time while
 * the use key is held; further bolts load once per charge interval, up to the enchantment's
 * capacity. Vanilla's charged-projectile shooting still handles the volley.
 */
@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void arrowslot$instantShot(Level level, Player player, InteractionHand hand,
                                       CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (!CrossbowEnchantments.isCrossbow(stack)) {
            return;
        }
        CrossbowEnchantments.normalize(player, stack);
        if (!CrossbowEnchantments.isInstantLoad(player, stack)) {
            return;
        }

        if (level instanceof net.minecraft.server.level.ServerLevel && player instanceof ServerPlayer serverPlayer
                && CrossbowMagazine.shootInstant(serverPlayer, stack, hand)) {
            cir.setReturnValue(InteractionResultHolder.consume(stack));
        } else if (level.isClientSide) {
            cir.setReturnValue(InteractionResultHolder.consume(stack));
        } else {
            cir.setReturnValue(InteractionResultHolder.fail(stack));
        }
    }

    @Inject(method = "getUseDuration", at = @At("RETURN"), cancellable = true)
    private void arrowslot$extendLoadDuration(ItemStack stack, LivingEntity entity,
                                              CallbackInfoReturnable<Integer> cir) {
        if (!(entity instanceof Player player) || !CrossbowEnchantments.isCrossbow(stack)
                || CrossbowItem.isCharged(stack) || CrossbowEnchantments.isInstantLoad(player, stack)) {
            return;
        }
        int max = CrossbowEnchantments.maxLoaded(player, stack);
        int charge = CrossbowItem.getChargeDuration(stack, entity);
        cir.setReturnValue(Math.max(cir.getReturnValue(), charge * Math.max(1, max) + 5));
    }

    @Inject(method = "onUseTick", at = @At("HEAD"))
    private void arrowslot$continueLoading(Level level, LivingEntity entity, ItemStack stack,
                                           int remaining, CallbackInfo ci) {
        if (level.isClientSide || !(entity instanceof ServerPlayer player)
                || !CrossbowEnchantments.isCrossbow(stack)) {
            return;
        }
        if (CrossbowEnchantments.isInstantLoad(player, stack)) {
            player.stopUsingItem();
            return;
        }

        int duration = stack.getUseDuration(entity);
        int elapsed = duration - remaining;
        int charge = CrossbowItem.getChargeDuration(stack, entity);
        if (charge <= 0 || elapsed < charge || elapsed % charge != 0) {
            return;
        }
        if (CrossbowMagazine.isFull(player, stack)) {
            player.stopUsingItem();
            return;
        }
        if (!CrossbowMagazine.loadOne(player, stack, false)) {
            player.stopUsingItem();
        } else if (CrossbowMagazine.isFull(player, stack)) {
            player.stopUsingItem();
        }
    }

    @Inject(method = "releaseUsing", at = @At("HEAD"), cancellable = true)
    private void arrowslot$cancelVanillaLoad(ItemStack stack, Level level, LivingEntity entity,
                                             int remaining, CallbackInfo ci) {
        if (entity instanceof Player && CrossbowEnchantments.isCrossbow(stack)) {
            ci.cancel();
        }
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

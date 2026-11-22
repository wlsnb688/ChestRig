package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.ammo.AmmoSlots;
import com.wlsnb.arrowslot.registry.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProjectileWeaponItem.class)
public abstract class ProjectileWeaponItemMixin {
    /**
     * Chest-rig ammo is read out of a data component, so the normal draw path only gets a
     * copy. Consume the real chest-rig stack here and return a clean projectile stack.
     */
    @Inject(method = "useAmmo", at = @At("HEAD"), cancellable = true)
    private static void arrowslot$consumeChestRigAmmo(ItemStack weapon, ItemStack ammo, LivingEntity shooter, boolean intangible, CallbackInfoReturnable<ItemStack> cir) {
        Integer candidate = ammo.get(ModDataComponents.RIG_AMMO.get());
        if (candidate == null || !(shooter instanceof Player player)) {
            return;
        }

        boolean noConsume = intangible
                || !(shooter.level() instanceof ServerLevel)
                || shooter.hasInfiniteMaterials()
                || (ammo.getItem() instanceof ArrowItem arrow && arrow.isInfinite(ammo, weapon, shooter));
        if (noConsume) {
            cir.setReturnValue(intangibleCopy(ammo));
            return;
        }

        int count = EnchantmentHelper.processAmmoUse((ServerLevel) shooter.level(), weapon, ammo, 1);
        if (count > ammo.getCount()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        if (count <= 0) {
            cir.setReturnValue(intangibleCopy(ammo));
            return;
        }

        ItemStack consumed = AmmoSlots.consumeRigAmmo(player, candidate, count);
        if (consumed.isEmpty()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        consumed.remove(ModDataComponents.RIG_AMMO.get());
        cir.setReturnValue(consumed);
    }

    private static ItemStack intangibleCopy(ItemStack ammo) {
        ItemStack result = ammo.copyWithCount(1);
        result.remove(ModDataComponents.RIG_AMMO.get());
        result.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
        return result;
    }
}

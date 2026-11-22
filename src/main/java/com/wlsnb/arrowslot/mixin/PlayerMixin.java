package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.ammo.AmmoSelection;
import com.wlsnb.arrowslot.ammo.AmmoSlots;
import com.wlsnb.arrowslot.equipment.RigWeaponState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "getProjectile", at = @At("HEAD"), cancellable = true)
    private void arrowslot$useSelectedAmmo(ItemStack shootable, CallbackInfoReturnable<ItemStack> cir) {
        if (AmmoSlots.isWeapon(shootable)) {
            cir.setReturnValue(AmmoSelection.getProjectile((Player) (Object) this, shootable));
        }
    }

    /**
     * Exposes the selected chest-rig weapon as the main-hand stack. The real hotbar slot
     * is never modified, so the hotbar keeps showing its original item.
     */
    @Inject(method = "getItemBySlot", at = @At("HEAD"), cancellable = true)
    private void arrowslot$getRigWeapon(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        Player player = (Player) (Object) this;
        if (slot == EquipmentSlot.MAINHAND && RigWeaponState.isActive(player)) {
            cir.setReturnValue(RigWeaponState.held(player));
        }
    }

    /**
     * Routes main-hand writes (item consumption, durability changes, setItemInHand) to
     * the virtual stack instead of the real hotbar slot.
     */
    @Inject(method = "setItemSlot", at = @At("HEAD"), cancellable = true)
    private void arrowslot$setRigWeapon(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (slot == EquipmentSlot.MAINHAND && RigWeaponState.isActive(player)) {
            RigWeaponState.setHeld(player, stack);
            ci.cancel();
        }
    }
}

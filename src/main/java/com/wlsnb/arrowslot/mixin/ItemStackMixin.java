package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.equipment.BrokenGear;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    /**
     * Vanilla shrinks the stack as soon as damage reaches the maximum. Tracked gear is
     * clamped to maximum damage and kept instead, so it can be repaired later.
     */
    @Redirect(
            method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V")
    )
    private void arrowslot$keepZeroDurabilityGear(ItemStack stack, int amount) {
        if (BrokenGear.isTrackedGear(stack)) {
            stack.setDamageValue(stack.getMaxDamage());
        } else {
            stack.shrink(amount);
        }
    }
}

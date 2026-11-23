package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.equipment.AutoEatVisualState;
import com.wlsnb.arrowslot.equipment.BrokenGear;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    /**
     * A zero-durability shield no longer blocks. The next attack that would have been
     * blocked destroys it immediately.
     */
    @Inject(method = "isDamageSourceBlocked", at = @At("RETURN"), cancellable = true)
    private void arrowslot$breakBrokenShieldOnBlockAttempt(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!cir.getReturnValueZ() || !BrokenGear.isBrokenShield(entity.getUseItem())) {
            return;
        }

        if (!entity.level().isClientSide) {
            BrokenGear.destroyBrokenShield(entity);
        }
        cir.setReturnValue(false);
    }

    @Inject(method = "isUsingItem", at = @At("HEAD"), cancellable = true)
    private void arrowslot$autoEatIsUsingItem(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level().isClientSide && entity instanceof Player player
                && AutoEatVisualState.isActive(player)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getUseItem", at = @At("HEAD"), cancellable = true)
    private void arrowslot$autoEatUseItem(CallbackInfoReturnable<ItemStack> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level().isClientSide && entity instanceof Player player
                && AutoEatVisualState.isActive(player)) {
            cir.setReturnValue(AutoEatVisualState.food(player));
        }
    }

    @Inject(method = "getUseItemRemainingTicks", at = @At("HEAD"), cancellable = true)
    private void arrowslot$autoEatRemainingTicks(CallbackInfoReturnable<Integer> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level().isClientSide && entity instanceof Player player
                && AutoEatVisualState.isActive(player)) {
            cir.setReturnValue(AutoEatVisualState.remaining(player));
        }
    }

    @Inject(method = "getUsedItemHand", at = @At("HEAD"), cancellable = true)
    private void arrowslot$autoEatUseHand(CallbackInfoReturnable<InteractionHand> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level().isClientSide && entity instanceof Player player
                && AutoEatVisualState.isActive(player)) {
            cir.setReturnValue(InteractionHand.MAIN_HAND);
        }
    }
}

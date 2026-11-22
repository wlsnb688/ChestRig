package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.equipment.BrokenGear;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
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
}

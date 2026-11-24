package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.equipment.AutoEatVisualState;
import com.wlsnb.arrowslot.equipment.CrossbowLoadVisualState;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * LocalPlayer overrides isUsingItem with its own startedUsingItem flag, so the shared
 * LivingEntity hook is not enough for the client-side auto-eat animation.
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "isUsingItem", at = @At("HEAD"), cancellable = true)
    private void arrowslot$autoEatIsUsingItem(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (CrossbowLoadVisualState.isActive(player) || AutoEatVisualState.isActive(player)) {
            cir.setReturnValue(true);
        }
    }
}

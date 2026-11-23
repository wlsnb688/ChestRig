package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.equipment.RigTridentHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Returns a tagged thrown trident directly to its original chest-rig weapon slot.
 */
@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin {
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void arrowslot$pickupRigTrident(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer
                && RigTridentHandler.tryPickup(serverPlayer, (ThrownTrident)(Object)this)) {
            ci.cancel();
        }
    }
}

package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.equipment.RigTridentHandler;
import com.wlsnb.arrowslot.equipment.RigWeaponState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces vanilla trident release while the trident comes from a chest-rig weapon slot.
 */
@Mixin(TridentItem.class)
public abstract class TridentItemMixin {
    @Inject(method = "releaseUsing", at = @At("HEAD"), cancellable = true)
    private void arrowslot$releaseRigTrident(ItemStack stack, Level level, LivingEntity entityLiving,
                                             int timeLeft, CallbackInfo ci) {
        if (!(entityLiving instanceof Player player)
                || player.getUsedItemHand() != InteractionHand.MAIN_HAND
                || !RigWeaponState.isActive(player)) {
            return;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (RigTridentHandler.throwFromRig(serverPlayer, stack, level, timeLeft)) {
                ci.cancel();
            }
        } else if (RigTridentHandler.isNormalThrow(player, stack, timeLeft)) {
            // The server handles the throw and syncs the cleared rig slot.
            ci.cancel();
        }
    }
}

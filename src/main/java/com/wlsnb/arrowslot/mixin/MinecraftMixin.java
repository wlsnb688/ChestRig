package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.client.RigWeaponClientState;
import com.wlsnb.arrowslot.network.RigWeaponActionPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void arrowslot$rigWeaponOffhandSwap(CallbackInfo ci) {
        if (!RigWeaponClientState.isGMode()) {
            return;
        }

        Minecraft minecraft = (Minecraft) (Object) this;
        while (minecraft.options.keySwapOffhand.consumeClick()) {
            PacketDistributor.sendToServer(new RigWeaponActionPayload(
                    RigWeaponActionPayload.CYCLE,
                    1
            ));
        }
    }
}

package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.client.hud.HudConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Removes the vanilla top-right effect icons while Chest Rig draws its own readout there.
 */
@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void arrowslot$replaceVanillaEffects(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        HudConfig config = HudConfig.get();
        if (config.enabled && config.potionEnabled) {
            ci.cancel();
        }
    }
}

package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.compat.ChestRigRowRenderer;
import com.wlsnb.arrowslot.inventory.ChestRig;
import com.wlsnb.arrowslot.inventory.ChestRigSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Creative mode counterpart of the chest rig row.
 *
 * <p>The creative inventory mirrors the player inventory menu into wrapper slots when its
 * inventory tab is open. Those wrappers include the chest rig row, so the row is aligned and
 * drawn at the same docked position as in survival. The creative background itself is not
 * resized, which is what keeps the rest of the creative UI in its correct position.</p>
 */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin {
    @Inject(method = "renderBg", at = @At("TAIL"))
    private void arrowslot$renderChestRigRow(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;
        Minecraft minecraft = screen.getMinecraft();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        if (!screen.isInventoryOpen()) {
            return;
        }

        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int panelLeft = screen.leftPos + (screen.getXSize() - ChestRigRowRenderer.WIDTH) / 2;
        int top = ChestRigRowRenderer.dockedTop(screenHeight);
        int slotY = ChestRigRowRenderer.slotTop(screenHeight) - screen.topPos;
        boolean hasChestplate = !ChestRig.getChestplate(player).isEmpty();

        // The creative menu appends the wrappers for the player inventory in menu order and a
        // destroy slot last. The final nine wrappers before the destroy slot are the rig row.
        int firstRigWrapper = screen.getMenu().slots.size() - 1 - ChestRig.DISPLAY_SLOTS;
        for (int i = 0; i < ChestRig.DISPLAY_SLOTS; i++) {
            int index = firstRigWrapper + i;
            if (index < 0 || index >= screen.getMenu().slots.size()) {
                break;
            }
            Slot slot = screen.getMenu().slots.get(index);
            slot.x = panelLeft - screen.leftPos + ChestRigSlot.ROW_X + i * ChestRigSlot.SLOT_STEP;
            slot.y = slotY;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
        ChestRigRowRenderer.draw(guiGraphics, panelLeft, top, hasChestplate);
        guiGraphics.pose().popPose();
    }
}

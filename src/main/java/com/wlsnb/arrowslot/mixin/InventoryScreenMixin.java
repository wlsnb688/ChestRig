package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.compat.ChestRigRowRenderer;
import com.wlsnb.arrowslot.inventory.ChestRig;
import com.wlsnb.arrowslot.inventory.ChestRigSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the chest rig row as an independent vanilla-styled panel docked at the bottom of the
 * screen and aligns its slots with the panel. The inventory background and the vanilla
 * hotbar layout are left untouched.
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    @Inject(method = "renderBg", at = @At("TAIL"))
    private void arrowslot$renderChestRigRow(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        InventoryScreen screen = (InventoryScreen) (Object) this;
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int panelLeft = screen.leftPos + (screen.getXSize() - ChestRigRowRenderer.WIDTH) / 2;
        int top = ChestRigRowRenderer.dockedTop(screenHeight);
        int slotY = ChestRigRowRenderer.slotTop(screenHeight) - screen.topPos;
        boolean hasChestplate = !ChestRig.getChestplate(player).isEmpty();
        InventoryMenu menu = player.inventoryMenu;
        int first = Math.max(0, menu.slots.size() - ChestRig.DISPLAY_SLOTS);

        for (int i = 0; i < ChestRig.DISPLAY_SLOTS && first + i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(first + i);
            if (slot instanceof ChestRigSlot) {
                slot.x = panelLeft - screen.leftPos + ChestRigSlot.ROW_X + i * ChestRigSlot.SLOT_STEP;
                slot.y = slotY;
            }
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
        ChestRigRowRenderer.draw(guiGraphics, panelLeft, top, hasChestplate);
        guiGraphics.pose().popPose();
    }
}

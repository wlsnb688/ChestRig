package com.wlsnb.arrowslot.client;

import com.wlsnb.arrowslot.ammo.AmmoSlots;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class AmmoHud {
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_GAP = 2;

    private AmmoHud() {
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!ClientAmmoState.isHudVisible()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui) {
            return;
        }

        ItemStack weapon = AmmoSlots.getHeldWeapon(player);
        if (weapon.isEmpty()) {
            return;
        }

        List<Integer> slots = ClientAmmoState.visibleSlots(player);
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int rowWidth = slots.isEmpty() ? 120 : slots.size() * (SLOT_SIZE + SLOT_GAP) - SLOT_GAP;
        float scale = Math.min(1.0F, (screenWidth - 24.0F) / rowWidth);
        int selected = ClientAmmoState.selected(player, weapon);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(screenWidth / 2.0F, screenHeight - 72.0F, 200.0F);
        guiGraphics.pose().scale(scale, scale, 1.0F);

        int rowLeft = -rowWidth / 2;
        int panelTop = -5;
        int panelBottom = SLOT_SIZE + 14;
        guiGraphics.fill(rowLeft - 6, panelTop, rowLeft + rowWidth + 6, panelBottom, 0xD0101010);
        guiGraphics.fill(rowLeft - 5, panelTop + 1, rowLeft + rowWidth + 5, panelBottom - 1, 0xB0202020);

        if (slots.isEmpty()) {
            String message = Component.translatable("hud.arrowslot.empty").getString();
            int textX = -(minecraft.font.width(message) / 2);
            guiGraphics.drawString(minecraft.font, message, textX, 4, 0xFFB0B0B0, true);
        } else {
            for (int position = 0; position < slots.size(); position++) {
                int slotIndex = slots.get(position);
                int x = rowLeft + position * (SLOT_SIZE + SLOT_GAP);
                boolean isSelected = slotIndex == selected;
                int border = isSelected ? 0xFFFF3030 : 0xFF3A3A3A;
                guiGraphics.fill(x - 1, -1, x + SLOT_SIZE + 1, SLOT_SIZE + 1, border);
                guiGraphics.fill(x, 0, x + SLOT_SIZE, SLOT_SIZE, isSelected ? 0xFF501010 : 0xFF202020);
                guiGraphics.fill(x + 1, 1, x + SLOT_SIZE - 1, SLOT_SIZE - 1, 0xFF8B8B8B);

                ItemStack stack = AmmoSlots.get(player, slotIndex);
                if (!stack.isEmpty()) {
                    guiGraphics.renderItem(stack, x + 1, 1);
                    guiGraphics.renderItemDecorations(minecraft.font, stack, x + 1, 1);
                }

                String label = label(slotIndex);
                if (!label.isEmpty()) {
                    float labelScale = Math.min(1.0F, (SLOT_SIZE - 2.0F) / minecraft.font.width(label));
                    float labelX = x + (SLOT_SIZE - minecraft.font.width(label) * labelScale) / 2.0F;
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(labelX, SLOT_SIZE + 3.0F, 0.0F);
                    guiGraphics.pose().scale(labelScale, labelScale, 1.0F);
                    guiGraphics.drawString(minecraft.font, label, 0, 0, 0xFFFFFFFF, true);
                    guiGraphics.pose().popPose();
                }
            }
        }

        guiGraphics.pose().popPose();
    }

    private static String label(int index) {
        if (AmmoSlots.isRigArrowSlot(index)) {
            return Component.translatable(
                    "hud.arrowslot.rig",
                    index - AmmoSlots.RIG_ARROW_START + 1
            ).getString();
        }
        if (index == AmmoSlots.OFFHAND) {
            return Component.translatable("hud.arrowslot.offhand").getString();
        }
        if (index >= AmmoSlots.HOTBAR_START && index < AmmoSlots.HOTBAR_START + AmmoSlots.HOTBAR_COUNT) {
            return Integer.toString(index - AmmoSlots.HOTBAR_START + 1);
        }
        return "";
    }
}

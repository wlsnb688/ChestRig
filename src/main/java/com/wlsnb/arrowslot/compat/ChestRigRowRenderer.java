package com.wlsnb.arrowslot.compat;

import com.wlsnb.arrowslot.ArrowSlotMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws the chest rig row as an independent vanilla-styled panel docked at the bottom of the
 * screen.
 *
 * <p>The row slots live in the player inventory menu and are positioned in screen-relative
 * coordinates, so the panel is identical in survival and creative mode. Its slot backgrounds
 * are copied from the vanilla inventory texture, so resource packs keep working, and the
 * vanilla inventory background and hotbar layout are never changed.</p>
 */
public final class ChestRigRowRenderer {
    public static final int WIDTH = 176;
    public static final int HEIGHT = 34;
    /** Slot background top inside the panel. */
    public static final int SLOT_TOP = 15;
    /** Slot item top inside the panel. */
    public static final int SLOT_ITEM_TOP = SLOT_TOP + 1;
    public static final int BOTTOM_MARGIN = 2;

    private static final ResourceLocation INVENTORY_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");

    private ChestRigRowRenderer() {
    }

    public static int dockedTop(int screenHeight) {
        return screenHeight - BOTTOM_MARGIN - HEIGHT;
    }

    /** Slot top in screen coordinates. */
    public static int slotTop(int screenHeight) {
        return dockedTop(screenHeight) + SLOT_ITEM_TOP;
    }

    /** Draws the panel, its border, the title and the nine slot backgrounds. */
    public static void draw(GuiGraphics guiGraphics, int left, int top, boolean hasChestplate) {
        guiGraphics.fill(left, top, left + WIDTH, top + HEIGHT, 0xFFC6C6C6);
        guiGraphics.fill(left, top, left + WIDTH, top + 1, 0xFFFFFFFF);
        guiGraphics.fill(left, top + HEIGHT - 1, left + WIDTH, top + HEIGHT, 0xFF555555);
        guiGraphics.fill(left, top, left + 1, top + HEIGHT, 0xFFFFFFFF);
        guiGraphics.fill(left + WIDTH - 1, top, left + WIDTH, top + HEIGHT, 0xFF555555);

        for (int i = 0; i < 9; i++) {
            guiGraphics.blit(INVENTORY_TEXTURE, left + 7 + i * 18, top + SLOT_TOP, 7, 141, 18, 18);
        }

        Font font = Minecraft.getInstance().font;
        Component label = hasChestplate
                ? Component.translatable("container." + ArrowSlotMod.MOD_ID + ".chest_rig")
                : Component.translatable("container." + ArrowSlotMod.MOD_ID + ".no_chestplate");
        int x = hasChestplate ? left + 8 : left + (WIDTH - font.width(label)) / 2;
        guiGraphics.drawString(font, label, x, top + 4, 0x404040, false);
    }
}

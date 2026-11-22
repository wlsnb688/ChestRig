package com.wlsnb.arrowslot.compat;

import com.wlsnb.arrowslot.inventory.RigContentsTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Renders the chest rig contents inside the item tooltip, the same way a bundle shows its
 * contents when hovered in the inventory.
 */
public final class ChestRigContentsTooltip implements ClientTooltipComponent {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.withDefaultNamespace("container/bundle/background");
    private static final ResourceLocation SLOT =
            ResourceLocation.withDefaultNamespace("container/bundle/slot");
    private static final int SLOT_X = 18;
    private static final int SLOT_Y = 20;
    private static final int MAX_COLUMNS = 9;

    private final NonNullList<ItemStack> items;
    private final int visible;
    private final int columns;
    private final int rows;

    public ChestRigContentsTooltip(RigContentsTooltip contents) {
        this.items = contents.items();
        this.visible = Math.max(1, lastNonEmpty(this.items) + 1);
        this.columns = Math.min(this.visible, MAX_COLUMNS);
        this.rows = Math.max(1, (int) Math.ceil((double) this.visible / this.columns));
    }

    private static int lastNonEmpty(NonNullList<ItemStack> items) {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (!items.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getHeight() {
        return this.rows * SLOT_Y + 2 + 4;
    }

    @Override
    public int getWidth(Font font) {
        return this.columns * SLOT_X + 2;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        guiGraphics.blitSprite(BACKGROUND, x, y, this.columns * SLOT_X + 2, this.rows * SLOT_Y + 2);

        for (int index = 0; index < this.visible; index++) {
            int column = index % this.columns;
            int row = index / this.columns;
            int slotX = x + column * SLOT_X + 1;
            int slotY = y + row * SLOT_Y + 1;
            guiGraphics.blitSprite(SLOT, slotX, slotY, 0, SLOT_X, SLOT_Y);

            if (index >= this.items.size() || this.items.get(index).isEmpty()) {
                continue;
            }

            ItemStack stack = this.items.get(index);
            guiGraphics.renderItem(stack, slotX + 1, slotY + 1, index);
            guiGraphics.renderItemDecorations(font, stack, slotX + 1, slotY + 1);
            if (index == 0) {
                AbstractContainerScreen.renderSlotHighlight(guiGraphics, slotX + 1, slotY + 1, 0);
            }
        }
    }
}

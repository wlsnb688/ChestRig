package com.wlsnb.arrowslot.client.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Drag editor for the inventory, armor and hands/counter panels.
 */
public final class HudPositionScreen extends Screen {
    private static final int SLOT = 18;
    private final HudConfig config = HudConfig.get();
    private int dragging = -1;
    private int dragOffsetX;
    private int dragOffsetY;

    public HudPositionScreen() {
        super(Component.translatable("hud.arrowslot.position_title"));
    }

    @Override
    protected void init() {
        // Keep both buttons at the top, clear of the bottom HUD boxes.
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                button -> this.onClose()
        ).bounds(this.width / 2 - 155, 50, 150, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("hud.arrowslot.option.reset_positions"),
                button -> {
                    config.resetPositions();
                    config.save();
                }
        ).bounds(this.width / 2 + 5, 50, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int[] inventory = inventoryBox();
        int[] left = HudOverlay.leftInfoBox(config);
        int[] right = HudOverlay.rightInfoBox(config);
        drawBox(guiGraphics, "hud.arrowslot.inventory", inventory[0], inventory[1], inventory[2], inventory[3]);
        drawBox(guiGraphics, "hud.arrowslot.armor_info", left[0], left[1], left[2], left[3]);
        drawBox(guiGraphics, "hud.arrowslot.hands_counters", right[0], right[1], right[2], right[3]);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 18, 0xFFFFFFFF);
        guiGraphics.drawCenteredString(
                this.font,
                Component.translatable("hud.arrowslot.position_hint"),
                this.width / 2,
                32,
                0xFFA0A0A0
        );
    }

    private void drawBox(GuiGraphics guiGraphics, String key, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0x80303030);
        guiGraphics.fill(x, y, x + width, y + 1, 0xFFFFFFFF);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFFFFFFFF);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFF808080);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF808080);
        guiGraphics.drawString(this.font, Component.translatable(key), x + 3, y + 3, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Let the Done and Reset buttons win when a HUD box overlaps them.
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0) {
            int[] inventory = inventoryBox();
            int[] left = HudOverlay.leftInfoBox(config);
            int[] right = HudOverlay.rightInfoBox(config);
            if (inside(mouseX, mouseY, inventory[0], inventory[1], inventory[2], inventory[3])) {
                dragging = 0;
                dragOffsetX = (int) mouseX - inventory[0];
                dragOffsetY = (int) mouseY - inventory[1];
            } else if (inside(mouseX, mouseY, left[0], left[1], left[2], left[3])) {
                dragging = 1;
                dragOffsetX = (int) mouseX - left[0];
                dragOffsetY = (int) mouseY - left[1];
            } else if (inside(mouseX, mouseY, right[0], right[1], right[2], right[3])) {
                dragging = 2;
                dragOffsetX = (int) mouseX - right[0];
                dragOffsetY = (int) mouseY - right[1];
            }
            if (dragging >= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging >= 0) {
            int x = clamp((int) mouseX - dragOffsetX, 0, Math.max(0, this.width - 20));
            int y = clamp((int) mouseY - dragOffsetY, 0, Math.max(0, this.height - 20));
            if (dragging == 0) {
                config.inventoryAnchored = false;
                config.inventoryX = x;
                config.inventoryY = y;
            } else if (dragging == 1) {
                config.leftInfoAnchored = false;
                config.leftInfoX = x;
                config.leftInfoY = y;
            } else {
                config.rightInfoAnchored = false;
                config.rightInfoX = x;
                config.rightInfoY = y;
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging >= 0) {
            dragging = -1;
            config.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        config.save();
        super.onClose();
    }

    private int[] inventoryBox() {
        float scale = config.inventoryMini ? 0.75F : 1.0F;
        int width = Math.round(((config.inventoryVertical ? 3 : 9) * SLOT - 2) * scale);
        int height = Math.round(((config.inventoryVertical ? 9 : 3) * SLOT - 2) * scale);
        int x = config.inventoryAnchored
                ? Math.round((this.width - width) / 2.0F) + config.inventoryX
                : config.inventoryX;
        int y = config.inventoryAnchored
                ? this.height - 24 - height + config.inventoryY
                : config.inventoryY;
        if (config.inventoryAnchored) {
            y = Math.max(0, Math.min(y, this.height - 24 - height));
        }
        x = clamp(x, 0, Math.max(0, this.width - width));
        y = clamp(y, 0, Math.max(0, this.height - height));
        return new int[]{x, y, width, height};
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

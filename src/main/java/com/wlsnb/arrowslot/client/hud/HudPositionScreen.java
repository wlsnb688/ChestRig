package com.wlsnb.arrowslot.client.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.List;

/**
 * Drag editor for the inventory and potion sections. The equipment readout follows the
 * vanilla hotbar and is therefore not freely positioned.
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
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                button -> this.onClose()
        ).bounds(this.width / 2 - 155, this.height - 30, 150, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("hud.arrowslot.option.reset_positions"),
                button -> {
                    config.resetPositions();
                    config.save();
                }
        ).bounds(this.width / 2 + 5, this.height - 30, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int[] inventory = inventoryBox();
        int[] potion = potionBox();
        drawBox(guiGraphics, "hud.arrowslot.inventory", inventory[0], inventory[1], inventory[2], inventory[3]);
        drawBox(guiGraphics, "hud.arrowslot.effects", potion[0], potion[1], potion[2], potion[3]);

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
        if (button == 0) {
            int[] inventory = inventoryBox();
            int[] potion = potionBox();
            if (inside(mouseX, mouseY, inventory[0], inventory[1], inventory[2], inventory[3])) {
                dragging = 0;
                dragOffsetX = (int) mouseX - inventory[0];
                dragOffsetY = (int) mouseY - inventory[1];
            } else if (inside(mouseX, mouseY, potion[0], potion[1], potion[2], potion[3])) {
                dragging = 1;
                dragOffsetX = (int) mouseX - potion[0];
                dragOffsetY = (int) mouseY - potion[1];
            }
            if (dragging >= 0) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
            } else {
                config.potionX = x;
                config.potionY = y;
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
        int y = config.inventoryAnchored ? this.height - config.inventoryY : config.inventoryY;
        x = clamp(x, 0, Math.max(0, this.width - width));
        y = clamp(y, 0, Math.max(0, this.height - height));
        return new int[]{x, y, width, height};
    }

    private int[] potionBox() {
        int count = Math.max(1, effects().size());
        int gap = config.potionGap;
        int width;
        int height;
        if (config.potionHorizontal) {
            int rowWidth = config.potionMini ? 18 : 36;
            width = count * rowWidth + (count - 1) * gap;
            height = config.potionMini ? 22 : 38;
        } else {
            int rowHeight = config.potionMini ? 18 : 24;
            width = config.potionMini ? 22 : 60;
            height = count * rowHeight + (count - 1) * gap;
        }
        int x = clamp(config.potionX, 0, Math.max(0, this.width - width));
        int y = clamp(config.potionY, 0, Math.max(0, this.height - height));
        return new int[]{x, y, width, height};
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static List<MobEffectInstance> effects() {
        var player = net.minecraft.client.Minecraft.getInstance().player;
        return player == null ? List.of() : List.copyOf(player.getActiveEffects());
    }
}

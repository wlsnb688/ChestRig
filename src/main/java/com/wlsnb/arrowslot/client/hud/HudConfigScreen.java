package com.wlsnb.arrowslot.client.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Small built-in settings screen. It intentionally mirrors the toggles of the former
 * Inventory HUD+ integration instead of depending on that mod's configuration screen.
 */
public final class HudConfigScreen extends Screen {
    private static final int WIDTH = 150;
    private static final int HEIGHT = 18;
    private final HudConfig config = HudConfig.get();

    public HudConfigScreen() {
        super(Component.translatable("hud.arrowslot.config_title"));
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 155;
        int right = this.width / 2 + 5;
        int y = 30;

        addToggle(left, y, "hud.arrowslot.option.enabled", () -> config.enabled, v -> config.enabled = v);
        addToggle(right, y, "hud.arrowslot.option.inventory", () -> config.inventoryEnabled, v -> config.inventoryEnabled = v);
        y += 24;
        addToggle(left, y, "hud.arrowslot.option.inventory_mini", () -> config.inventoryMini, v -> config.inventoryMini = v);
        addToggle(right, y, "hud.arrowslot.option.inventory_vertical", () -> config.inventoryVertical, v -> config.inventoryVertical = v);
        y += 24;
        addToggle(left, y, "hud.arrowslot.option.potion", () -> config.potionEnabled, v -> config.potionEnabled = v);
        addToggle(right, y, "hud.arrowslot.option.potion_mini", () -> config.potionMini, v -> config.potionMini = v);
        y += 24;
        addToggle(left, y, "hud.arrowslot.option.potion_horizontal", () -> config.potionHorizontal, v -> config.potionHorizontal = v);
        addToggle(right, y, "hud.arrowslot.option.show_empty", () -> config.showEmpty, v -> config.showEmpty = v);
        y += 24;
        addToggle(left, y, "hud.arrowslot.option.equipment", () -> config.equipmentEnabled, v -> config.equipmentEnabled = v);
        addToggle(right, y, "hud.arrowslot.option.show_armor", () -> config.showArmor, v -> config.showArmor = v);
        y += 24;
        addToggle(left, y, "hud.arrowslot.option.show_held", () -> config.showHeld, v -> config.showHeld = v);
        addToggle(right, y, "hud.arrowslot.option.show_durability", () -> config.showDurability, v -> config.showDurability = v);
        y += 24;
        addToggle(left, y, "hud.arrowslot.option.show_free_slots", () -> config.showFreeSlots, v -> config.showFreeSlots = v);
        addToggle(right, y, "hud.arrowslot.option.show_arrows", () -> config.showArrows, v -> config.showArrows = v);
        y += 22;

        this.addRenderableWidget(Button.builder(
                Component.translatable("hud.arrowslot.option.edit_positions"),
                button -> this.minecraft.setScreen(new HudPositionScreen())
        ).bounds(this.width / 2 - 75, y, WIDTH, HEIGHT).build());
        y += 24;
        this.addRenderableWidget(Button.builder(
                Component.translatable("hud.arrowslot.option.reset_positions"),
                button -> {
                    config.resetPositions();
                    config.save();
                }
        ).bounds(this.width / 2 - 75, y, WIDTH, HEIGHT).build());
    }

    private void addToggle(int x, int y, String key, BooleanSupplier getter, Consumer<Boolean> setter) {
        Button button = Button.builder(label(key, getter.getAsBoolean()), pressed -> {
            boolean value = !getter.getAsBoolean();
            setter.accept(value);
            pressed.setMessage(label(key, value));
            config.save();
        }).bounds(x, y, WIDTH, HEIGHT).build();
        this.addRenderableWidget(button);
    }

    private static Component label(String key, boolean value) {
        return Component.translatable(key)
                .append(": ")
                .append(Component.translatable(value ? "options.on" : "options.off"));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 18, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        config.save();
        super.onClose();
    }
}
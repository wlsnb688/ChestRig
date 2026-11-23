package com.wlsnb.arrowslot.client.hud;

import com.wlsnb.arrowslot.ammo.AmmoSlots;
import com.wlsnb.arrowslot.inventory.ChestRig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Clean-room implementation of the HUD layout used by Inventory HUD+. The geometry is
 * intentionally based on the vanilla hotbar and vanilla item rendering rather than on
 * Inventory HUD+'s renderer code.
 */
public final class HudOverlay {
    private static final int SLOT = 18;
    private static final int INV_COLS = 9;
    private static final int INV_ROWS = 3;

    private static final int WHITE = 0xFFFFFFFF;

    private static final ResourceLocation EMPTY_HELMET = tex("empty/helmet");
    private static final ResourceLocation EMPTY_CHESTPLATE = tex("empty/chestplate");
    private static final ResourceLocation EMPTY_LEGGINGS = tex("empty/leggings");
    private static final ResourceLocation EMPTY_BOOTS = tex("empty/boots");
    private static final ResourceLocation EMPTY_SHIELD = tex("empty/shield");
    private static final ResourceLocation EMPTY_MAINHAND = tex("empty/mainhand");
    private static final ResourceLocation ARROWS = tex("empty/arrows");
    private static final ResourceLocation INVENTORY = tex("empty/inventory");

    private HudOverlay() {
    }

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath("arrowslot", "textures/gui/" + path + ".png");
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        HudConfig config = HudConfig.get();
        if (player == null || minecraft.options.hideGui || !config.enabled) {
            return;
        }

        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        if (config.inventoryEnabled) {
            renderInventory(guiGraphics, player, config, minecraft.font, width, height);
        }
        if (config.potionEnabled) {
            renderPotions(guiGraphics, player, config, minecraft, width, height);
        }
        if (config.equipmentEnabled) {
            renderEquipment(guiGraphics, player, config, minecraft.font, width, height);
        }
    }

    // -------------------------------------------------------------------------
    // Inventory: 27 main slots, bottom-centre by default, transparent by default.
    // -------------------------------------------------------------------------

    private static void renderInventory(GuiGraphics guiGraphics, Player player, HudConfig config,
                                        Font font, int screenWidth, int screenHeight) {
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>) {
            return;
        }
        List<ItemStack> items = player.getInventory().items;
        boolean anyItem = false;
        for (int i = 0; i < 27; i++) {
            if (!items.get(9 + i).isEmpty()) {
                anyItem = true;
                break;
            }
        }
        if (!anyItem && config.inventoryAlpha <= 0) {
            return;
        }

        float scale = config.inventoryMini ? 0.75F : 1.0F;
        int columns = config.inventoryVertical ? 3 : INV_COLS;
        int rows = config.inventoryVertical ? 9 : INV_ROWS;
        int contentWidth = columns * SLOT - 2;
        int contentHeight = rows * SLOT - 2;

        int x;
        int y;
        if (config.inventoryAnchored) {
            x = Math.round((screenWidth - contentWidth * scale) / 2.0F) + config.inventoryX;
            y = Math.round(screenHeight - config.inventoryY);
        } else {
            x = config.inventoryX;
            y = config.inventoryY;
        }
        x = clamp(x, 0, Math.max(0, screenWidth - Math.round(contentWidth * scale)));
        y = clamp(y, 0, Math.max(0, screenHeight - Math.round(contentHeight * scale)));

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0.0F);
        guiGraphics.pose().scale(scale, scale, 1.0F);

        if (config.inventoryAlpha > 0) {
            drawInventoryBackground(guiGraphics, config, contentWidth, contentHeight, columns, rows);
        }

        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
                int index = config.inventoryVertical
                        ? (3 - column) * 9 + row
                        : column + (row + 1) * 9;
                ItemStack stack = items.get(index);
                if (!stack.isEmpty()) {
                    int itemX = column * SLOT;
                    int itemY = row * SLOT;
                    guiGraphics.renderItem(stack, itemX, itemY);
                    guiGraphics.renderItemDecorations(font, stack, itemX, itemY);
                }
            }
        }
        guiGraphics.pose().popPose();
    }

    private static void drawInventoryBackground(GuiGraphics guiGraphics, HudConfig config,
                                                int width, int height, int columns, int rows) {
        int alpha = alphaByte(config.inventoryAlpha);
        if (alpha <= 0) {
            return;
        }
        guiGraphics.fill(-8, -8, width + 8, height + 8, argb(alpha, 0x161616));
        guiGraphics.fill(-8, -8, width + 8, -7, argb(alpha, 0x3A3A3A));
        guiGraphics.fill(-8, -8, -7, height + 8, argb(alpha, 0x3A3A3A));
        guiGraphics.fill(-8, height + 7, width + 8, height + 8, argb(alpha, 0x0E0E0E));
        guiGraphics.fill(width + 7, -8, width + 8, height + 8, argb(alpha, 0x0E0E0E));
        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
                int x = column * SLOT;
                int y = row * SLOT;
                guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, argb(alpha, 0x202020));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Potions: 60x24 per effect by default, with icon, level and duration.
    // -------------------------------------------------------------------------

    private static void renderPotions(GuiGraphics guiGraphics, Player player, HudConfig config,
                                      Minecraft minecraft, int screenWidth, int screenHeight) {
        List<MobEffectInstance> effects = new ArrayList<>();
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (effect.showIcon() || config.potionShowHidden) {
                effects.add(effect);
            }
        }
        if (effects.isEmpty()) {
            return;
        }

        int gap = config.potionGap;
        if (config.potionHorizontal) {
            int rowWidth = config.potionMini ? 18 : 36;
            int rowHeight = config.potionMini ? 22 : 38;
            int x = clamp(config.potionX, 0, Math.max(0, screenWidth - rowWidth));
            int y = clamp(config.potionY, 0, Math.max(0, screenHeight - rowHeight));
            for (int i = 0; i < effects.size(); i++) {
                drawPotion(guiGraphics, minecraft, config, effects.get(i), x + i * (rowWidth + gap), y, true);
            }
        } else {
            int rowWidth = config.potionMini ? 22 : 60;
            int rowHeight = config.potionMini ? 18 : 24;
            int totalHeight = effects.size() * rowHeight + (effects.size() - 1) * gap;
            int x = clamp(config.potionX, 0, Math.max(0, screenWidth - rowWidth));
            int y = clamp(config.potionY, 0, Math.max(0, screenHeight - totalHeight));
            for (int i = 0; i < effects.size(); i++) {
                drawPotion(guiGraphics, minecraft, config, effects.get(i), x, y + i * (rowHeight + gap), false);
            }
        }
    }

    private static void drawPotion(GuiGraphics guiGraphics, Minecraft minecraft, HudConfig config,
                                   MobEffectInstance effect, int x, int y, boolean horizontal) {
        boolean mini = config.potionMini;
        int width = horizontal ? (mini ? 18 : 36) : (mini ? 22 : 60);
        int height = horizontal ? (mini ? 22 : 38) : (mini ? 18 : 24);

        int alpha = alphaByte(config.potionAlpha);
        if (alpha > 0) {
            guiGraphics.fill(x, y, x + width, y + height, argb(alpha, 0x161616));
            guiGraphics.fill(x, y, x + width, y + 1, argb(alpha, 0x3A3A3A));
            guiGraphics.fill(x, y, x + 1, y + height, argb(alpha, 0x3A3A3A));
            guiGraphics.fill(x, y + height - 1, x + width, y + height, argb(alpha, 0x0E0E0E));
            guiGraphics.fill(x + width - 1, y, x + width, y + height, argb(alpha, 0x0E0E0E));
        }

        int iconSize = mini ? 12 : 18;
        int iconX = x + (horizontal ? (mini ? 3 : 9) : 3);
        int iconY = y + 3;
        if (!horizontal) {
            iconX = x + 3;
        }
        TextureAtlasSprite sprite = minecraft.getMobEffectTextures().get(effect.getEffect());
        guiGraphics.blit(iconX, iconY, 0, iconSize, iconSize, sprite);

        int level = effect.getAmplifier() + 1;
        String levelText = !config.potionLevels && level > 9 ? "∞" : Integer.toString(level);
        int levelX = x + (horizontal ? (mini ? 9 : 21) : (mini ? 9 : 15));
        guiGraphics.drawString(minecraft.font, levelText, levelX, y + 1, WHITE, false);

        if (mini) {
            drawPotionBar(guiGraphics, effect, x, y, horizontal);
        } else {
            String time = formatDuration(effect);
            int timeColor = isShortDuration(effect) ? 0xFFFF5555 : WHITE;
            int timeX = horizontal ? x + 5 : x + 28;
            int timeY = horizontal ? y + 25 : y + 8;
            guiGraphics.drawString(minecraft.font, time, timeX, timeY, timeColor, true);
        }
    }

    private static void drawPotionBar(GuiGraphics guiGraphics, MobEffectInstance effect,
                                      int x, int y, boolean horizontal) {
        float scale = barScale(effect);
        int color = barColor(scale);
        if (horizontal) {
            int width = Math.max(1, Math.round(18.0F * scale));
            guiGraphics.fill(x + 3, y + 18, x + 18, y + 21, 0xFF202020);
            guiGraphics.fill(x + 3, y + 18, x + 3 + width, y + 21, color);
        } else {
            int height = Math.max(1, Math.round(12.0F * scale));
            guiGraphics.fill(x + 17, y + 2, x + 20, y + 15, 0xFF202020);
            guiGraphics.fill(x + 17, y + 15 - height, x + 20, y + 15, color);
        }
    }

    private static float barScale(MobEffectInstance effect) {
        if (effect.isInfiniteDuration()) {
            return 1.0F;
        }
        return Math.min(1.0F, Math.max(0.0F, effect.getDuration() / 6000.0F));
    }

    private static int barColor(float scale) {
        float red = 1.0F;
        float green = 1.0F;
        if (scale < 0.5F) {
            green = scale / 0.5F;
        } else {
            red = 1.0F - (scale - 0.5F) / 0.5F;
        }
        return 0xFF000000 | ((int) (red * 255.0F) << 16) | ((int) (green * 255.0F) << 8);
    }

    // -------------------------------------------------------------------------
    // Equipment: the six vanilla equipment slots around the hotbar.
    // -------------------------------------------------------------------------

    private static void renderEquipment(GuiGraphics guiGraphics, Player player, HudConfig config,
                                        Font font, int width, int height) {
        int leftX = width / 2 - 136;
        int rightX = width / 2 + 120;
        int topY = height - 74;
        int middleY = height - 56;
        int bottomY = height - 38;
        boolean leftTextRight = player.getOffhandItem().isEmpty();

        if (config.showHeld) {
            drawEquipmentItem(guiGraphics, font, config, player.getMainHandItem(),
                    leftX, topY, EMPTY_MAINHAND, leftTextRight);
            drawEquipmentItem(guiGraphics, font, config, player.getOffhandItem(),
                    rightX, topY, EMPTY_SHIELD, false);
        }
        if (config.showArmor) {
            drawEquipmentItem(guiGraphics, font, config, player.getItemBySlot(EquipmentSlot.HEAD),
                    leftX, middleY, EMPTY_HELMET, leftTextRight);
            drawEquipmentItem(guiGraphics, font, config, player.getItemBySlot(EquipmentSlot.CHEST),
                    leftX, bottomY, EMPTY_CHESTPLATE, leftTextRight);
            drawEquipmentItem(guiGraphics, font, config, player.getItemBySlot(EquipmentSlot.LEGS),
                    rightX, middleY, EMPTY_LEGGINGS, false);
            drawEquipmentItem(guiGraphics, font, config, player.getItemBySlot(EquipmentSlot.FEET),
                    rightX, bottomY, EMPTY_BOOTS, false);
        }

        int countY = height - 20;
        if (config.showArrows) {
            // Keep both counters readable even when the offhand is filled; the free-slot
            // counter occupies the space to the left of this icon.
            drawEquipmentIcon(guiGraphics, font, ARROWS, Integer.toString(countArrows(player)),
                    leftX, countY, true);
        }
        if (config.showFreeSlots) {
            // The chest-rig weapon panel and its two counters occupy the right side of
            // the hotbar. Keep the inventory count on the left so neither readout covers
            // the other; the arrows counter stays next to it in the default position.
            int freeX = leftX - 20;
            String freeText = Integer.toString(freeSlots(player));
            if (freeX - 1 - font.width(freeText) >= 0) {
                drawEquipmentIcon(guiGraphics, font, INVENTORY, freeText, freeX, countY, false);
            }
        }
    }

    private static void drawEquipmentItem(GuiGraphics guiGraphics, Font font, HudConfig config,
                                          ItemStack stack, int x, int y, ResourceLocation emptyIcon,
                                          boolean textRight) {
        if (stack.isEmpty()) {
            if (config.showEmpty) {
                guiGraphics.blit(emptyIcon, x, y, 0.0F, 0.0F, 16, 16, 16, 16);
            }
            return;
        }

        guiGraphics.renderItem(stack, x, y);
        if (!stack.isDamageableItem()) {
            guiGraphics.renderItemDecorations(font, stack, x, y);
            return;
        }
        if (!config.showDurability || stack.getMaxDamage() <= 0) {
            return;
        }
        int percent = (stack.getMaxDamage() - stack.getDamageValue()) * 100 / stack.getMaxDamage();
        String text = percent + "%";
        int textX = textRight ? x + 17 : x - 1 - font.width(text);
        int color = durabilityColor(percent);
        guiGraphics.drawString(font, text, textX, y + 4, color, true);
    }

    private static void drawEquipmentIcon(GuiGraphics guiGraphics, Font font, ResourceLocation icon,
                                          String text, int x, int y, boolean textRight) {
        guiGraphics.blit(icon, x, y, 0.0F, 0.0F, 16, 16, 16, 16);
        int textX = textRight ? x + 17 : x - 1 - font.width(text);
        guiGraphics.drawString(font, text, textX, y + 4, WHITE, true);
    }

    private static int durabilityColor(int percent) {
        if (percent >= 100) {
            return 0xFF55FF55;
        }
        if (percent < 1) {
            return 0xFFAA0000;
        }
        if (percent <= 10) {
            return 0xFFFF5555;
        }
        if (percent <= 25) {
            return 0xFFFFAA00;
        }
        if (percent <= 50) {
            return 0xFFFFFF55;
        }
        return WHITE;
    }

    // -------------------------------------------------------------------------
    // Utilities.
    // -------------------------------------------------------------------------

    private static String formatDuration(MobEffectInstance effect) {
        if (effect.isInfiniteDuration()) {
            return "**:**";
        }
        int seconds = Math.max(0, effect.getDuration() / 20);
        if (seconds >= 1600) {
            return "**:**";
        }
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private static boolean isShortDuration(MobEffectInstance effect) {
        return !effect.isInfiniteDuration() && effect.getDuration() / 20 <= 10;
    }

    private static int freeSlots(Player player) {
        int free = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) {
                free++;
            }
        }
        return free;
    }

    private static int countArrows(Player player) {
        int arrows = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (AmmoSlots.isArrow(stack)) {
                arrows += stack.getCount();
            }
        }
        ItemStack offhand = player.getOffhandItem();
        if (AmmoSlots.isArrow(offhand)) {
            arrows += offhand.getCount();
        }
        return arrows + ChestRig.totalArrows(player);
    }

    private static int alphaByte(int percent) {
        return Math.round(255.0F * clamp(percent, 0, 100) / 100.0F);
    }

    private static int argb(int alpha, int rgb) {
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

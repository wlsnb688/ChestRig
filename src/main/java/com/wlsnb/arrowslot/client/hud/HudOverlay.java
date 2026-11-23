package com.wlsnb.arrowslot.client.hud;

import com.wlsnb.arrowslot.inventory.ChestRig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/**
 * Built-in HUD: optional inventory grid, a vanilla-style top-right effect readout, and two
 * flat equipment panels in the bottom corners.
 */
public final class HudOverlay {
    private static final int SLOT = 18;
    private static final int INV_COLS = 9;
    private static final int INV_ROWS = 3;

    private static final int WHITE = 0xFFFFFFFF;
    private static final int NORMAL_WIDTH = 46;
    private static final int NORMAL_HEIGHT = 72;
    private static final int MINI_WIDTH = 74;
    private static final int MINI_HEIGHT = 36;
    private static final int MINI_CELL_WIDTH = 36;
    private static final int MINI_GAP = 2;
    private static final int ROW = 18;
    private static final float MINI_TEXT_SCALE = 0.75F;

    private static final ResourceLocation EFFECT_BACKGROUND =
            ResourceLocation.withDefaultNamespace("hud/effect_background");
    private static final ResourceLocation EFFECT_BACKGROUND_AMBIENT =
            ResourceLocation.withDefaultNamespace("hud/effect_background_ambient");

    private static final ResourceLocation EMPTY_HELMET = tex("empty/helmet");
    private static final ResourceLocation EMPTY_CHESTPLATE = tex("empty/chestplate");
    private static final ResourceLocation EMPTY_LEGGINGS = tex("empty/leggings");
    private static final ResourceLocation EMPTY_BOOTS = tex("empty/boots");
    private static final ResourceLocation EMPTY_SHIELD = tex("empty/shield");
    private static final ResourceLocation EMPTY_MAINHAND = tex("empty/mainhand");
    private static final ResourceLocation EMPTY_ARROW =
            ResourceLocation.fromNamespaceAndPath("arrowslot", "textures/item/empty_slot_arrow.png");
    private static final ResourceLocation EMPTY_BREAD =
            ResourceLocation.fromNamespaceAndPath("arrowslot", "textures/item/empty_slot_bread.png");
    private static final ResourceLocation EMPTY_DRUMSTICK =
            ResourceLocation.fromNamespaceAndPath("arrowslot", "textures/item/empty_slot_drumstick.png");

    private HudOverlay() {
    }

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath("arrowslot", "textures/gui/" + path + ".png");
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.getOverlay() != null
                || minecraft.screen instanceof LevelLoadingScreen
                || minecraft.screen instanceof ReceivingLevelScreen) {
            return;
        }
        HudConfig config = HudConfig.get();
        if (minecraft.options.hideGui || !config.enabled) {
            return;
        }

        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        if (config.inventoryEnabled) {
            renderInventory(guiGraphics, player, config, minecraft.font, width, height);
        }
        if (config.potionEnabled) {
            renderEffects(guiGraphics, player, config, minecraft, width);
        }
        if (config.armorHudEnabled) {
            renderArmorPanel(guiGraphics, player, config, minecraft.font, width, height);
        }
        if (config.handsHudEnabled) {
            renderHandsPanel(guiGraphics, player, config, minecraft, width, height);
        }
    }

    // -------------------------------------------------------------------------
    // Inventory: 27 main slots, centred above the hotbar by default.
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
        int scaledWidth = Math.round(contentWidth * scale);
        int scaledHeight = Math.round(contentHeight * scale);

        int x;
        int y;
        if (config.inventoryAnchored) {
            x = Math.round((screenWidth - scaledWidth) / 2.0F) + config.inventoryX;
            y = screenHeight - 24 - scaledHeight + config.inventoryY;
            y = Math.max(0, Math.min(y, screenHeight - 24 - scaledHeight));
        } else {
            x = config.inventoryX;
            y = config.inventoryY;
        }
        x = clamp(x, 0, Math.max(0, screenWidth - scaledWidth));
        y = clamp(y, 0, Math.max(0, screenHeight - scaledHeight));

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
    // Effects: same top-right two-row placement as the vanilla effect HUD.
    // -------------------------------------------------------------------------

    private static void renderEffects(GuiGraphics guiGraphics, Player player, HudConfig config,
                                      Minecraft minecraft, int screenWidth) {
        if (minecraft.screen instanceof EffectRenderingInventoryScreen<?>) {
            return;
        }
        List<MobEffectInstance> effects = new ArrayList<>();
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (effect.showIcon()) {
                effects.add(effect);
            }
        }
        if (effects.isEmpty()) {
            return;
        }
        effects.sort((a, b) -> b.compareTo(a));

        int beneficial = 0;
        int harmful = 0;
        for (MobEffectInstance effect : effects) {
            boolean good = effect.getEffect().value().isBeneficial();
            int column = good ? ++beneficial : ++harmful;
            int y = good ? 1 : 27;
            if (minecraft.isDemo()) {
                y += 15;
            }
            int x = screenWidth - 25 * column;
            drawEffect(guiGraphics, minecraft, effect, x, y);
        }
    }

    private static void drawEffect(GuiGraphics guiGraphics, Minecraft minecraft,
                                   MobEffectInstance effect, int x, int y) {
        ResourceLocation background = effect.isAmbient() ? EFFECT_BACKGROUND_AMBIENT : EFFECT_BACKGROUND;
        guiGraphics.blitSprite(background, x, y, 24, 24);

        TextureAtlasSprite sprite = minecraft.getMobEffectTextures().get(effect.getEffect());
        guiGraphics.blit(x + 3, y + 3, 0, 18, 18, sprite);

        Font font = minecraft.font;
        int level = effect.getAmplifier() + 1;
        if (level > 1) {
            drawScaledString(guiGraphics, font, roman(level), x + 3, y + 2, WHITE, MINI_TEXT_SCALE);
        }

        String duration = formatDuration(effect);
        int color = isShortDuration(effect) ? 0xFFFF5555 : WHITE;
        guiGraphics.fill(x + 2, y + 13, x + 22, y + 22, 0x66000000);
        drawScaledString(guiGraphics, font, duration, x + 4, y + 14, color, MINI_TEXT_SCALE);
    }

    private static String roman(int value) {
        return switch (value) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> Integer.toString(value);
        };
    }

    // -------------------------------------------------------------------------
    // Equipment: normal mode is a single column; mini mode is two rows of columns.
    // -------------------------------------------------------------------------

    public static int[] leftInfoBox(HudConfig config) {
        Minecraft minecraft = Minecraft.getInstance();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int width = config.equipmentMini ? MINI_WIDTH : NORMAL_WIDTH;
        int height = config.equipmentMini ? MINI_HEIGHT : NORMAL_HEIGHT;
        int x = config.leftInfoAnchored ? 4 + config.leftInfoX : config.leftInfoX;
        int y = config.leftInfoAnchored ? screenHeight - height - 4 + config.leftInfoY : config.leftInfoY;
        return clampBox(x, y, width, height, screenWidth, screenHeight);
    }

    public static int[] rightInfoBox(HudConfig config) {
        Minecraft minecraft = Minecraft.getInstance();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int width = config.equipmentMini ? MINI_WIDTH : NORMAL_WIDTH;
        int height = config.equipmentMini ? MINI_HEIGHT : NORMAL_HEIGHT;
        int x = config.rightInfoAnchored
                ? screenWidth - width - 4 + config.rightInfoX
                : config.rightInfoX;
        int y = config.rightInfoAnchored ? screenHeight - height - 4 + config.rightInfoY : config.rightInfoY;
        return clampBox(x, y, width, height, screenWidth, screenHeight);
    }

    private static int[] clampBox(int x, int y, int width, int height, int screenWidth, int screenHeight) {
        x = clamp(x, 0, Math.max(0, screenWidth - width));
        y = clamp(y, 0, Math.max(0, screenHeight - height));
        return new int[]{x, y, width, height};
    }

    private static void renderArmorPanel(GuiGraphics guiGraphics, Player player, HudConfig config,
                                         Font font, int screenWidth, int screenHeight) {
        int[] box = leftInfoBox(config);
        int x = box[0];
        int y = box[1];
        if (config.equipmentMini) {
            drawGearCell(guiGraphics, font, config, player.getItemBySlot(EquipmentSlot.HEAD),
                    EMPTY_HELMET, x, y, true);
            drawGearCell(guiGraphics, font, config, player.getItemBySlot(EquipmentSlot.CHEST),
                    EMPTY_CHESTPLATE, x + MINI_CELL_WIDTH + MINI_GAP, y, true);
            drawGearCell(guiGraphics, font, config, player.getItemBySlot(EquipmentSlot.LEGS),
                    EMPTY_LEGGINGS, x, y + ROW, true);
            drawGearCell(guiGraphics, font, config, player.getItemBySlot(EquipmentSlot.FEET),
                    EMPTY_BOOTS, x + MINI_CELL_WIDTH + MINI_GAP, y + ROW, true);
        } else {
            drawGearCell(guiGraphics, font, config, player.getItemBySlot(EquipmentSlot.HEAD),
                    EMPTY_HELMET, x, y, false);
            drawGearCell(guiGraphics, font, config, player.getItemBySlot(EquipmentSlot.CHEST),
                    EMPTY_CHESTPLATE, x, y + ROW, false);
            drawGearCell(guiGraphics, font, config, player.getItemBySlot(EquipmentSlot.LEGS),
                    EMPTY_LEGGINGS, x, y + 2 * ROW, false);
            drawGearCell(guiGraphics, font, config, player.getItemBySlot(EquipmentSlot.FEET),
                    EMPTY_BOOTS, x, y + 3 * ROW, false);
        }
    }

    private static void renderHandsPanel(GuiGraphics guiGraphics, Player player, HudConfig config,
                                         Minecraft minecraft, int screenWidth, int screenHeight) {
        Font font = minecraft.font;
        int[] box = rightInfoBox(config);
        int x = box[0];
        int y = box[1];
        int secondColumn = x + MINI_CELL_WIDTH + MINI_GAP;

        int arrows = ChestRig.totalArrows(player);
        int foodItems = ChestRig.totalFoodItems(player);
        boolean appleSkin = ModList.get().isLoaded("appleskin");
        String arrowText = Integer.toString(arrows);
        String foodText = Integer.toString(appleSkin ? ChestRig.totalFoodNutrition(player) : foodItems);

        if (config.equipmentMini) {
            drawGearCell(guiGraphics, font, config, player.getMainHandItem(), EMPTY_MAINHAND,
                    x, y, true);
            drawGearCell(guiGraphics, font, config, player.getOffhandItem(), EMPTY_SHIELD,
                    secondColumn, y, true);
            drawArrowCounter(guiGraphics, font, config, arrows, arrowText, x, y + ROW, true);
            drawFoodCounter(guiGraphics, font, config, appleSkin, foodItems, foodText,
                    secondColumn, y + ROW, true);
        } else {
            drawGearCell(guiGraphics, font, config, player.getMainHandItem(), EMPTY_MAINHAND,
                    x, y, false);
            drawGearCell(guiGraphics, font, config, player.getOffhandItem(), EMPTY_SHIELD,
                    x, y + ROW, false);
            drawArrowCounter(guiGraphics, font, config, arrows, arrowText, x, y + 2 * ROW, false);
            drawFoodCounter(guiGraphics, font, config, appleSkin, foodItems, foodText,
                    x, y + 3 * ROW, false);
        }
    }

    private static void drawArrowCounter(GuiGraphics guiGraphics, Font font, HudConfig config,
                                         int arrows, String text, int x, int y, boolean mini) {
        if (arrows > 0) {
            guiGraphics.renderItem(new ItemStack(Items.ARROW), x, y);
        } else {
            guiGraphics.blit(EMPTY_ARROW, x, y, 0.0F, 0.0F, 16, 16, 16, 16);
        }
        drawCounterText(guiGraphics, font, text, x, y, mini);
    }

    private static void drawFoodCounter(GuiGraphics guiGraphics, Font font, HudConfig config,
                                        boolean appleSkin, int foodItems, String text,
                                        int x, int y, boolean mini) {
        if (appleSkin) {
            if (foodItems > 0) {
                guiGraphics.renderItem(new ItemStack(Items.COOKED_CHICKEN), x, y);
            } else {
                guiGraphics.blit(EMPTY_DRUMSTICK, x, y, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        } else {
            if (foodItems > 0) {
                guiGraphics.renderItem(new ItemStack(Items.BREAD), x, y);
            } else {
                guiGraphics.blit(EMPTY_BREAD, x, y, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        }
        drawCounterText(guiGraphics, font, text, x, y, mini);
    }

    private static void drawCounterText(GuiGraphics guiGraphics, Font font, String text,
                                        int x, int y, boolean mini) {
        if (mini) {
            drawScaledString(guiGraphics, font, text, x + 18, y + 5, WHITE, MINI_TEXT_SCALE);
        } else {
            guiGraphics.drawString(font, text, x + 18, y + 4, WHITE, true);
        }
    }

    private static void drawGearCell(GuiGraphics guiGraphics, Font font, HudConfig config, ItemStack stack,
                                     ResourceLocation emptyIcon, int x, int y, boolean mini) {
        if (stack.isEmpty()) {
            if (config.showEmpty) {
                guiGraphics.blit(emptyIcon, x, y, 0.0F, 0.0F, 16, 16, 16, 16);
            }
            return;
        }

        guiGraphics.renderItem(stack, x, y);
        if (!stack.isDamageableItem()) {
            if (stack.isStackable()) {
                // Hide the corner stack count and print the count where durability would be.
                String count = Integer.toString(stack.getCount());
                if (mini) {
                    drawScaledString(guiGraphics, font, count, x + 18, y + 5, WHITE, MINI_TEXT_SCALE);
                } else {
                    guiGraphics.drawString(font, count, x + 18, y + 4, WHITE, true);
                }
            }
            return;
        }
        if (!config.showDurability || stack.getMaxDamage() <= 0) {
            return;
        }
        int percent = (stack.getMaxDamage() - stack.getDamageValue()) * 100 / stack.getMaxDamage();
        String text = percent + "%";
        int color = durabilityColor(percent);
        if (mini) {
            drawScaledString(guiGraphics, font, text, x + 18, y + 5, color, MINI_TEXT_SCALE);
        } else {
            guiGraphics.drawString(font, text, x + 18, y + 4, color, true);
        }
    }

    private static void drawScaledString(GuiGraphics guiGraphics, Font font, String text,
                                         int x, int y, int color, float scale) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0.0F);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.drawString(font, text, 0, 0, color, true);
        guiGraphics.pose().popPose();
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

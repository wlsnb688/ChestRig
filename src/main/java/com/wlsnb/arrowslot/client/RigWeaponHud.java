package com.wlsnb.arrowslot.client;

import com.wlsnb.arrowslot.ArrowSlotMod;
import com.wlsnb.arrowslot.inventory.ChestRig;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

public final class RigWeaponHud {
    private static final int PANEL_WIDTH = 29;
    private static final int PANEL_HEIGHT = 24;
    private static final ResourceLocation HOTBAR_OFFHAND_RIGHT =
            ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_right");
    private static final ResourceLocation EMPTY_ARROW =
            ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, "textures/item/empty_slot_arrow.png");
    private static final ResourceLocation EMPTY_BREAD =
            ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, "textures/item/empty_slot_bread.png");
    private static final ResourceLocation EMPTY_DRUMSTICK =
            ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, "textures/item/empty_slot_drumstick.png");
    private static final ResourceLocation EMPTY_SWORD =
            ResourceLocation.withDefaultNamespace("textures/item/empty_slot_sword.png");

    private RigWeaponHud() {
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui) {
            return;
        }

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int panelX = screenWidth / 2 + 91;
        int panelY = screenHeight - 23;

        // Leave the vanilla hotbar attack indicator its normal place on the right.
        if (minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR
                && player.getMainArm() == HumanoidArm.RIGHT
                && panelX + 24 + 59 <= screenWidth) {
            panelX += 24;
        }

        // The vanilla offhand panel is drawn on the right only for left-handed players.
        // Shift our panel past it instead of covering the offhand item.
        boolean offhandOnRight = player.getMainArm() == HumanoidArm.LEFT && !player.getOffhandItem().isEmpty();
        int shifted = panelX + PANEL_WIDTH + 1;
        if (offhandOnRight && shifted + 59 <= screenWidth) {
            panelX = shifted;
        }
        panelX = Math.min(panelX, screenWidth - PANEL_WIDTH);

        // Same sprite and item position as the vanilla offhand hotbar panel. Resource packs
        // replace this sprite together with the rest of the hotbar.
        guiGraphics.blitSprite(HOTBAR_OFFHAND_RIGHT, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);

        int itemX = panelX + 10;
        int itemY = panelY + 4;
        ItemStack display = displayStack(player);
        if (display.isEmpty()) {
            drawEmpty(guiGraphics, EMPTY_SWORD, itemX, itemY);
        } else {
            guiGraphics.renderItem(display, itemX, itemY);
            guiGraphics.renderItemDecorations(minecraft.font, display, itemX, itemY);
        }

        if (RigWeaponClientState.isActive()) {
            guiGraphics.renderOutline(itemX - 1, itemY - 1, 18, 18, 0xFFFF3030);
        }

        int arrows = ChestRig.totalArrows(player);
        int foodItems = ChestRig.totalFoodItems(player);
        boolean appleSkin = ModList.get().isLoaded("appleskin");
        int iconX = panelX + 31;
        int textX = iconX + 10;
        int topY = panelY + 3;

        if (arrows > 0) {
            drawSmallItem(guiGraphics, new ItemStack(Items.ARROW), iconX, topY);
        } else {
            drawSmallEmpty(guiGraphics, EMPTY_ARROW, iconX, topY);
        }
        guiGraphics.drawString(minecraft.font, Integer.toString(arrows), textX, topY + 1, 0xFFFFFFFF, true);

        int bottomY = panelY + 13;
        if (appleSkin) {
            if (foodItems > 0) {
                drawSmallItem(guiGraphics, new ItemStack(Items.COOKED_CHICKEN), iconX, bottomY);
            } else {
                drawSmallEmpty(guiGraphics, EMPTY_DRUMSTICK, iconX, bottomY);
            }
            guiGraphics.drawString(
                    minecraft.font,
                    Integer.toString(ChestRig.totalFoodNutrition(player)),
                    textX,
                    bottomY + 1,
                    0xFFFFFFFF,
                    true
            );
        } else {
            if (foodItems > 0) {
                drawSmallItem(guiGraphics, new ItemStack(Items.BREAD), iconX, bottomY);
            } else {
                drawSmallEmpty(guiGraphics, EMPTY_BREAD, iconX, bottomY);
            }
            guiGraphics.drawString(minecraft.font, Integer.toString(foodItems), textX, bottomY + 1, 0xFFFFFFFF, true);
        }
    }

    private static ItemStack displayStack(Player player) {
        if (RigWeaponClientState.isActive()) {
            ItemStack mainHand = player.getMainHandItem();
            if (!mainHand.isEmpty()) {
                return mainHand;
            }
        }

        int index = RigWeaponClientState.index();
        ItemStack stack = ChestRig.getWeapon(player, index);
        if (!stack.isEmpty()) {
            return stack;
        }
        int first = ChestRig.firstWeaponIndex(player);
        return first >= 0 ? ChestRig.getWeapon(player, first) : ItemStack.EMPTY;
    }

    private static void drawEmpty(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y) {
        guiGraphics.blit(texture, x, y, 0.0F, 0.0F, 16, 16, 16, 16);
    }

    private static void drawSmallEmpty(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y) {
        guiGraphics.blit(texture, x, y, 0.0F, 0.0F, 8, 8, 16, 16);
    }

    private static void drawSmallItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0.0F);
        guiGraphics.pose().scale(0.5F, 0.5F, 1.0F);
        guiGraphics.renderItem(stack, 0, 0);
        guiGraphics.pose().popPose();
    }
}

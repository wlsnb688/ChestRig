package com.wlsnb.arrowslot.client;

import com.wlsnb.arrowslot.ArrowSlotMod;
import com.wlsnb.arrowslot.equipment.RigWeaponState;
import com.wlsnb.arrowslot.inventory.ChestRig;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The chest-rig weapon slot next to the hotbar, using the vanilla offhand-panel sprite.
 * Counters and equipment information are drawn by {@link com.wlsnb.arrowslot.client.hud.HudOverlay}.
 */
public final class RigWeaponHud {
    private static final int PANEL_WIDTH = 29;
    private static final int PANEL_HEIGHT = 24;
    private static final ResourceLocation HOTBAR_OFFHAND_RIGHT =
            ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_right");
    private static final ResourceLocation EMPTY_SWORD =
            ResourceLocation.withDefaultNamespace("textures/item/empty_slot_sword.png");

    private RigWeaponHud() {
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui || !shouldShow(player)) {
            return;
        }
        if (minecraft.level == null || minecraft.getOverlay() != null
                || minecraft.screen instanceof LevelLoadingScreen
                || minecraft.screen instanceof ReceivingLevelScreen) {
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
            guiGraphics.blit(EMPTY_SWORD, itemX, itemY, 0.0F, 0.0F, 16, 16, 16, 16);
        } else {
            guiGraphics.renderItem(display, itemX, itemY);
            guiGraphics.renderItemDecorations(minecraft.font, display, itemX, itemY);
        }

        if (RigWeaponClientState.isActive()) {
            // A thickened hotbar-style selection frame around the 20x20 item area.
            guiGraphics.renderOutline(itemX - 2, itemY - 2, 20, 20, 0xFFFFFFFF);
            guiGraphics.renderOutline(itemX - 3, itemY - 3, 22, 22, 0xFFFFFFFF);
        }
    }

    private static boolean shouldShow(Player player) {
        if (ChestRig.getChestplate(player).isEmpty()) {
            return false;
        }
        if (ChestRig.firstWeaponIndex(player) >= 0) {
            return true;
        }
        // The only weapon was a trident that is currently in flight. Keep the selected
        // empty slot visible until G or a successful hotbar change ends the selection.
        return RigWeaponState.isPending(player);
    }

    private static ItemStack displayStack(Player player) {
        if (RigWeaponClientState.isActive()) {
            // Use the synced rig stack, not the temporary auto-eat food held in the
            // client-side main hand for the eating animation.
            ItemStack held = RigWeaponState.held(player);
            return held.isEmpty() ? ItemStack.EMPTY : held;
        }

        int index = RigWeaponClientState.index();
        ItemStack stack = ChestRig.getWeapon(player, index);
        if (!stack.isEmpty()) {
            return stack;
        }
        int first = ChestRig.firstWeaponIndex(player);
        return first >= 0 ? ChestRig.getWeapon(player, first) : ItemStack.EMPTY;
    }
}

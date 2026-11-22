package com.wlsnb.arrowslot.client;

import com.wlsnb.arrowslot.ammo.AmmoSlots;
import com.wlsnb.arrowslot.network.SelectAmmoPayload;
import com.wlsnb.arrowslot.network.SyncSelectionPayload;
import com.wlsnb.arrowslot.registry.ModDataComponents;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ClientAmmoState {
    private static final long QUICK_SWITCH_HUD_MS = 2000L;
    private static final Map<UUID, Integer> SELECTIONS = new HashMap<>();

    private static boolean hudVisible;
    private static long quickHudUntil;

    private ClientAmmoState() {
    }

    /**
     * True only when the player is genuinely crouching. Elytra flight, creative flight and
     * swimming must not open the HUD or allow scroll selection.
     */
    public static boolean isActuallySneaking(Player player) {
        if (player.isFallFlying() || player.isSwimming()) {
            return false;
        }
        // Pose.CROUCHING is the normal case. The on-ground fallback also covers creative
        // flight that has just landed while the flight ability itself is still enabled.
        return player.getPose() == Pose.CROUCHING
                || (player.onGround() && player.isShiftKeyDown());
    }

    public static void accept(SyncSelectionPayload payload) {
        if (payload.candidate() < 0) {
            SELECTIONS.remove(payload.weaponId());
        } else {
            SELECTIONS.put(payload.weaponId(), payload.candidate());
        }
    }

    public static int selected(Player player, ItemStack weapon) {
        UUID id = weapon.get(ModDataComponents.WEAPON_ID.get());
        if (id != null) {
            Integer selection = SELECTIONS.get(id);
            if (selection != null) {
                return selection;
            }
        }
        return AmmoSlots.findDefault(player, weapon);
    }

    public static List<Integer> visibleSlots(Player player) {
        return AmmoSlots.visibleArrowSlots(player);
    }

    public static boolean isHudVisible() {
        return hudVisible;
    }

    public static void scroll(int direction) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null || !isActuallySneaking(player)) {
            return;
        }

        ItemStack weapon = AmmoSlots.getHeldWeapon(player);
        if (weapon.isEmpty()) {
            return;
        }

        List<Integer> slots = visibleSlots(player);
        if (slots.isEmpty()) {
            hudVisible = true;
            return;
        }

        int current = selected(player, weapon);
        int position = slots.indexOf(current);
        if (position < 0) {
            position = direction > 0 ? -1 : 0;
        }
        int next = slots.get(Math.floorMod(position + direction, slots.size()));

        hudVisible = true;
        UUID id = weapon.get(ModDataComponents.WEAPON_ID.get());
        if (id != null) {
            SELECTIONS.put(id, next);
        }
        PacketDistributor.sendToServer(new SelectAmmoPayload(next));
    }

    /**
     * Cycle the selected arrow slot and keep the HUD visible for two seconds. This shortcut
     * is independent of sneaking and of the flight/swim restrictions.
     */
    public static void quickSwitch(int direction) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null) {
            return;
        }

        ItemStack weapon = AmmoSlots.getHeldWeapon(player);
        if (weapon.isEmpty()) {
            return;
        }

        quickHudUntil = Util.getMillis() + QUICK_SWITCH_HUD_MS;
        hudVisible = true;

        List<Integer> slots = visibleSlots(player);
        if (slots.isEmpty()) {
            return;
        }

        int current = selected(player, weapon);
        int position = slots.indexOf(current);
        if (position < 0) {
            position = direction > 0 ? -1 : 0;
        }
        int next = slots.get(Math.floorMod(position + direction, slots.size()));

        UUID id = weapon.get(ModDataComponents.WEAPON_ID.get());
        if (id != null) {
            SELECTIONS.put(id, next);
        }
        PacketDistributor.sendToServer(new SelectAmmoPayload(next));
    }

    /**
     * Called when the middle mouse button is pressed while the HUD is shown.
     *
     * @return true when the click was consumed
     */
    public static boolean resetSelection() {
        if (!hudVisible) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null || player.hasInfiniteMaterials()) {
            return false;
        }

        ItemStack weapon = AmmoSlots.getHeldWeapon(player);
        if (weapon.isEmpty()) {
            return false;
        }

        int defaultSlot = AmmoSlots.findDefault(player, weapon);
        UUID id = weapon.get(ModDataComponents.WEAPON_ID.get());
        if (id != null) {
            if (defaultSlot >= 0) {
                SELECTIONS.put(id, defaultSlot);
            } else {
                SELECTIONS.remove(id);
            }
        }

        PacketDistributor.sendToServer(new SelectAmmoPayload(-1));
        return true;
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            reset();
            return;
        }

        ItemStack weapon = AmmoSlots.getHeldWeapon(player);
        boolean quickHud = Util.getMillis() < quickHudUntil;
        hudVisible = minecraft.screen == null
                && !weapon.isEmpty()
                && (isActuallySneaking(player) || quickHud);

        if (!hudVisible) {
            return;
        }

        // If the selected stack was consumed or moved while the HUD is open, keep a
        // visible slot selected instead of leaving the red border pointing at nothing.
        ensureVisibleSelection(player, weapon);
    }

    public static void reset() {
        // Keep the UUID -> selection map across dimension travel and respawn. The player
        // object can briefly be null during those transitions, and the server preserves
        // its selections there. Stale entries are harmless (UUIDs are unique per weapon)
        // and are overwritten by the server's initial sync after a reconnect.
        hudVisible = false;
        quickHudUntil = 0L;
    }

    private static void ensureVisibleSelection(Player player, ItemStack weapon) {
        List<Integer> slots = visibleSlots(player);
        if (slots.isEmpty()) {
            return;
        }

        int current = selected(player, weapon);
        if (slots.contains(current)) {
            return;
        }

        int next = slots.get(0);
        UUID id = weapon.get(ModDataComponents.WEAPON_ID.get());
        if (id != null) {
            SELECTIONS.put(id, next);
        }
        PacketDistributor.sendToServer(new SelectAmmoPayload(next));
    }
}

package com.wlsnb.arrowslot.equipment;

import com.wlsnb.arrowslot.inventory.ChestRig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Common held-item state for a chest-rig weapon.
 *
 * <p>The selected rig weapon is exposed through {@link Player#getItemBySlot} without
 * moving it out of the chest rig. That keeps the real hotbar stack untouched while the
 * player still attacks, mines, uses items and renders with the rig weapon.</p>
 *
 * <p>The server keeps the authoritative {@link #held(Player)} stack and writes changes
 * back to the chestplate. The client receives the same stack in the sync payload, so
 * item use and rendering have a stable object to work with.</p>
 */
public final class RigWeaponState {
    // Integrated servers and clients are separate threads in the same JVM and use
    // different Player instances, but they still share this static map.
    private static final Map<Player, State> STATES = Collections.synchronizedMap(new WeakHashMap<>());

    private RigWeaponState() {
    }

    public static boolean isActive(Player player) {
        State state = STATES.get(player);
        return state != null && state.active;
    }

    public static boolean isGMode(Player player) {
        State state = STATES.get(player);
        return state != null && state.active && state.gMode;
    }

    public static int index(Player player) {
        State state = STATES.get(player);
        return state == null ? 0 : state.index;
    }

    public static int hotbarSlot(Player player) {
        State state = STATES.get(player);
        return state == null ? -1 : state.hotbarSlot;
    }

    public static ItemStack held(Player player) {
        State state = STATES.get(player);
        return state != null && state.active ? state.held : ItemStack.EMPTY;
    }

    public static void activate(Player player, int index, boolean gMode, int hotbarSlot, ItemStack stack) {
        State state = state(player);
        state.active = true;
        state.gMode = gMode;
        state.index = index;
        state.hotbarSlot = hotbarSlot;
        state.held = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        state.lastSaved = state.held.copy();
    }

    /** Applies a server sync packet to a client player. */
    public static void applyClient(Player player, boolean active, boolean gMode, int index, ItemStack stack) {
        if (!active) {
            deactivate(player);
            return;
        }

        State state = state(player);
        state.active = true;
        state.gMode = gMode;
        state.index = index;
        state.held = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        state.lastSaved = ItemStack.EMPTY;
    }

    public static void setHeld(Player player, ItemStack stack) {
        State state = STATES.get(player);
        if (state != null && state.active) {
            state.held = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;
        }
    }

    public static void setIndex(Player player, int index) {
        State state = STATES.get(player);
        if (state != null) {
            state.index = index;
        }
    }

    public static void setGMode(Player player, boolean gMode) {
        State state = STATES.get(player);
        if (state != null) {
            state.gMode = gMode;
        }
    }

    public static void deactivate(Player player) {
        State state = STATES.get(player);
        if (state != null) {
            state.active = false;
            state.gMode = false;
            state.held = ItemStack.EMPTY;
            state.lastSaved = ItemStack.EMPTY;
        }
    }

    /**
     * Writes a changed held stack back to the chestplate. Returns true when the chestplate
     * contents changed, so the caller can broadcast the equipment slot.
     */
    public static boolean save(Player player) {
        State state = STATES.get(player);
        if (state == null || !state.active) {
            return false;
        }

        if (state.held.isEmpty()) {
            ChestRig.setWeapon(player, state.index, ItemStack.EMPTY);
            state.active = false;
            state.gMode = false;
            state.lastSaved = ItemStack.EMPTY;
            return true;
        }

        if (!ItemStack.matches(state.held, state.lastSaved)) {
            ChestRig.setWeapon(player, state.index, state.held);
            state.lastSaved = state.held.copy();
            return true;
        }

        return false;
    }

    public static void clear(Player player) {
        STATES.remove(player);
    }

    private static State state(Player player) {
        return STATES.computeIfAbsent(player, ignored -> new State());
    }

    private static final class State {
        private boolean active;
        private boolean gMode;
        private int index;
        private int hotbarSlot = -1;
        private ItemStack held = ItemStack.EMPTY;
        private ItemStack lastSaved = ItemStack.EMPTY;
    }
}

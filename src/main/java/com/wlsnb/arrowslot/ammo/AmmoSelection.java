package com.wlsnb.arrowslot.ammo;

import com.wlsnb.arrowslot.equipment.BrokenGear;
import com.wlsnb.arrowslot.inventory.ChestRig;
import com.wlsnb.arrowslot.network.ModNetwork;
import com.wlsnb.arrowslot.registry.ModDataComponents;
import com.wlsnb.arrowslot.util.PlayerKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-authoritative, deliberately non-persistent per-weapon ammo selection.
 *
 * <p>Weapons are identified by a synced UUID component so that moving a stack between
 * inventory slots does not lose its selection, while dropping it, putting it into a
 * container, or ending the session does. A player state is kept on both logical sides;
 * the server is authoritative and sends {@link com.wlsnb.arrowslot.network.SyncSelectionPayload}
 * whenever it changes a selection.</p>
 */
public final class AmmoSelection {
    // Entity#equals compares entity ids, which the client and integrated server share.
    // Include the logical side so client selections never overwrite server selections.
    private static final Map<PlayerKey, PlayerState> STATES = new ConcurrentHashMap<>();

    private AmmoSelection() {
    }

    /**
     * Called from the Player#getProjectile mixin. Returns the ammo stack the vanilla bow or
     * crossbow code should use, or an empty stack (or a creative arrow) when there is none.
     */
    public static ItemStack getProjectile(Player player, ItemStack weapon) {
        if (BrokenGear.isBroken(weapon)) {
            return ItemStack.EMPTY;
        }
        UUID id = ensureWeaponId(player, weapon);
        if (id == null) {
            // The client has not received the server-assigned id yet. Falling back to the
            // normal search order keeps bow drawing usable until the next slot sync.
            int fallback = AmmoSlots.findDefault(player, weapon);
            if (fallback >= 0) {
                return ammoFor(player, weapon, fallback);
            }
            return player.hasInfiniteMaterials() ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
        }

        int selected = resolve(player, weapon, id, true);
        if (selected >= 0) {
            ItemStack ammo = ammoFor(player, weapon, selected);
            if (!ammo.isEmpty()) {
                return ammo;
            }
        }
        return player.hasInfiniteMaterials() ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    private static ItemStack ammoFor(Player player, ItemStack weapon, int selected) {
        ItemStack ammo = AmmoSlots.get(player, selected);
        if (!AmmoSlots.isValidAmmo(weapon, ammo)) {
            return ItemStack.EMPTY;
        }
        if (!AmmoSlots.isRigArrowSlot(selected)) {
            return ammo;
        }

        // The chest rig stores its contents in a data component, so AmmoSlots.get returns a
        // copy. Mark the copy so ProjectileWeaponItem.useAmmo can consume the real stack.
        ItemStack marked = ammo.copy();
        marked.set(ModDataComponents.RIG_AMMO.get(), selected);
        return marked;
    }

    public static int selected(Player player, ItemStack weapon) {
        UUID id = ensureWeaponId(player, weapon);
        if (id == null) {
            return AmmoSlots.findDefault(player, weapon);
        }
        return resolve(player, weapon, id, false);
    }

    /** Returns the currently selected ammo stack without consuming it. */
    public static ItemStack peekSelected(Player player, ItemStack weapon) {
        int selected = selected(player, weapon);
        return selected < 0 ? ItemStack.EMPTY : AmmoSlots.get(player, selected).copy();
    }

    /**
     * Consumes one projectile from the selected ammo slot. Used by the crossbow magazine,
     * which loads ammo before vanilla's shooting path runs.
     */
    public static ItemStack consumeSelected(ServerPlayer player, ItemStack weapon) {
        if (BrokenGear.isBroken(weapon)) {
            return ItemStack.EMPTY;
        }
        UUID id = ensureWeaponId(player, weapon);
        int selected = id == null
                ? AmmoSlots.findDefault(player, weapon)
                : resolve(player, weapon, id, true);
        if (selected < 0 || !AmmoSlots.isValidAmmo(weapon, AmmoSlots.get(player, selected))) {
            return ItemStack.EMPTY;
        }

        ItemStack ammo = AmmoSlots.get(player, selected);
        ItemStack taken = ammo.copyWithCount(1);
        if (player.hasInfiniteMaterials()) {
            return taken;
        }

        if (AmmoSlots.isRigArrowSlot(selected)) {
            return AmmoSlots.consumeRigAmmo(player, selected, 1);
        }
        ammo.shrink(1);
        return taken;
    }

    public static void select(ServerPlayer player, ItemStack weapon, int candidate) {
        if (!AmmoSlots.isWeapon(weapon) || BrokenGear.isBroken(weapon)) {
            return;
        }
        UUID id = ensureWeaponId(player, weapon);
        if (id == null) {
            return;
        }
        int clamped = Math.max(0, Math.min(AmmoSlots.COUNT - 1, candidate));
        state(player).selections.put(id, clamped);
        sync(player, id, clamped);
    }

    public static void reset(ServerPlayer player, ItemStack weapon) {
        if (!AmmoSlots.isWeapon(weapon) || BrokenGear.isBroken(weapon) || player.hasInfiniteMaterials()) {
            return;
        }
        UUID id = ensureWeaponId(player, weapon);
        if (id == null) {
            return;
        }
        int selected = AmmoSlots.findDefault(player, weapon);
        state(player).selections.put(id, selected);
        sync(player, id, selected);
    }

    public static void tick(ServerPlayer player) {
        PlayerState state = state(player);
        Set<UUID> seen = new HashSet<>();

        // Hotbar, main inventory, armor slots and offhand. Armor cannot contain a weapon, so
        // scanning the whole inventory is harmless and catches modded inventory extensions.
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!AmmoSlots.isWeapon(stack)) {
                continue;
            }

            UUID id = ensureWeaponId(player, stack);
            if (id == null) {
                continue;
            }
            if (!seen.add(id)) {
                // A copied stack must not share another weapon's selection.
                id = UUID.randomUUID();
                stack.set(ModDataComponents.WEAPON_ID.get(), id);
                seen.add(id);
            }

            if (!state.selections.containsKey(id)) {
                int selected = AmmoSlots.findDefault(player, stack);
                state.selections.put(id, selected);
                sync(player, id, selected);
            }
        }

        // Rig weapons are not in the inventory, so scan their storage explicitly.
        // Without this, the selection of a virtually held rig weapon would be removed
        // every tick and the ammo choice would reset.
        for (int i = 0; i < ChestRig.weaponSlots(player); i++) {
            ItemStack stack = ChestRig.getWeapon(player, i);
            if (!AmmoSlots.isWeapon(stack)) {
                continue;
            }

            UUID before = stack.get(ModDataComponents.WEAPON_ID.get());
            UUID id = ensureWeaponId(player, stack);
            if (id == null) {
                continue;
            }

            boolean changed = before == null;
            if (!seen.add(id)) {
                id = UUID.randomUUID();
                stack.set(ModDataComponents.WEAPON_ID.get(), id);
                seen.add(id);
                changed = true;
            }
            if (changed) {
                ChestRig.setWeapon(player, i, stack);
            }

            if (!state.selections.containsKey(id)) {
                int selected = AmmoSlots.findDefault(player, stack);
                state.selections.put(id, selected);
                sync(player, id, selected);
            }
        }

        Iterator<Map.Entry<UUID, Integer>> iterator = state.selections.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            if (!seen.contains(entry.getKey())) {
                iterator.remove();
                sync(player, entry.getKey(), -1);
            }
        }
    }

    public static void copyState(Player from, Player to) {
        PlayerState oldState = STATES.get(PlayerKey.of(from));
        if (oldState == null) {
            return;
        }
        // Respawn and dimension travel keep the same UUID, so state(to) could be the
        // same object as oldState. Copy into a fresh state instead of clearing itself.
        PlayerState newState = new PlayerState();
        newState.selections.putAll(oldState.selections);
        STATES.put(PlayerKey.of(to), newState);
    }

    public static void clear(Player player) {
        STATES.remove(PlayerKey.of(player));
    }

    private static int resolve(Player player, ItemStack weapon, UUID id, boolean reselectIfInvalid) {
        PlayerState state = state(player);
        Integer existing = state.selections.get(id);
        if (existing == null) {
            int selected = AmmoSlots.findDefault(player, weapon);
            state.selections.put(id, selected);
            sync(player, id, selected);
            return selected;
        }

        if (reselectIfInvalid && !AmmoSlots.isValidAmmo(weapon, AmmoSlots.get(player, existing))) {
            int selected = AmmoSlots.findDefault(player, weapon);
            state.selections.put(id, selected);
            sync(player, id, selected);
            return selected;
        }
        return existing;
    }

    private static UUID ensureWeaponId(Player player, ItemStack weapon) {
        UUID id = weapon.get(ModDataComponents.WEAPON_ID.get());
        if (id != null) {
            return id;
        }
        if (!(player instanceof ServerPlayer)) {
            return null;
        }
        UUID created = UUID.randomUUID();
        weapon.set(ModDataComponents.WEAPON_ID.get(), created);
        return created;
    }

    private static PlayerState state(Player player) {
        return STATES.computeIfAbsent(PlayerKey.of(player), ignored -> new PlayerState());
    }

    private static void sync(Player player, UUID id, int selected) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetwork.syncSelection(serverPlayer, id, selected);
        }
    }

    private static final class PlayerState {
        private final Map<UUID, Integer> selections = new HashMap<>();
    }
}
package com.wlsnb.arrowslot.equipment;

import com.wlsnb.arrowslot.inventory.ChestRig;
import com.wlsnb.arrowslot.network.ModNetwork;
import com.wlsnb.arrowslot.network.RigWeaponActionPayload;
import com.wlsnb.arrowslot.registry.ModDataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * Server-side selection controller for the chest-rig weapon slots.
 *
 * <p>The selected weapon stays inside the chest rig. It is exposed as a virtual main-hand
 * stack through {@link RigWeaponState}, so the real hotbar slot keeps its original item.
 * This also means selecting a rig weapon never changes what the hotbar displays.</p>
 */
public final class RigWeaponController {
    private RigWeaponController() {
    }

    public static void handle(ServerPlayer player, int action, int value) {
        switch (action) {
            case RigWeaponActionPayload.SELECT_G -> {
                if (RigWeaponState.isActive(player)) {
                    if (RigWeaponState.isGMode(player)) {
                        restore(player);
                    }
                } else {
                    select(player, true);
                }
            }
            case RigWeaponActionPayload.SELECT_VANILLA -> {
                if (!RigWeaponState.isActive(player)) {
                    select(player, false);
                } else if (RigWeaponState.isGMode(player)) {
                    RigWeaponState.setGMode(player, false);
                    sync(player);
                }
            }
            case RigWeaponActionPayload.CYCLE -> {
                if (RigWeaponState.isGMode(player)) {
                    cycle(player, value >= 0 ? 1 : -1);
                }
            }
            case RigWeaponActionPayload.RESTORE -> {
                if (RigWeaponState.isActive(player)) {
                    restore(player);
                }
            }
            default -> {
            }
        }
    }

    public static void tick(ServerPlayer player) {
        if (!RigWeaponState.isActive(player)) {
            return;
        }
        if (player.isRemoved() || player.isSpectator() || ChestRig.getChestplate(player).isEmpty()) {
            RigWeaponState.deactivate(player);
            sync(player);
            return;
        }
        if (player.getInventory().selected != RigWeaponState.hotbarSlot(player)) {
            // Scroll or a hotbar key changed the vanilla slot: return to normal held items.
            restore(player);
            return;
        }
        if (RigWeaponState.index(player) >= ChestRig.weaponSlots(player)) {
            restore(player);
            return;
        }
        ItemStack stored = ChestRig.getWeapon(player, RigWeaponState.index(player));
        if (stored.isEmpty()) {
            // The player moved the weapon out of the rig while it was virtually held.
            RigWeaponState.deactivate(player);
            sync(player);
            return;
        }

        // AmmoSelection assigns a weapon id to rig weapons. Mirror it onto the virtual
        // stack so the client can resolve the same ammo-selection entry.
        ItemStack held = RigWeaponState.held(player);
        UUID storedId = stored.get(ModDataComponents.WEAPON_ID.get());
        if (!held.isEmpty() && storedId != null && held.get(ModDataComponents.WEAPON_ID.get()) == null) {
            held.set(ModDataComponents.WEAPON_ID.get(), storedId);
        }

        if (RigWeaponState.save(player)) {
            broadcast(player);
            sync(player);
        }
    }

    public static void restoreForTransition(ServerPlayer player) {
        if (RigWeaponState.isActive(player)) {
            restore(player);
        }
    }

    public static void clear(Player player) {
        RigWeaponState.clear(player);
    }

    public static int displayIndex(ServerPlayer player) {
        int index = RigWeaponState.index(player);
        if (index >= 0 && index < ChestRig.weaponSlots(player)) {
            return index;
        }
        return Math.max(0, ChestRig.firstWeaponIndex(player));
    }

    public static boolean isActive(ServerPlayer player) {
        return RigWeaponState.isActive(player);
    }

    public static boolean isGMode(ServerPlayer player) {
        return RigWeaponState.isGMode(player);
    }

    private static void select(ServerPlayer player, boolean gMode) {
        if (player.isSpectator()) {
            return;
        }

        int index = RigWeaponState.index(player);
        if (!isUsableWeapon(player, index)) {
            index = firstUsableWeapon(player);
        }
        if (index < 0) {
            return;
        }

        ItemStack rigWeapon = ChestRig.getWeapon(player, index);
        if (!isUsableWeapon(rigWeapon)) {
            return;
        }

        int hotbar = player.getInventory().selected;
        if (hotbar < 0 || hotbar > 8) {
            return;
        }

        // Do not touch the hotbar. The weapon stays in the rig and is exposed as the
        // virtual main-hand stack instead.
        RigWeaponState.activate(player, index, gMode, hotbar, rigWeapon.copy());
        sync(player);
    }

    private static void restore(ServerPlayer player) {
        if (!RigWeaponState.isActive(player)) {
            return;
        }

        boolean changed = RigWeaponState.save(player);
        RigWeaponState.deactivate(player);
        if (changed) {
            broadcast(player);
        }
        sync(player);
    }

    private static void cycle(ServerPlayer player, int direction) {
        if (!RigWeaponState.isActive(player) || !RigWeaponState.isGMode(player)) {
            return;
        }

        boolean changed = RigWeaponState.save(player);
        if (!RigWeaponState.isActive(player)) {
            if (changed) {
                broadcast(player);
            }
            sync(player);
            return;
        }

        int slots = ChestRig.weaponSlots(player);
        if (slots <= 1) {
            if (changed) {
                broadcast(player);
            }
            sync(player);
            return;
        }

        int current = RigWeaponState.index(player);
        int next = -1;
        for (int step = 1; step <= slots; step++) {
            int candidate = Math.floorMod(current + step * direction, slots);
            if (candidate != current && isUsableWeapon(player, candidate)) {
                next = candidate;
                break;
            }
        }
        if (next < 0) {
            if (changed) {
                broadcast(player);
            }
            sync(player);
            return;
        }

        ItemStack nextWeapon = ChestRig.getWeapon(player, next);
        RigWeaponState.setIndex(player, next);
        RigWeaponState.setHeld(player, nextWeapon.copy());
        if (changed) {
            broadcast(player);
        }
        sync(player);
    }

    private static int firstUsableWeapon(ServerPlayer player) {
        for (int i = 0; i < ChestRig.weaponSlots(player); i++) {
            if (isUsableWeapon(player, i)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isUsableWeapon(ServerPlayer player, int index) {
        return isUsableWeapon(ChestRig.getWeapon(player, index));
    }

    private static boolean isUsableWeapon(ItemStack stack) {
        return !stack.isEmpty() && !BrokenGear.isBroken(stack);
    }

    private static void broadcast(ServerPlayer player) {
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) {
            player.containerMenu.broadcastChanges();
        }
    }

    private static void sync(ServerPlayer player) {
        ModNetwork.syncRigWeapon(
                player,
                RigWeaponState.isActive(player),
                RigWeaponState.isGMode(player),
                Math.max(0, RigWeaponState.index(player)),
                RigWeaponState.held(player)
        );
    }
}

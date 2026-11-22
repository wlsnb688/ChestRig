package com.wlsnb.arrowslot.network;

import com.wlsnb.arrowslot.ammo.AmmoSelection;
import com.wlsnb.arrowslot.ammo.AmmoSlots;
import com.wlsnb.arrowslot.equipment.RigWeaponController;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(SelectAmmoPayload.TYPE, SelectAmmoPayload.STREAM_CODEC, ModNetwork::handleSelect);
        registrar.playToServer(RigWeaponActionPayload.TYPE, RigWeaponActionPayload.STREAM_CODEC, ModNetwork::handleRigWeapon);
        // The client-bound payload is registered in the client-only event subscriber so that
        // dedicated servers never need to resolve any net.minecraft.client class.
    }

    public static void syncSelection(ServerPlayer player, UUID weaponId, int candidate) {
        PacketDistributor.sendToPlayer(player, new SyncSelectionPayload(weaponId, candidate));
    }

    public static void syncRigWeapon(ServerPlayer player, boolean active, boolean gMode, int index, ItemStack stack) {
        PacketDistributor.sendToPlayer(player, new SyncRigWeaponPayload(active, gMode, index, stack));
    }

    private static void handleRigWeapon(RigWeaponActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                RigWeaponController.handle(player, payload.action(), payload.value());
            }
        });
    }

    private static void handleSelect(SelectAmmoPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ItemStack weapon = AmmoSlots.getHeldWeapon(player);
            if (!weapon.isEmpty()) {
                if (payload.candidate() < 0) {
                    AmmoSelection.reset(player, weapon);
                } else {
                    AmmoSelection.select(player, weapon, payload.candidate());
                }
            }
        });
    }
}

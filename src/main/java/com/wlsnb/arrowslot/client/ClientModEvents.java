package com.wlsnb.arrowslot.client;

import com.wlsnb.arrowslot.ArrowSlotMod;
import com.wlsnb.arrowslot.ammo.AmmoSelection;
import com.wlsnb.arrowslot.client.hud.HudOverlay;
import com.wlsnb.arrowslot.compat.ChestRigContentsTooltip;
import com.wlsnb.arrowslot.inventory.RigContentsTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import com.wlsnb.arrowslot.equipment.AutoEatVisualState;
import com.wlsnb.arrowslot.equipment.RigWeaponState;
import com.wlsnb.arrowslot.network.AutoEatVisualPayload;
import com.wlsnb.arrowslot.network.SyncRigWeaponPayload;
import com.wlsnb.arrowslot.network.SyncSelectionPayload;
import net.minecraft.world.entity.player.Player;

@EventBusSubscriber(modid = ArrowSlotMod.MOD_ID, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(
                SyncSelectionPayload.TYPE,
                SyncSelectionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientAmmoState.accept(payload))
        );
        registrar.playToClient(
                AutoEatVisualPayload.TYPE,
                AutoEatVisualPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    Player player = Minecraft.getInstance().player;
                    if (player == null) {
                        return;
                    }
                    if (payload.active()) {
                        AutoEatVisualState.start(player, payload.food(), payload.durationTicks());
                    } else {
                        AutoEatVisualState.stop(player);
                    }
                })
        );
        registrar.playToClient(
                SyncRigWeaponPayload.TYPE,
                SyncRigWeaponPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    RigWeaponClientState.accept(payload);
                    Player player = Minecraft.getInstance().player;
                    if (player != null) {
                        RigWeaponState.applyClient(
                                player,
                                payload.active(),
                                payload.gMode(),
                                payload.index(),
                                payload.stack()
                        );
                    }
                })
        );
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        // Opening a world also fires LoggingOut from Minecraft#disconnect, and the event
        // can carry a null player. Only clear per-player state when there is one.
        if (event.getPlayer() != null) {
            RigWeaponState.clear(event.getPlayer());
            AmmoSelection.clear(event.getPlayer());
            AutoEatVisualState.clear(event.getPlayer());
        }
        RigWeaponClientState.reset();
    }

    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(RigContentsTooltip.class, ChestRigContentsTooltip::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        KeyBindings.register(event);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, "ammo_hud"),
                AmmoHud::render
        );
        // Built-in HUD: inventory, vanilla-style effects, armor and hands/counters panels.
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, "inventory_hud"),
                (guiGraphics, deltaTracker) -> HudOverlay.render(guiGraphics)
        );
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, "rig_weapon_hud"),
                (guiGraphics, deltaTracker) -> RigWeaponHud.render(guiGraphics)
        );
    }
}
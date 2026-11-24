package com.wlsnb.arrowslot.network;

import com.wlsnb.arrowslot.ArrowSlotMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Dedicated servers only send these clientbound payloads. They still need to be registered
 * so the payload codecs are known when a packet is constructed; the handlers are never
 * invoked on a dedicated server because no clientbound payload is received there.
 */
@EventBusSubscriber(modid = ArrowSlotMod.MOD_ID, value = Dist.DEDICATED_SERVER)
public final class ServerPayloadRegistration {
    private ServerPayloadRegistration() {
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(
                SyncSelectionPayload.TYPE,
                SyncSelectionPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );
        registrar.playToClient(
                SyncRigWeaponPayload.TYPE,
                SyncRigWeaponPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );
        registrar.playToClient(
                AutoEatVisualPayload.TYPE,
                AutoEatVisualPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );
        registrar.playToClient(
                CrossbowLoadVisualPayload.TYPE,
                CrossbowLoadVisualPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );
    }
}

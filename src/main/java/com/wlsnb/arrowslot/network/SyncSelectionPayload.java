package com.wlsnb.arrowslot.network;

import com.wlsnb.arrowslot.ArrowSlotMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record SyncSelectionPayload(UUID weaponId, int candidate) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncSelectionPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, "sync_selection"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSelectionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    net.minecraft.core.UUIDUtil.STREAM_CODEC,
                    SyncSelectionPayload::weaponId,
                    ByteBufCodecs.VAR_INT,
                    SyncSelectionPayload::candidate,
                    SyncSelectionPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

package com.wlsnb.arrowslot.network;

import com.wlsnb.arrowslot.ArrowSlotMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SelectAmmoPayload(int candidate) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SelectAmmoPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, "select_ammo"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectAmmoPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    SelectAmmoPayload::candidate,
                    SelectAmmoPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

package com.wlsnb.arrowslot.network;

import com.wlsnb.arrowslot.ArrowSlotMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RigWeaponActionPayload(int action, int value) implements CustomPacketPayload {
    public static final int SELECT_G = 0;
    public static final int CYCLE = 2;
    public static final int RESTORE = 3;

    public static final CustomPacketPayload.Type<RigWeaponActionPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, "rig_weapon_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RigWeaponActionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    RigWeaponActionPayload::action,
                    ByteBufCodecs.VAR_INT,
                    RigWeaponActionPayload::value,
                    RigWeaponActionPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

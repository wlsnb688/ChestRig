package com.wlsnb.arrowslot.network;

import com.wlsnb.arrowslot.ArrowSlotMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record SyncRigWeaponPayload(boolean active, boolean gMode, int index, ItemStack stack)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncRigWeaponPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, "sync_rig_weapon"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncRigWeaponPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    SyncRigWeaponPayload::active,
                    ByteBufCodecs.BOOL,
                    SyncRigWeaponPayload::gMode,
                    ByteBufCodecs.VAR_INT,
                    SyncRigWeaponPayload::index,
                    ItemStack.OPTIONAL_STREAM_CODEC,
                    SyncRigWeaponPayload::stack,
                    SyncRigWeaponPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

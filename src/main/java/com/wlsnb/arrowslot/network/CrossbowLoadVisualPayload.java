package com.wlsnb.arrowslot.network;

import com.wlsnb.arrowslot.ArrowSlotMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record CrossbowLoadVisualPayload(boolean active, ItemStack weapon, int durationTicks)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CrossbowLoadVisualPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, "crossbow_load_visual"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrossbowLoadVisualPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    CrossbowLoadVisualPayload::active,
                    ItemStack.OPTIONAL_STREAM_CODEC,
                    CrossbowLoadVisualPayload::weapon,
                    ByteBufCodecs.VAR_INT,
                    CrossbowLoadVisualPayload::durationTicks,
                    CrossbowLoadVisualPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

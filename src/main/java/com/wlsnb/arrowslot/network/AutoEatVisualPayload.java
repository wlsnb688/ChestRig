package com.wlsnb.arrowslot.network;

import com.wlsnb.arrowslot.ArrowSlotMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record AutoEatVisualPayload(boolean active, ItemStack food, int durationTicks)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AutoEatVisualPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, "auto_eat_visual"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AutoEatVisualPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    AutoEatVisualPayload::active,
                    ItemStack.OPTIONAL_STREAM_CODEC,
                    AutoEatVisualPayload::food,
                    ByteBufCodecs.VAR_INT,
                    AutoEatVisualPayload::durationTicks,
                    AutoEatVisualPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

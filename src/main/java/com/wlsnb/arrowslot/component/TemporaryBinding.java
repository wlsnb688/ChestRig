package com.wlsnb.arrowslot.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Tracks a temporary binding curse applied after a keepInventory respawn.
 *
 * @param expiresAt   game time at which the temporary curse ends
 * @param addedCurse  whether this mod added the curse and should remove it again
 */
public record TemporaryBinding(long expiresAt, boolean addedCurse) {
    public static final Codec<TemporaryBinding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("expires_at").forGetter(TemporaryBinding::expiresAt),
            Codec.BOOL.fieldOf("added_curse").forGetter(TemporaryBinding::addedCurse)
    ).apply(instance, TemporaryBinding::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TemporaryBinding> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_LONG,
                    TemporaryBinding::expiresAt,
                    ByteBufCodecs.BOOL,
                    TemporaryBinding::addedCurse,
                    TemporaryBinding::new
            );
}

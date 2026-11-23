package com.wlsnb.arrowslot.util;

import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Key for per-player transient state.
 *
 * <p>{@link net.minecraft.world.entity.Entity#equals(Object)} compares entity ids, and the
 * integrated server and client usually give their player the same id. A plain
 * {@code Map<Player, ...>} therefore mixes the two logical sides. This key keeps the UUID
 * but also separates the client and server player instances.</p>
 */
public record PlayerKey(UUID uuid, boolean clientSide) {
    public static PlayerKey of(Player player) {
        return new PlayerKey(
                player.getUUID(),
                player.level() != null && player.level().isClientSide()
        );
    }
}
package com.wlsnb.arrowslot.equipment;

import com.wlsnb.arrowslot.util.PlayerKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-only visual state for auto-eating. The server owns the actual food consumption;
 * these values only make the local player perform the vanilla eat animation and slowdown.
 */
public final class AutoEatVisualState {
    private static final Map<PlayerKey, Visual> STATES = new ConcurrentHashMap<>();

    private AutoEatVisualState() {
    }

    public static void start(Player player, ItemStack food, int durationTicks) {
        if (food.isEmpty() || durationTicks <= 0) {
            stop(player);
            return;
        }
        Visual visual = new Visual();
        visual.food = food.copy();
        visual.remaining = durationTicks;
        STATES.put(PlayerKey.of(player), visual);
    }

    public static void stop(Player player) {
        STATES.remove(PlayerKey.of(player));
    }

    public static void clear(Player player) {
        stop(player);
    }

    public static boolean isActive(Player player) {
        Visual visual = STATES.get(PlayerKey.of(player));
        return visual != null && visual.remaining > 0 && !visual.food.isEmpty();
    }

    public static ItemStack food(Player player) {
        Visual visual = STATES.get(PlayerKey.of(player));
        return visual == null ? ItemStack.EMPTY : visual.food;
    }

    public static int remaining(Player player) {
        Visual visual = STATES.get(PlayerKey.of(player));
        return visual == null ? 0 : Math.max(0, visual.remaining);
    }

    /** Advances the client animation countdown once per client tick. */
    public static void tick(Player player) {
        PlayerKey key = PlayerKey.of(player);
        Visual visual = STATES.get(key);
        if (visual == null) {
            return;
        }
        visual.remaining--;
        if (visual.remaining <= 0) {
            STATES.remove(key, visual);
        }
    }

    private static final class Visual {
        private ItemStack food = ItemStack.EMPTY;
        private int remaining;
    }
}

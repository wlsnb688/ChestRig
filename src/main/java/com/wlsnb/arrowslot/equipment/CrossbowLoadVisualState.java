package com.wlsnb.arrowslot.equipment;

import com.wlsnb.arrowslot.util.PlayerKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-only fake crossbow-charge animation. The server owns the timed reload session;
 * this state only makes the local player show the vanilla crossbow pose without starting
 * a real item use that an attack could cancel.
 */
public final class CrossbowLoadVisualState {
    private static final Map<PlayerKey, Visual> STATES = new ConcurrentHashMap<>();

    private CrossbowLoadVisualState() {
    }

    public static void start(Player player, ItemStack weapon, int durationTicks) {
        if (weapon.isEmpty() || durationTicks <= 0) {
            stop(player);
            return;
        }
        Visual visual = new Visual();
        visual.weapon = weapon.copy();
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
        return visual != null && visual.remaining > 0 && !visual.weapon.isEmpty();
    }

    public static ItemStack weapon(Player player) {
        Visual visual = STATES.get(PlayerKey.of(player));
        return visual == null ? ItemStack.EMPTY : visual.weapon;
    }

    public static int remaining(Player player) {
        Visual visual = STATES.get(PlayerKey.of(player));
        return visual == null ? 0 : Math.max(0, visual.remaining);
    }

    public static InteractionHand hand(Player player) {
        return InteractionHand.MAIN_HAND;
    }

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
        private ItemStack weapon = ItemStack.EMPTY;
        private int remaining;
    }
}

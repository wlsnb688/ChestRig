package com.wlsnb.arrowslot.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;

import java.lang.reflect.Method;

/**
 * Optional AppleSkin bridge. Reflection keeps AppleSkin classes out of the mod's
 * verification path, so the mod still loads when AppleSkin is absent.
 */
public final class AppleSkinFoodCompat {
    private static Method estimatedHealthMethod;
    private static boolean resolved;

    private AppleSkinFoodCompat() {
    }

    /**
     * AppleSkin's estimated health restore for eating this food in the player's current state.
     */
    public static float estimatedHealthIncrement(Player player, FoodProperties food) {
        Method method = resolve();
        if (method == null) {
            return 0.0F;
        }
        try {
            return ((Number) method.invoke(null, player, food)).floatValue();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return 0.0F;
        }
    }

    private static synchronized Method resolve() {
        if (resolved) {
            return estimatedHealthMethod;
        }
        resolved = true;
        try {
            Class<?> foodHelper = Class.forName("squeek.appleskin.helpers.FoodHelper");
            estimatedHealthMethod = foodHelper.getMethod(
                    "getEstimatedHealthIncrement", Player.class, FoodProperties.class);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            estimatedHealthMethod = null;
        }
        return estimatedHealthMethod;
    }
}

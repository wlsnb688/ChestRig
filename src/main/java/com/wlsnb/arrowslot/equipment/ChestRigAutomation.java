package com.wlsnb.arrowslot.equipment;

import com.wlsnb.arrowslot.compat.AppleSkinFoodCompat;
import com.wlsnb.arrowslot.inventory.ChestRig;
import com.wlsnb.arrowslot.network.ModNetwork;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side auto-eating from the chest rig's food slots.
 *
 * <p>The server owns the food consumption and cooldowns. A lightweight client visual state
 * makes the local player perform the vanilla eating animation and movement slowdown while
 * the server waits for the same use duration.</p>
 */
public final class ChestRigAutomation {
    private static final Map<UUID, EatState> EATING = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> NEXT_EAT = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> GOLDEN_LOCK = new ConcurrentHashMap<>();
    private static final Set<UUID> FEEDING = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> HEALING = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> NORMAL_EAT_REFUND = ConcurrentHashMap.newKeySet();

    private static final int EAT_COOLDOWN_TICKS = 100;
    private static final int GOLDEN_APPLE_LOCK_TICKS = 600;
    private static final int ENCHANTED_GOLDEN_APPLE_LOCK_TICKS = 1200;

    private ChestRigAutomation() {
    }

    public static void tick(ServerPlayer player) {
        UUID id = player.getUUID();
        EatState eating = EATING.get(id);
        if (eating != null) {
            tickEating(player, id, eating);
            return;
        }

        if (player.isCreative() || player.isSpectator() || player.isDeadOrDying() || player.isUsingItem()) {
            return;
        }

        FoodData foodData = player.getFoodData();
        // Start the healing session before the low-hunger branch. Otherwise the first
        // eaten food can raise health above three hearts, and the session would never start.
        if (player.getHealth() <= 6.0F && ModList.get().isLoaded("appleskin")) {
            HEALING.add(id);
        }
        if (foodData.getFoodLevel() <= 6) {
            FEEDING.add(id);
        }
        if (FEEDING.contains(id)) {
            if (foodData.getFoodLevel() >= 10) {
                FEEDING.remove(id);
            } else {
                tryStartEating(player, id);
                return;
            }
        }

        // Keep the session alive after health rises above three hearts; it ends when actual
        // health is full, hunger is full, or the eaten food is predicted to fill health.
        if (!HEALING.contains(id)) {
            return;
        }
        if (player.getHealth() >= player.getMaxHealth() || foodData.getFoodLevel() >= 20) {
            HEALING.remove(id);
            return;
        }

        tryStartEating(player, id);
    }

    /**
     * A hit during the post-eat cooldown grants one immediate auto-eat attempt for
     * non-golden food. Golden apples keep their own lock and are skipped by that attempt.
     */
    public static void onDamaged(ServerPlayer player) {
        UUID id = player.getUUID();
        if (player.tickCount < NEXT_EAT.getOrDefault(id, 0)) {
            NORMAL_EAT_REFUND.add(id);
        }
    }

    public static void cancel(ServerPlayer player) {
        cancel(player, player.getUUID());
    }

    public static void clear(ServerPlayer player) {
        UUID id = player.getUUID();
        // Do not send a packet while the connection is closing.
        EATING.remove(id);
        NEXT_EAT.remove(id);
        GOLDEN_LOCK.remove(id);
        FEEDING.remove(id);
        HEALING.remove(id);
        NORMAL_EAT_REFUND.remove(id);
    }

    /**
     * Clears the transient per-life state. Tick counters restart on the respawned player,
     * so keeping the old cooldown timestamps would suppress auto-eating after death.
     */
    public static void resetAfterDeath(ServerPlayer player) {
        cancel(player);
        clear(player);
    }

    private static void tickEating(ServerPlayer player, UUID id, EatState state) {
        if (player.isRemoved() || player.isDeadOrDying() || player.isSpectator()
                || ChestRig.getChestplate(player).isEmpty()) {
            cancel(player, id);
            return;
        }
        if (player.isUsingItem()) {
            // A manual use action takes priority over the scripted eat.
            cancel(player, id);
            return;
        }
        if (player.tickCount - state.startTick >= state.duration) {
            complete(player, id, state);
            return;
        }
        if (player.tickCount % 8 == 0) {
            playEatSound(player);
        }
    }

    private static void tryStartEating(ServerPlayer player, UUID id) {
        if (player.tickCount >= NEXT_EAT.getOrDefault(id, 0)) {
            NORMAL_EAT_REFUND.remove(id);
            startEating(player, id, true);
            return;
        }
        if (NORMAL_EAT_REFUND.contains(id) && startEating(player, id, false)) {
            NORMAL_EAT_REFUND.remove(id);
        }
    }

    private static boolean startEating(ServerPlayer player, UUID id, boolean allowGolden) {
        int storage = findBestFoodSlot(player, id, allowGolden);
        if (storage < 0) {
            return false;
        }
        ItemStack stack = ChestRig.get(ChestRig.getChestplate(player), storage);
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null) {
            return false;
        }

        int duration = Math.max(1, stack.getItem().getUseDuration(stack, player));
        float healthAtStart = player.getHealth();
        boolean finishesHealing = HEALING.contains(id)
                && ModList.get().isLoaded("appleskin")
                && healthAtStart + AppleSkinFoodCompat.estimatedHealthIncrement(player, food) >= player.getMaxHealth();
        EatState state = new EatState(storage, stack.copyWithCount(1), player.tickCount, duration,
                healthAtStart, finishesHealing);
        EATING.put(id, state);
        ModNetwork.syncAutoEat(player, true, state.food, duration);
        playEatSound(player);
        return true;
    }

    private static void complete(ServerPlayer player, UUID id, EatState state) {
        FoodProperties food = state.food.get(DataComponents.FOOD);
        int storage = findMatchingFoodSlot(player, state.food);
        if (food == null || storage < 0) {
            cancel(player, id);
            return;
        }

        ItemStack current = ChestRig.get(ChestRig.getChestplate(player), storage);
        ItemStack remaining = current.copy();
        remaining.shrink(1);
        ChestRig.set(ChestRig.getChestplate(player), storage, remaining);

        ItemStack leftover = state.food.copyWithCount(1).finishUsingItem(player.level(), player);
        if (!leftover.isEmpty() && !player.getInventory().add(leftover)) {
            player.drop(leftover, false);
        }

        player.inventoryMenu.broadcastChanges();
        NEXT_EAT.put(id, player.tickCount + EAT_COOLDOWN_TICKS);
        if (state.food.is(Items.GOLDEN_APPLE)) {
            GOLDEN_LOCK.put(id, player.tickCount + GOLDEN_APPLE_LOCK_TICKS);
        } else if (state.food.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            GOLDEN_LOCK.put(id, player.tickCount + ENCHANTED_GOLDEN_APPLE_LOCK_TICKS);
        }

        if (state.finishesHealing && player.getHealth() >= state.healthAtStart) {
            HEALING.remove(id);
        }

        EATING.remove(id);
        ModNetwork.syncAutoEat(player, false, ItemStack.EMPTY, 0);
    }

    private static void cancel(ServerPlayer player, UUID id) {
        EatState removed = EATING.remove(id);
        if (removed != null) {
            ModNetwork.syncAutoEat(player, false, ItemStack.EMPTY, 0);
        }
    }

    /**
     * Chooses the best food instead of the first occupied slot. During low-health
     * recovery AppleSkin's estimated healing is the primary key; otherwise nutrition and
     * saturation are used. Slot order is the final tie-breaker.
     */
    private static int findBestFoodSlot(ServerPlayer player, UUID id, boolean allowGolden) {
        int lockUntil = GOLDEN_LOCK.getOrDefault(id, 0);
        boolean healingPriority = HEALING.contains(id) && ModList.get().isLoaded("appleskin");
        int bestStorage = -1;
        float bestHealing = Float.NEGATIVE_INFINITY;
        int bestNutrition = Integer.MIN_VALUE;
        float bestSaturation = Float.NEGATIVE_INFINITY;

        for (int i = 0; i < foodSlots(player); i++) {
            int storage = ChestRig.FOOD_START + i;
            ItemStack stack = ChestRig.get(ChestRig.getChestplate(player), storage);
            if (!stack.has(DataComponents.FOOD)) {
                continue;
            }
            if (!allowGolden && isGoldenApple(stack)) {
                continue;
            }
            if (isGoldenApple(stack) && player.tickCount < lockUntil) {
                continue;
            }

            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food == null) {
                continue;
            }
            if (player.getCooldowns().isOnCooldown(stack.getItem())
                    || !player.canEat(food.canAlwaysEat())) {
                continue;
            }
            float healing = healingPriority
                    ? AppleSkinFoodCompat.estimatedHealthIncrement(player, food)
                    : 0.0F;
            if (bestStorage < 0
                    || healing > bestHealing
                    || (healing == bestHealing && food.nutrition() > bestNutrition)
                    || (healing == bestHealing && food.nutrition() == bestNutrition
                    && food.saturation() > bestSaturation)) {
                bestStorage = storage;
                bestHealing = healing;
                bestNutrition = food.nutrition();
                bestSaturation = food.saturation();
            }
        }
        return bestStorage;
    }

    private static int findMatchingFoodSlot(ServerPlayer player, ItemStack food) {
        for (int i = 0; i < foodSlots(player); i++) {
            int storage = ChestRig.FOOD_START + i;
            ItemStack stack = ChestRig.get(ChestRig.getChestplate(player), storage);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, food)) {
                return storage;
            }
        }
        return -1;
    }

    private static boolean isGoldenApple(ItemStack stack) {
        return stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE);
    }

    private static void playEatSound(ServerPlayer player) {
        player.level().playSound(
                null,
                player,
                SoundEvents.GENERIC_EAT,
                SoundSource.PLAYERS,
                0.5F,
                0.8F + player.getRandom().nextFloat() * 0.4F
        );
    }

    private static int foodSlots(ServerPlayer player) {
        return ChestRig.material(ChestRig.getChestplate(player)).foodSlots();
    }

    private record EatState(int storage, ItemStack food, int startTick, int duration,
                            float healthAtStart, boolean finishesHealing) {
    }
}

package com.wlsnb.arrowslot.equipment;

import com.wlsnb.arrowslot.inventory.ChestRig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Server-side auto-eating from the chest rig's food slots.
 *
 * <p>Low hunger always works. Low health is intentionally gated behind AppleSkin so the
 * mod does not invent a healing estimate when that optional mod is absent.</p>
 */
public final class ChestRigAutomation {
    private static final Set<UUID> HEALING = new HashSet<>();

    private ChestRigAutomation() {
    }

    public static void tick(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator() || player.isDeadOrDying() || player.isUsingItem()) {
            return;
        }

        FoodData foodData = player.getFoodData();
        boolean lowHunger = foodData.getFoodLevel() < 6;
        if (lowHunger) {
            int guard = 0;
            while (foodData.getFoodLevel() <= 10 && guard++ < 10 && eatOne(player)) {
                // Keep eating until the bar is above five icons, or the rig runs out.
            }
            return;
        }

        UUID id = player.getUUID();
        if (player.getHealth() < 6.0F && ModList.get().isLoaded("appleskin")) {
            HEALING.add(id);
        }
        if (!HEALING.contains(id)) {
            return;
        }
        if (player.getHealth() >= player.getMaxHealth() || foodData.getFoodLevel() >= 20) {
            HEALING.remove(id);
            return;
        }

        if (player.tickCount % 20 == 0) {
            eatOne(player);
        }
    }

    private static boolean eatOne(ServerPlayer player) {
        for (int i = 0; i < foodSlots(player); i++) {
            int storage = ChestRig.FOOD_START + i;
            ItemStack stack = ChestRig.get(ChestRig.getChestplate(player), storage);
            if (stack.isEmpty()) {
                continue;
            }

            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food == null) {
                continue;
            }

            ItemStack one = stack.copyWithCount(1);
            ItemStack remainder = player.eat(player.level(), one, food);
            ItemStack remaining = stack.copy();
            remaining.shrink(1);
            ChestRig.set(ChestRig.getChestplate(player), storage, remaining);

            if (!remainder.isEmpty()) {
                player.getInventory().add(remainder);
            }
            return true;
        }
        return false;
    }

    private static int foodSlots(ServerPlayer player) {
        return ChestRig.material(ChestRig.getChestplate(player)).foodSlots();
    }
}

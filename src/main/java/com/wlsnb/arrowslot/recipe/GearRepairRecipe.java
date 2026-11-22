package com.wlsnb.arrowslot.recipe;

import com.wlsnb.arrowslot.equipment.BrokenGear;
import com.wlsnb.arrowslot.registry.ModRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets two identical pieces of gear be combined in a crafting table to repair a
 * zero-durability item. The result keeps the first item's components (name, enchantments,
 * chest-rig contents, and so on) and has half of the summed remaining durability,
 * rounded up.
 */
public final class GearRepairRecipe extends CustomRecipe {
    public GearRepairRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        List<ItemStack> items = new ArrayList<>(2);
        for (ItemStack stack : input.items()) {
            if (!stack.isEmpty()) {
                items.add(stack);
            }
        }

        if (items.size() != 2) {
            return false;
        }

        ItemStack first = items.get(0);
        ItemStack second = items.get(1);
        if (!ItemStack.isSameItem(first, second) || !BrokenGear.isTrackedGear(first)) {
            return false;
        }
        if (!BrokenGear.isBroken(first) && !BrokenGear.isBroken(second)) {
            return false;
        }

        return remainingDurability(first) + remainingDurability(second) > 0;
    }

    @Override
    public ItemStack assemble(CraftingInput input, net.minecraft.core.HolderLookup.Provider registries) {
        ItemStack first = ItemStack.EMPTY;
        ItemStack second = ItemStack.EMPTY;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (first.isEmpty()) {
                first = stack;
            } else if (second.isEmpty()) {
                second = stack;
            }
        }

        if (first.isEmpty() || second.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int maxDamage = first.getMaxDamage();
        int totalRemaining = remainingDurability(first) + remainingDurability(second);
        int repairedDurability = (int) Math.ceil(totalRemaining / 2.0D);
        repairedDurability = Math.max(1, Math.min(maxDamage, repairedDurability));

        ItemStack result = first.copy();
        result.setCount(1);
        result.setDamageValue(maxDamage - repairedDurability);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeSerializer<?> getSerializer() {
        return ModRecipes.GEAR_REPAIR.get();
    }

    private static int remainingDurability(ItemStack stack) {
        return Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
    }
}

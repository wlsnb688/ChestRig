package com.wlsnb.arrowslot.loot;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Shared tool checks for the leaf-drop rules.
 */
public final class LeafDropRules {
    private LeafDropRules() {
    }

    /**
     * Returns whether this tool must keep vanilla leaf drops: shears or any tool with
     * Silk Touch. An empty stack (hand or non-tool source) is not excluded.
     */
    public static boolean keepsVanillaLeafDrops(ItemStack tool) {
        if (tool == null || tool.isEmpty()) {
            return false;
        }
        if (tool.is(Items.SHEARS)) {
            return true;
        }

        ItemEnchantments enchantments = tool.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            if (enchantment.unwrapKey().filter(key -> key.equals(Enchantments.SILK_TOUCH)).isPresent()) {
                return true;
            }
        }
        return false;
    }
}

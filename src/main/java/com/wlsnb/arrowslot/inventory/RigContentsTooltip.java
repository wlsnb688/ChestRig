package com.wlsnb.arrowslot.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/**
 * Tooltip payload carrying a chestplate's storage contents. The client renders it as a
 * bundle-style grid of slots.
 */
public record RigContentsTooltip(NonNullList<ItemStack> items) implements TooltipComponent {
    public static RigContentsTooltip of(ItemStack chestplate) {
        return new RigContentsTooltip(ChestRig.copyContents(chestplate));
    }

    public static boolean hasContents(ItemStack chestplate) {
        return ChestRigMaterial.of(chestplate) != ChestRigMaterial.NONE && !ChestRig.previewIsEmpty(chestplate);
    }
}

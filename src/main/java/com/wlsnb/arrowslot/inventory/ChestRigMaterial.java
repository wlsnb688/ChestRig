package com.wlsnb.arrowslot.inventory;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;

/**
 * Vanilla chestplate materials and the number of chest-rig slots each one unlocks.
 *
 * <p>The storage array is deliberately fixed at eleven category slots. The row shown in
 * the inventory is the active subset, so upgrading a chestplate can reveal more slots
 * without moving or discarding the existing contents.</p>
 */
public enum ChestRigMaterial {
    NONE(0, 0, 0, 0),
    LEATHER(1, 1, 0, 0),
    CHAIN(1, 2, 1, 0),
    IRON(2, 3, 1, 0),
    GOLD(2, 3, 4, 0),
    DIAMOND(2, 4, 3, 0),
    NETHERITE(2, 4, 2, 1);

    private final int weaponSlots;
    private final int arrowSlots;
    private final int foodSlots;
    private final int universalSlots;

    ChestRigMaterial(int weaponSlots, int arrowSlots, int foodSlots, int universalSlots) {
        this.weaponSlots = weaponSlots;
        this.arrowSlots = arrowSlots;
        this.foodSlots = foodSlots;
        this.universalSlots = universalSlots;
    }

    public int weaponSlots() {
        return weaponSlots;
    }

    public int arrowSlots() {
        return arrowSlots;
    }

    public int foodSlots() {
        return foodSlots;
    }

    public int universalSlots() {
        return universalSlots;
    }

    public int totalSlots() {
        return weaponSlots + arrowSlots + foodSlots + universalSlots;
    }

    public static ChestRigMaterial of(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem armor) || armor.getType() != ArmorItem.Type.CHESTPLATE) {
            return NONE;
        }

        Holder<ArmorMaterial> material = armor.getMaterial();
        if (material.value() == ArmorMaterials.LEATHER.value()) {
            return LEATHER;
        }
        if (material.value() == ArmorMaterials.CHAIN.value()) {
            return CHAIN;
        }
        if (material.value() == ArmorMaterials.IRON.value()) {
            return IRON;
        }
        if (material.value() == ArmorMaterials.GOLD.value()) {
            return GOLD;
        }
        if (material.value() == ArmorMaterials.DIAMOND.value()) {
            return DIAMOND;
        }
        if (material.value() == ArmorMaterials.NETHERITE.value()) {
            return NETHERITE;
        }
        return NONE;
    }
}

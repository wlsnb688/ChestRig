package com.wlsnb.arrowslot.inventory;

import com.mojang.datafixers.util.Pair;
import com.wlsnb.arrowslot.ArrowSlotMod;
import com.wlsnb.arrowslot.ammo.AmmoSlots;
import com.wlsnb.arrowslot.registry.ModDataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;

/**
 * Fixed storage and player-facing helpers for the chestplate-mounted chest rig.
 *
 * <p>Storage is split into category ranges so a slot keeps its meaning when the player
 * changes to another chestplate material. The inventory row only exposes the active
 * subset for the currently equipped chestplate.</p>
 */
public final class ChestRig {
    public static final int WEAPON_START = 0;
    public static final int WEAPON_CAPACITY = 2;
    public static final int ARROW_START = WEAPON_START + WEAPON_CAPACITY;
    public static final int ARROW_CAPACITY = 4;
    public static final int FOOD_START = ARROW_START + ARROW_CAPACITY;
    public static final int FOOD_CAPACITY = 4;
    public static final int UNIVERSAL_SLOT = FOOD_START + FOOD_CAPACITY;
    public static final int STORAGE_SIZE = UNIVERSAL_SLOT + 1;

    /** The inventory row is nine vanilla slots wide at most. */
    public static final int DISPLAY_SLOTS = 9;

    private static final ResourceLocation EMPTY_SWORD =
            ResourceLocation.withDefaultNamespace("item/empty_slot_sword");
    private static final ResourceLocation EMPTY_ARROW =
            ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, "item/empty_slot_arrow");
    private static final ResourceLocation EMPTY_BREAD =
            ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, "item/empty_slot_bread");

    private ChestRig() {
    }

    public static ItemStack getChestplate(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return ChestRigMaterial.of(chest) == ChestRigMaterial.NONE ? ItemStack.EMPTY : chest;
    }

    public static ChestRigMaterial material(ItemStack chestplate) {
        return ChestRigMaterial.of(chestplate);
    }

    public static ItemStack getArrow(Player player, int arrowIndex) {
        ItemStack chestplate = getChestplate(player);
        if (arrowIndex < 0 || arrowIndex >= ChestRigMaterial.of(chestplate).arrowSlots()) {
            return ItemStack.EMPTY;
        }
        return get(chestplate, ARROW_START + arrowIndex);
    }

    public static int weaponSlots(Player player) {
        return ChestRigMaterial.of(getChestplate(player)).weaponSlots();
    }

    public static ItemStack getWeapon(Player player, int weaponIndex) {
        if (weaponIndex < 0 || weaponIndex >= weaponSlots(player)) {
            return ItemStack.EMPTY;
        }
        return get(getChestplate(player), WEAPON_START + weaponIndex);
    }

    public static void setWeapon(Player player, int weaponIndex, ItemStack stack) {
        if (weaponIndex >= 0 && weaponIndex < weaponSlots(player)) {
            set(getChestplate(player), WEAPON_START + weaponIndex, stack);
        }
    }

    public static int firstWeaponIndex(Player player) {
        for (int i = 0; i < weaponSlots(player); i++) {
            if (!getWeapon(player, i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public static int insertArrows(Player player, ItemStack stack, boolean allowEmpty) {
        ItemStack chestplate = getChestplate(player);
        int moved = 0;
        for (int i = 0; i < ChestRigMaterial.of(chestplate).arrowSlots() && !stack.isEmpty(); i++) {
            int storage = ARROW_START + i;
            ItemStack current = get(chestplate, storage);
            if (current.isEmpty() && !allowEmpty) {
                continue;
            }
            if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) {
                continue;
            }

            int limit = Math.min(64, stack.getMaxStackSize());
            int amount = Math.min(limit - current.getCount(), stack.getCount());
            if (amount <= 0) {
                continue;
            }

            ItemStack merged = current.isEmpty() ? stack.copyWithCount(amount) : current.copy();
            if (!current.isEmpty()) {
                merged.grow(amount);
            }
            set(chestplate, storage, merged);
            stack.shrink(amount);
            moved += amount;
        }
        return moved;
    }

    public static int insertFood(Player player, ItemStack stack, boolean allowEmpty) {
        ItemStack chestplate = getChestplate(player);
        if (!stack.has(DataComponents.FOOD)) {
            return 0;
        }

        int moved = 0;
        for (int i = 0; i < ChestRigMaterial.of(chestplate).foodSlots() && !stack.isEmpty(); i++) {
            int storage = FOOD_START + i;
            ItemStack current = get(chestplate, storage);
            if (current.isEmpty() && !allowEmpty) {
                continue;
            }
            if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) {
                continue;
            }

            int limit = Math.min(64, stack.getMaxStackSize());
            int amount = Math.min(limit - current.getCount(), stack.getCount());
            if (amount <= 0) {
                continue;
            }

            ItemStack merged = current.isEmpty() ? stack.copyWithCount(amount) : current.copy();
            if (!current.isEmpty()) {
                merged.grow(amount);
            }
            set(chestplate, storage, merged);
            stack.shrink(amount);
            moved += amount;
        }
        return moved;
    }

    public static boolean insertWeapon(Player player, ItemStack stack) {
        for (int i = 0; i < weaponSlots(player); i++) {
            if (getWeapon(player, i).isEmpty()) {
                setWeapon(player, i, stack.copy());
                stack.setCount(0);
                return true;
            }
        }
        return false;
    }

    public static int insertUniversal(Player player, ItemStack stack) {
        ItemStack chestplate = getChestplate(player);
        if (ChestRigMaterial.of(chestplate).universalSlots() <= 0) {
            return 0;
        }

        ItemStack current = get(chestplate, UNIVERSAL_SLOT);
        if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) {
            return 0;
        }

        int limit = Math.min(64, stack.getMaxStackSize());
        int amount = Math.min(limit - current.getCount(), stack.getCount());
        if (amount <= 0) {
            return 0;
        }

        ItemStack merged = current.isEmpty() ? stack.copyWithCount(amount) : current.copy();
        if (!current.isEmpty()) {
            merged.grow(amount);
        }
        set(chestplate, UNIVERSAL_SLOT, merged);
        stack.shrink(amount);
        return amount;
    }

    public static int totalArrows(Player player) {
        return totalArrows(getChestplate(player));
    }

    public static int totalArrows(ItemStack chestplate) {
        int total = 0;
        for (int i = 0; i < ChestRigMaterial.of(chestplate).arrowSlots(); i++) {
            total += get(chestplate, ARROW_START + i).getCount();
        }
        return total;
    }

    public static int totalFoodItems(Player player) {
        return totalFoodItems(getChestplate(player));
    }

    public static int totalFoodItems(ItemStack chestplate) {
        int total = 0;
        for (int i = 0; i < ChestRigMaterial.of(chestplate).foodSlots(); i++) {
            total += get(chestplate, FOOD_START + i).getCount();
        }
        return total;
    }

    public static int totalFoodNutrition(Player player) {
        return totalFoodNutrition(getChestplate(player));
    }

    public static int totalFoodNutrition(ItemStack chestplate) {
        int total = 0;
        for (int i = 0; i < ChestRigMaterial.of(chestplate).foodSlots(); i++) {
            ItemStack stack = get(chestplate, FOOD_START + i);
            if (!stack.isEmpty()) {
                var food = stack.get(DataComponents.FOOD);
                if (food != null) {
                    total += food.nutrition() * stack.getCount();
                }
            }
        }
        return total;
    }

    public static List<Integer> visibleStorageSlots(ItemStack chestplate) {
        ChestRigMaterial material = ChestRigMaterial.of(chestplate);
        List<Integer> slots = new ArrayList<>(material.totalSlots());
        for (int i = 0; i < material.weaponSlots(); i++) {
            slots.add(WEAPON_START + i);
        }
        for (int i = 0; i < material.arrowSlots(); i++) {
            slots.add(ARROW_START + i);
        }
        for (int i = 0; i < material.foodSlots(); i++) {
            slots.add(FOOD_START + i);
        }
        if (material.universalSlots() > 0) {
            slots.add(UNIVERSAL_SLOT);
        }
        return slots;
    }

    public static int storageIndex(ItemStack chestplate, int displayIndex) {
        List<Integer> slots = visibleStorageSlots(chestplate);
        return displayIndex >= 0 && displayIndex < slots.size() ? slots.get(displayIndex) : -1;
    }

    public static boolean isActive(ItemStack chestplate, int displayIndex) {
        return storageIndex(chestplate, displayIndex) >= 0;
    }

    public static ItemStack get(ItemStack chestplate, int storageIndex) {
        if (storageIndex < 0 || storageIndex >= STORAGE_SIZE || chestplate.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemContainerContents contents = chestplate.get(ModDataComponents.RIG_CONTENTS.get());
        if (contents == null || storageIndex >= contents.getSlots()) {
            return ItemStack.EMPTY;
        }
        return contents.getStackInSlot(storageIndex);
    }

    public static void set(ItemStack chestplate, int storageIndex, ItemStack stack) {
        if (storageIndex < 0 || storageIndex >= STORAGE_SIZE || chestplate.isEmpty()) {
            return;
        }
        NonNullList<ItemStack> items = copyContents(chestplate);
        items.set(storageIndex, stack == null ? ItemStack.EMPTY : stack.copy());
        write(chestplate, items);
    }

    /** Number of leading storage slots that are visible for this chestplate material. */
    public static int visibleStorageCount(ItemStack chestplate) {
        ChestRigMaterial material = ChestRigMaterial.of(chestplate);
        if (material == ChestRigMaterial.NONE) {
            return 0;
        }
        if (material.universalSlots() > 0) {
            return UNIVERSAL_SLOT + 1;
        }
        return FOOD_START + material.foodSlots();
    }

    /** Whether every visible storage slot of this chestplate is empty. */
    public static boolean previewIsEmpty(ItemStack chestplate) {
        int visible = visibleStorageCount(chestplate);
        for (int i = 0; i < visible; i++) {
            if (!get(chestplate, i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static void normalize(ItemStack chestplate) {
        if (!chestplate.isEmpty() && ChestRigMaterial.of(chestplate) == ChestRigMaterial.NETHERITE) {
            // copyContents performs the surplus-food relocation and writes it back when needed.
            copyContents(chestplate);
        }
    }

    public static NonNullList<ItemStack> copyContents(ItemStack chestplate) {
        NonNullList<ItemStack> items = NonNullList.withSize(STORAGE_SIZE, ItemStack.EMPTY);
        if (!chestplate.isEmpty()) {
            ItemContainerContents contents = chestplate.get(ModDataComponents.RIG_CONTENTS.get());
            if (contents != null) {
                contents.copyInto(items);
            }
            normalizeNetheriteOverflow(chestplate, items);
        }
        return items;
    }

    public static List<ItemStack> takeContents(ItemStack chestplate) {
        List<ItemStack> result = new ArrayList<>();
        if (chestplate.isEmpty()) {
            return result;
        }

        NonNullList<ItemStack> items = copyContents(chestplate);
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                result.add(stack.copy());
            }
        }
        chestplate.remove(ModDataComponents.RIG_CONTENTS.get());
        return result;
    }

    public static boolean mayPlace(ItemStack chestplate, int storageIndex, ItemStack stack) {
        if (storageIndex < 0 || storageIndex >= STORAGE_SIZE) {
            return false;
        }
        if (storageIndex == UNIVERSAL_SLOT) {
            return true;
        }
        if (storageIndex >= WEAPON_START && storageIndex < WEAPON_START + WEAPON_CAPACITY) {
            return isRigWeapon(stack);
        }
        if (storageIndex >= ARROW_START && storageIndex < ARROW_START + ARROW_CAPACITY) {
            return AmmoSlots.isArrow(stack);
        }
        if (storageIndex >= FOOD_START && storageIndex < FOOD_START + FOOD_CAPACITY) {
            return stack.has(DataComponents.FOOD);
        }
        return false;
    }

    private static boolean isRigWeapon(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof BowItem
                || item instanceof CrossbowItem
                || item instanceof SwordItem
                || item instanceof AxeItem
                || item instanceof TridentItem;
    }

    public static Pair<ResourceLocation, ResourceLocation> emptyIcon(int storageIndex) {
        if (storageIndex >= WEAPON_START && storageIndex < WEAPON_START + WEAPON_CAPACITY) {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_SWORD);
        }
        if (storageIndex >= ARROW_START && storageIndex < ARROW_START + ARROW_CAPACITY) {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_ARROW);
        }
        if (storageIndex >= FOOD_START && storageIndex < FOOD_START + FOOD_CAPACITY) {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_BREAD);
        }
        return null;
    }

    private static void normalizeNetheriteOverflow(ItemStack chestplate, NonNullList<ItemStack> items) {
        // Diamond has three food slots, netherite has two plus one universal slot. Move the
        // surplus food into the universal slot on upgrade so no stored item becomes hidden.
        if (ChestRigMaterial.of(chestplate) == ChestRigMaterial.NETHERITE) {
            int overflowFood = FOOD_START + 2;
            if (!items.get(overflowFood).isEmpty() && items.get(UNIVERSAL_SLOT).isEmpty()) {
                items.set(UNIVERSAL_SLOT, items.get(overflowFood));
                items.set(overflowFood, ItemStack.EMPTY);
                write(chestplate, items);
            }
        }
    }

    private static void write(ItemStack chestplate, NonNullList<ItemStack> items) {
        boolean empty = true;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                empty = false;
                break;
            }
        }

        if (empty) {
            chestplate.remove(ModDataComponents.RIG_CONTENTS.get());
        } else {
            chestplate.set(ModDataComponents.RIG_CONTENTS.get(), ItemContainerContents.fromItems(items));
        }
    }
}

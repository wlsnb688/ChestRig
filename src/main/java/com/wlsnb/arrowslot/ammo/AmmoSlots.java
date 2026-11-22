package com.wlsnb.arrowslot.ammo;

import com.wlsnb.arrowslot.equipment.BrokenGear;
import com.wlsnb.arrowslot.inventory.ChestRig;
import com.wlsnb.arrowslot.inventory.ChestRigMaterial;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

import java.util.ArrayList;
import java.util.List;

/**
 * The selectable ammo window is a single row made of the chest-rig arrow slots, the
 * offhand slot, the nine hotbar slots and the twenty-seven main inventory slots.
 *
 * <p>Only slots that currently contain arrow-type items are shown. The default search
 * order is chest-rig arrow slots, offhand, hotbar 1..9, then main inventory 1..27.</p>
 */
public final class AmmoSlots {
    public static final int RIG_ARROW_START = 0;
    public static final int RIG_ARROW_COUNT = ChestRig.ARROW_CAPACITY;
    public static final int OFFHAND = RIG_ARROW_START + RIG_ARROW_COUNT;
    public static final int HOTBAR_START = OFFHAND + 1;
    public static final int HOTBAR_COUNT = 9;
    public static final int MAIN_START = HOTBAR_START + HOTBAR_COUNT;
    public static final int MAIN_COUNT = 27;
    public static final int COUNT = MAIN_START + MAIN_COUNT;

    private AmmoSlots() {
    }

    public static boolean isWeapon(ItemStack stack) {
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    public static boolean isArrow(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof ArrowItem || stack.is(ItemTags.ARROWS));
    }

    public static ItemStack get(Player player, int index) {
        if (index >= RIG_ARROW_START && index < RIG_ARROW_START + RIG_ARROW_COUNT) {
            return ChestRig.getArrow(player, index - RIG_ARROW_START);
        }
        if (index == OFFHAND) {
            return player.getOffhandItem();
        }
        if (index >= HOTBAR_START && index < HOTBAR_START + HOTBAR_COUNT) {
            return player.getInventory().getItem(index - HOTBAR_START);
        }
        if (index >= MAIN_START && index < COUNT) {
            return player.getInventory().getItem(9 + index - MAIN_START);
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getHeldWeapon(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (isWeapon(mainHand) && !BrokenGear.isBroken(mainHand)) {
            return mainHand;
        }
        ItemStack offhand = player.getOffhandItem();
        if (isWeapon(offhand) && !BrokenGear.isBroken(offhand)) {
            return offhand;
        }
        return ItemStack.EMPTY;
    }

    public static boolean isValidAmmo(ItemStack weapon, ItemStack ammo) {
        if (!isWeapon(weapon) || BrokenGear.isBroken(weapon) || !isArrow(ammo)) {
            return false;
        }
        return ((ProjectileWeaponItem) weapon.getItem()).getAllSupportedProjectiles(weapon).test(ammo);
    }

    /**
     * @return the first arrow-containing slot in the chest-rig, offhand, hotbar 1..9,
     * main 1..27 order, or {@code -1} when no arrow is present.
     */
    public static int findDefault(Player player, ItemStack weapon) {
        for (int i = 0; i < COUNT; i++) {
            if (isValidAmmo(weapon, get(player, i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return the candidate indices that currently contain arrow-type items, in default order.
     */
    public static List<Integer> visibleArrowSlots(Player player) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < COUNT; i++) {
            if (isArrow(get(player, i))) {
                result.add(i);
            }
        }
        return result;
    }

    public static boolean isRigArrowSlot(int index) {
        return index >= RIG_ARROW_START && index < RIG_ARROW_START + RIG_ARROW_COUNT;
    }

    /**
     * Consumes ammo from the real chest-rig stack. The projectile code receives a marked
     * copy, so consumption cannot use the copy's own split/shrink operation.
     */
    public static ItemStack consumeRigAmmo(Player player, int candidate, int count) {
        if (!isRigArrowSlot(candidate) || count <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack chestplate = ChestRig.getChestplate(player);
        int arrowIndex = candidate - RIG_ARROW_START;
        if (arrowIndex >= ChestRigMaterial.of(chestplate).arrowSlots()) {
            return ItemStack.EMPTY;
        }

        int storageIndex = ChestRig.ARROW_START + arrowIndex;
        ItemStack actual = ChestRig.get(chestplate, storageIndex);
        if (actual.isEmpty() || actual.getCount() < count) {
            return ItemStack.EMPTY;
        }

        ItemStack taken = actual.copyWithCount(count);
        actual.shrink(count);
        ChestRig.set(chestplate, storageIndex, actual);
        return taken;
    }

    public static String labelTranslationKey(int index) {
        if (isRigArrowSlot(index)) {
            return "hud.arrowslot.rig";
        }
        if (index == OFFHAND) {
            return "hud.arrowslot.offhand";
        }
        if (index >= HOTBAR_START && index < HOTBAR_START + HOTBAR_COUNT) {
            return "hud.arrowslot.hotbar";
        }
        return "hud.arrowslot.main";
    }
}

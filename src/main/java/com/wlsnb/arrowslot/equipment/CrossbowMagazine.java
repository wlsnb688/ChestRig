package com.wlsnb.arrowslot.equipment;

import com.wlsnb.arrowslot.ammo.AmmoSelection;
import com.wlsnb.arrowslot.ammo.AmmoSlots;
import com.wlsnb.arrowslot.inventory.ChestRig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-side crossbow magazine built on vanilla {@link ChargedProjectiles}.
 *
 * <p>Loaded projectiles are one stack each, all the same item/components. Vanilla's
 * charged-crossbow path already fires the whole list, so no custom shooting code is
 * needed for the magazine itself.</p>
 */
public final class CrossbowMagazine {
    private CrossbowMagazine() {
    }

    public static int loaded(ItemStack stack) {
        ChargedProjectiles charged = stack.get(DataComponents.CHARGED_PROJECTILES);
        return charged == null ? 0 : charged.getItems().size();
    }

    public static boolean isFull(ServerPlayer player, ItemStack stack) {
        int max = CrossbowEnchantments.maxLoaded(player, stack);
        return max <= 0 || loaded(stack) >= max;
    }

    public static boolean loadOne(ServerPlayer player, ItemStack stack, boolean dropIfFull) {
        if (!CrossbowEnchantments.isCrossbow(stack) || BrokenGear.isBroken(stack)) {
            return false;
        }
        CrossbowEnchantments.normalize(player, stack);
        if (CrossbowEnchantments.isInstantLoad(player, stack)) {
            return false;
        }

        int max = CrossbowEnchantments.maxLoaded(player, stack);
        if (max <= 0) {
            return false;
        }

        List<ItemStack> items = new ArrayList<>(loadedItems(stack));
        ItemStack next = AmmoSelection.peekSelected(player, stack);
        if (next.isEmpty()) {
            return false;
        }

        if (!items.isEmpty()) {
            boolean same = ItemStack.isSameItemSameComponents(items.get(0), next);
            if (items.size() >= max || !same) {
                if (!dropIfFull) {
                    return false;
                }
                dropLoaded(player, stack, items);
                items.clear();
            }
        }

        ItemStack loaded = AmmoSelection.consumeSelected(player, stack);
        if (loaded.isEmpty()) {
            return false;
        }

        items.add(loaded);
        setLoaded(stack, items);
        player.inventoryMenu.broadcastChanges();
        playLoadSound(player);
        return true;
    }

    public static void manualReload(ServerPlayer player) {
        ItemStack weapon = AmmoSlots.getHeldWeapon(player);
        if (!CrossbowEnchantments.isCrossbow(weapon) || BrokenGear.isBroken(weapon)) {
            return;
        }
        CrossbowEnchantments.normalize(player, weapon);
        if (CrossbowEnchantments.isInstantLoad(player, weapon)) {
            return;
        }
        if (player.isUsingItem()) {
            player.stopUsingItem();
        }
        loadOne(player, weapon, true);
    }

    public static void stopReload(ServerPlayer player) {
        if (player.isUsingItem() && CrossbowEnchantments.isCrossbow(player.getUseItem())) {
            player.stopUsingItem();
        }
    }

    public static void tick(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            CrossbowEnchantments.normalize(player, player.getInventory().getItem(i));
        }
        for (int i = 0; i < ChestRig.weaponSlots(player); i++) {
            ItemStack stack = ChestRig.getWeapon(player, i);
            if (CrossbowEnchantments.isCrossbow(stack)) {
                ItemStack before = stack.copy();
                CrossbowEnchantments.normalize(player, stack);
                if (!ItemStack.matches(before, stack)) {
                    ChestRig.setWeapon(player, i, stack);
                }
            }
        }
    }

    public static boolean shootInstant(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        if (!CrossbowEnchantments.isInstantLoad(player, stack)) {
            return false;
        }
        clearLoaded(player, stack, true);
        ItemStack ammo = AmmoSelection.consumeSelected(player, stack);
        if (ammo.isEmpty()) {
            return false;
        }

        stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(List.of(ammo)));
        ((CrossbowItem) stack.getItem()).performShooting(
                player.level(), player, hand, stack, 3.15F, 1.0F, null);
        player.inventoryMenu.broadcastChanges();
        return true;
    }

    public static void clearLoaded(ServerPlayer player, ItemStack stack, boolean drop) {
        List<ItemStack> items = loadedItems(stack);
        if (items.isEmpty()) {
            return;
        }
        if (drop) {
            dropLoaded(player, stack, items);
        }
        stack.remove(DataComponents.CHARGED_PROJECTILES);
        player.inventoryMenu.broadcastChanges();
    }

    private static List<ItemStack> loadedItems(ItemStack stack) {
        ChargedProjectiles charged = stack.get(DataComponents.CHARGED_PROJECTILES);
        return charged == null ? List.of() : charged.getItems();
    }

    private static void setLoaded(ItemStack stack, List<ItemStack> items) {
        if (items.isEmpty()) {
            stack.remove(DataComponents.CHARGED_PROJECTILES);
        } else {
            stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(items));
        }
    }

    private static void dropLoaded(ServerPlayer player, ItemStack stack, List<ItemStack> items) {
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                player.drop(item.copyWithCount(1), false);
            }
        }
        stack.remove(DataComponents.CHARGED_PROJECTILES);
    }

    private static void playLoadSound(ServerPlayer player) {
        player.level().playSound(
                null,
                player,
                SoundEvents.CROSSBOW_LOADING_END.value(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F / (player.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F
        );
    }
}

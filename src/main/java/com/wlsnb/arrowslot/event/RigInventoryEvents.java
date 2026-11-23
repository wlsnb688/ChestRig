package com.wlsnb.arrowslot.event;

import com.wlsnb.arrowslot.ArrowSlotMod;
import com.wlsnb.arrowslot.ammo.AmmoSlots;
import com.wlsnb.arrowslot.equipment.RigWeaponController;
import com.wlsnb.arrowslot.inventory.ChestRig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Inventory feedback for the chest rig: pickup routing and right-click storage.
 */
@EventBusSubscriber(modid = ArrowSlotMod.MOD_ID)
public final class RigInventoryEvents {
    private RigInventoryEvents() {
    }

    @SubscribeEvent
    public static void onPickup(ItemEntityPickupEvent.Pre event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        ItemEntity itemEntity = event.getItemEntity();
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) {
            return;
        }

        if (AmmoSlots.isArrow(stack)) {
            int moved = ChestRig.insertArrows(player, stack, false);
            finishPickup(event, itemEntity, stack, moved > 0);
            return;
        }

        if (stack.has(DataComponents.FOOD)) {
            int moved = ChestRig.insertFood(player, stack, false);
            finishPickup(event, itemEntity, stack, moved > 0);
            return;
        }

        ItemStack universal = ChestRig.get(ChestRig.getChestplate(player), ChestRig.UNIVERSAL_SLOT);
        boolean canUseUniversal = !universal.isEmpty()
                ? ItemStack.isSameItemSameComponents(universal, stack) && universal.getCount() < stack.getMaxStackSize()
                : stack.is(Items.TOTEM_OF_UNDYING);
        if (!canUseUniversal || hasHotbarOrOffhandCopy(player, stack)) {
            return;
        }

        int moved = ChestRig.insertUniversal(player, stack);
        finishPickup(event, itemEntity, stack, moved > 0);
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (RigWeaponController.isActive(player)) {
            return;
        }

        ItemStack held = player.getItemInHand(event.getHand());
        if (held.isEmpty()) {
            return;
        }

        if (AmmoSlots.isArrow(held)) {
            if (ChestRig.insertArrows(player, held, true) > 0) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
            return;
        }

        if (!(held.getItem() instanceof SwordItem) && !(held.getItem() instanceof AxeItem)) {
            return;
        }

        if (ChestRig.insertWeapon(player, held)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        int index = RigWeaponController.displayIndex(player);
        ItemStack stored = ChestRig.getWeapon(player, index);
        if (stored.isEmpty()) {
            return;
        }

        ChestRig.setWeapon(player, index, held.copy());
        player.setItemInHand(event.getHand(), stored);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private static void finishPickup(ItemEntityPickupEvent.Pre event, ItemEntity itemEntity, ItemStack stack,
                                     boolean moved) {
        if (!moved) {
            return;
        }
        if (stack.isEmpty()) {
            // Only suppress the vanilla pickup when the whole stack was moved. A partial
            // merge leaves the remainder for the normal inventory pickup.
            itemEntity.discard();
            event.setCanPickup(TriState.FALSE);
        }
    }

    private static boolean hasHotbarOrOffhandCopy(Player player, ItemStack stack) {
        for (int i = 0; i < 9; i++) {
            ItemStack slot = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
                return true;
            }
        }

        ItemStack offhand = player.getOffhandItem();
        return ItemStack.isSameItemSameComponents(offhand, stack) && offhand.getCount() < offhand.getMaxStackSize();
    }
}

package com.wlsnb.arrowslot.event;

import com.wlsnb.arrowslot.ArrowSlotMod;
import com.wlsnb.arrowslot.ammo.AmmoSelection;
import com.wlsnb.arrowslot.equipment.BrokenGear;
import com.wlsnb.arrowslot.equipment.ChestRigAutomation;
import com.wlsnb.arrowslot.equipment.CrossbowMagazine;
import com.wlsnb.arrowslot.equipment.RigWeaponController;
import com.wlsnb.arrowslot.equipment.RigWeaponState;
import com.wlsnb.arrowslot.inventory.ChestRig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = ArrowSlotMod.MOD_ID)
public final class CommonEvents {
    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Persist a damaged/consumed virtual rig weapon before scanning weapon ids,
            // otherwise the rig copy and the held copy could receive different ids.
            RigWeaponController.tick(player);
            AmmoSelection.tick(player);
            BrokenGear.tickTemporaryBinding(player);
            CrossbowMagazine.tick(player);
            ChestRigAutomation.tick(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RigWeaponController.restoreForTransition(player);
            RigWeaponController.clear(player);
            CrossbowMagazine.clear(player);
            ChestRigAutomation.clear(player);
        }
        AmmoSelection.clear(event.getEntity());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RigWeaponController.restoreForTransition(player);
            // Cooldowns use absolute tick counters, which restart on the respawned player.
            CrossbowMagazine.stopReload(player);
            CrossbowMagazine.clear(player);
            ChestRigAutomation.resetAfterDeath(player);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getOriginalDamage() > 0.0F) {
            CrossbowMagazine.stopReload(player);
            ChestRigAutomation.onDamaged(player);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RigWeaponController.restoreForTransition(player);
            CrossbowMagazine.stopReload(player);
            ChestRigAutomation.cancel(player);
        }
    }

    /**
     * While a rig weapon is virtually held, a vanilla offhand swap must move the real
     * hotbar stack, not the virtual rig stack. Deactivate first and replace the event's
     * virtual main-hand entry with the actual hotbar item.
     */
    @SubscribeEvent
    public static void onSwapHands(LivingSwapItemsEvent.Hands event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !RigWeaponController.isActive(player)) {
            return;
        }
        if (RigWeaponState.isPending(player)) {
            // Only G or a successful hotbar change may end the empty pending selection.
            event.setCanceled(true);
            return;
        }

        ItemStack hotbar = player.getInventory().getItem(player.getInventory().selected).copy();
        ItemStack offhand = player.getOffhandItem().copy();
        event.setItemSwappedToMainHand(offhand);
        event.setItemSwappedToOffHand(hotbar);
        RigWeaponController.restoreForTransition(player);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        Player original = event.getOriginal();
        if (event.isWasDeath()) {
            boolean keepInventory = player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
            if (keepInventory) {
                AmmoSelection.copyState(original, player);
                BrokenGear.applyTemporaryBinding(player);
            } else {
                AmmoSelection.clear(original);
            }
            CrossbowMagazine.clear(player);
            ChestRigAutomation.clear(player);
            RigWeaponController.clear(original);
        } else {
            // Dimension travel keeps the transient per-weapon choices.
            AmmoSelection.copyState(original, player);
        }
    }

    /**
     * Broken armor is destroyed on death when keepInventory is off. A broken chestplate
     * drops its stored contents instead of dropping the chestplate itself.
     */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return;
        }

        List<ItemStack> chestContents = new ArrayList<>();
        Iterator<ItemEntity> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemEntity drop = iterator.next();
            ItemStack stack = drop.getItem();
            if (!BrokenGear.isArmorOrShield(stack) || !BrokenGear.isBroken(stack)) {
                continue;
            }

            iterator.remove();
            if (BrokenGear.isChestplate(stack)) {
                chestContents.addAll(ChestRig.takeContents(stack));
            }
        }

        for (ItemStack stack : chestContents) {
            if (stack.isEmpty()) {
                continue;
            }
            event.getDrops().add(new ItemEntity(
                    player.level(),
                    player.getX(),
                    player.getY() + 0.5D,
                    player.getZ(),
                    stack
            ));
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (BrokenGear.isBroken(event.getEntity().getMainHandItem())) {
            // Broken tools should not keep mining at full speed forever.
            event.setNewSpeed(0.0F);
        }
    }

    /**
     * Broken gear keeps its component modifiers, but they no longer apply to the entity.
     * Equipment-change detection calls this event again when the damage component changes,
     * so an equipped item loses its armor value on the next equipment update.
     */
    @SubscribeEvent
    public static void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        if (BrokenGear.isBroken(event.getItemStack())) {
            event.clearModifiers();
        }
    }
}

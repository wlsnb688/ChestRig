package com.wlsnb.arrowslot.equipment;

import com.wlsnb.arrowslot.inventory.ChestRig;
import com.wlsnb.arrowslot.network.ModNetwork;
import com.wlsnb.arrowslot.registry.ModDataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Handles a trident thrown from a chest-rig weapon slot. Vanilla only removes the thrown
 * stack from the player inventory, so a virtual rig weapon would stay in the rig while a
 * second trident entity is created. This class removes the rig copy, tags the entity with
 * its owner and original slot, and returns it to that slot on pickup.
 */
public final class RigTridentHandler {
    private static final String TAG_OWNER = "ArrowSlotRigOwner";
    private static final String TAG_SLOT = "ArrowSlotRigSlot";
    private static final String TAG_WEAPON_ID = "ArrowSlotRigWeaponId";

    private RigTridentHandler() {
    }

    /** Client-side guard: mirrors the normal (non-riptide) throw branch. */
    public static boolean isNormalThrow(net.minecraft.world.entity.player.Player player, ItemStack stack, int timeLeft) {
        if (!(stack.getItem() instanceof TridentItem)) {
            return false;
        }
        int useTicks = stack.getItem().getUseDuration(stack, player) - timeLeft;
        return useTicks >= 10 && EnchantmentHelper.getTridentSpinAttackStrength(stack, player) <= 0.0F;
    }

    /**
     * @return true when the throw was handled and the vanilla method must be cancelled.
     */
    public static boolean throwFromRig(ServerPlayer player, ItemStack stack, Level level, int timeLeft) {
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        if (player.getUsedItemHand() != InteractionHand.MAIN_HAND) {
            return false;
        }
        if (!(stack.getItem() instanceof TridentItem)) {
            return false;
        }
        if (!RigWeaponState.isActive(player)) {
            return false;
        }

        int slot = RigWeaponState.index(player);
        if (slot < 0 || slot >= ChestRig.weaponSlots(player)) {
            return false;
        }
        ItemStack stored = ChestRig.getWeapon(player, slot);
        UUID storedId = stored.get(ModDataComponents.WEAPON_ID.get());
        UUID heldId = stack.get(ModDataComponents.WEAPON_ID.get());
        if (stored.isEmpty()
                || !ItemStack.isSameItem(stored, stack)
                || (storedId != null && heldId != null && !storedId.equals(heldId))) {
            // The held virtual stack no longer matches the slot. Cancel the vanilla throw
            // (which cannot remove a virtual stack) instead of creating a free trident.
            RigWeaponState.deactivate(player);
            sync(player);
            return true;
        }

        int useTicks = stack.getItem().getUseDuration(stack, player) - timeLeft;
        if (useTicks < 10) {
            return false;
        }

        // Riptide does not create a thrown entity, so vanilla already handles it correctly.
        float spin = EnchantmentHelper.getTridentSpinAttackStrength(stack, player);
        if (spin > 0.0F) {
            return false;
        }

        // Mirror TridentItem.isTooDamagedToUse; vanilla would not throw in this case.
        if (stack.getMaxDamage() > 0 && stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return false;
        }

        // Creative tridents are CREATIVE_ONLY and are not removed from the inventory.
        if (player.hasInfiniteMaterials()) {
            return false;
        }

        ItemStack thrown = stack.copy();
        thrown.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        if (thrown.isEmpty()) {
            ChestRig.setWeapon(player, slot, ItemStack.EMPTY);
            RigWeaponState.deactivate(player);
            sync(player);
            return true;
        }

        UUID weaponId = thrown.get(ModDataComponents.WEAPON_ID.get());
        if (weaponId == null) {
            weaponId = UUID.randomUUID();
            thrown.set(ModDataComponents.WEAPON_ID.get(), weaponId);
        }

        ThrownTrident trident = new ThrownTrident(level, player, thrown);
        trident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
        trident.setGlowingTag(true);
        CompoundTag tag = trident.getPersistentData();
        tag.putUUID(TAG_OWNER, player.getUUID());
        tag.putInt(TAG_SLOT, slot);
        tag.putUUID(TAG_WEAPON_ID, weaponId);

        level.addFreshEntity(trident);
        level.playSound(null, trident, SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));

        ChestRig.setWeapon(player, slot, ItemStack.EMPTY);
        RigWeaponController.holdEmptyAfterThrow(player, slot);
        player.inventoryMenu.broadcastChanges();
        return true;
    }

    /**
     * @return true when the trident was consumed by the rig and the vanilla pickup must be
     * cancelled.
     */
    public static boolean tryPickup(ServerPlayer player, ThrownTrident trident) {
        CompoundTag tag = trident.getPersistentData();
        if (!tag.contains(TAG_OWNER)) {
            return false;
        }
        UUID owner = tag.getUUID(TAG_OWNER);
        if (!player.getUUID().equals(owner)) {
            return false;
        }

        ItemStack picked = trident.getPickupItemStackOrigin().copy();
        if (picked.isEmpty() || !(picked.getItem() instanceof TridentItem)) {
            return false;
        }
        if (tag.contains(TAG_WEAPON_ID)) {
            picked.set(ModDataComponents.WEAPON_ID.get(), tag.getUUID(TAG_WEAPON_ID));
        }

        int slot = tag.getInt(TAG_SLOT);
        if (slot < 0 || slot >= ChestRig.weaponSlots(player) || !ChestRig.getWeapon(player, slot).isEmpty()) {
            slot = firstEmptyWeaponSlot(player);
        }
        if (slot < 0) {
            // The rig has no room; let vanilla put the trident into the player inventory.
            return false;
        }

        ChestRig.setWeapon(player, slot, picked);
        RigWeaponController.resumeHeldAfterPickup(player, slot, picked);
        player.inventoryMenu.broadcastChanges();
        trident.discard();
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ITEM_PICKUP,
                SoundSource.PLAYERS,
                0.2F,
                1.0F
        );
        return true;
    }

    private static int firstEmptyWeaponSlot(ServerPlayer player) {
        for (int i = 0; i < ChestRig.weaponSlots(player); i++) {
            if (ChestRig.getWeapon(player, i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private static void sync(ServerPlayer player) {
        ModNetwork.syncRigWeapon(player, false, false, 0, ItemStack.EMPTY);
    }
}

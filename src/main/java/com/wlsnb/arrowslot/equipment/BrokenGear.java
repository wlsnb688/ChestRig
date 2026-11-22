package com.wlsnb.arrowslot.equipment;

import com.wlsnb.arrowslot.component.TemporaryBinding;
import com.wlsnb.arrowslot.registry.ModDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Rules for gear whose durability has reached zero.
 *
 * <p>Tracked equipment remains in the world at zero durability instead of disappearing.
 * Armor and shields lose their protective benefits and non-negative enchantment effects;
 * curses and Mending keep working. The item can be repaired normally.</p>
 */
public final class BrokenGear {
    public static final int TEMPORARY_BINDING_TICKS = 20 * 60;

    private BrokenGear() {
    }

    public static boolean isBroken(ItemStack stack) {
        return stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage();
    }

    public static boolean isBrokenArmor(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem && isBroken(stack);
    }

    public static boolean isBrokenShield(ItemStack stack) {
        return stack.getItem() instanceof ShieldItem && isBroken(stack);
    }

    public static boolean isArmorOrShield(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem || stack.getItem() instanceof ShieldItem;
    }

    public static boolean isChestplate(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.CHESTPLATE;
    }

    public static boolean isTrackedGear(ItemStack stack) {
        if (!stack.isDamageableItem()) {
            return false;
        }
        Item item = stack.getItem();
        return item instanceof ArmorItem
                || item instanceof ShieldItem
                || item instanceof TieredItem
                || item instanceof BowItem
                || item instanceof CrossbowItem
                || item instanceof TridentItem;
    }

    public static boolean isDiamondToNetherite(ItemStack base, ItemStack result) {
        if (base.isEmpty() || result.isEmpty()) {
            return false;
        }

        ResourceLocation baseId = BuiltInRegistries.ITEM.getKey(base.getItem());
        ResourceLocation resultId = BuiltInRegistries.ITEM.getKey(result.getItem());
        if (!"minecraft".equals(baseId.getNamespace()) || !"minecraft".equals(resultId.getNamespace())) {
            return false;
        }

        String basePath = baseId.getPath();
        return basePath.startsWith("diamond_")
                && resultId.getPath().equals("netherite_" + basePath.substring("diamond_".length()));
    }

    public static void destroyBrokenShield(LivingEntity entity) {
        ItemStack shield = entity.getUseItem();
        if (!isBrokenShield(shield)) {
            return;
        }

        InteractionHand hand = entity.getUsedItemHand();
        entity.stopUsingItem();
        entity.setItemInHand(hand, ItemStack.EMPTY);
        entity.playSound(
                SoundEvents.SHIELD_BREAK,
                0.8F,
                0.8F + entity.getRandom().nextFloat() * 0.4F
        );
    }

    public static void applyTemporaryBinding(ServerPlayer player) {
        long now = player.level().getGameTime();
        Holder<Enchantment> binding = player.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.BINDING_CURSE);

        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!isBrokenArmor(stack)) {
                continue;
            }

            TemporaryBinding existing = stack.get(ModDataComponents.TEMPORARY_BINDING.get());
            boolean hadCurse = stack.getEnchantmentLevel(binding) > 0;
            boolean addedCurse = existing != null ? existing.addedCurse() : !hadCurse;
            if (!hadCurse) {
                stack.enchant(binding, 1);
            }
            stack.set(
                    ModDataComponents.TEMPORARY_BINDING.get(),
                    new TemporaryBinding(now + TEMPORARY_BINDING_TICKS, addedCurse)
            );
        }
    }

    public static void tickTemporaryBinding(ServerPlayer player) {
        long now = player.level().getGameTime();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            TemporaryBinding existing = stack.get(ModDataComponents.TEMPORARY_BINDING.get());
            if (existing == null || now < existing.expiresAt()) {
                continue;
            }

            stack.remove(ModDataComponents.TEMPORARY_BINDING.get());
            if (existing.addedCurse()) {
                removeBindingCurse(player, stack);
            }
        }
    }

    private static void removeBindingCurse(ServerPlayer player, ItemStack stack) {
        Holder<Enchantment> binding = player.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.BINDING_CURSE);
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(
                EnchantmentHelper.getEnchantmentsForCrafting(stack)
        );
        enchantments.removeIf(holder -> holder.equals(binding));
        EnchantmentHelper.setEnchantments(stack, enchantments.toImmutable());
    }
}

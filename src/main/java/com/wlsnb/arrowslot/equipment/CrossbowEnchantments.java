package com.wlsnb.arrowslot.equipment;

import com.wlsnb.arrowslot.ArrowSlotMod;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Code-side rules for the two data-driven crossbow enchantments.
 *
 * <p>Multi-Load raises the magazine cap from 8 to 16/32. At level 3 it converts into
 * Instant Load, which removes the magazine entirely. Instant Load can never convert back.</p>
 */
public final class CrossbowEnchantments {
    private static final ResourceKey<Enchantment> MULTILOAD = key("multiload");
    private static final ResourceKey<Enchantment> INSTANT_LOAD = key("instant_load");

    private CrossbowEnchantments() {
    }

    public static boolean isCrossbow(ItemStack stack) {
        return stack.getItem() instanceof CrossbowItem;
    }

    public static int multiloadLevel(Player player, ItemStack stack) {
        Holder<Enchantment> enchantment = holder(player, MULTILOAD);
        return enchantment == null ? 0 : level(stack, enchantment);
    }

    public static boolean isInstantLoad(Player player, ItemStack stack) {
        Holder<Enchantment> enchantment = holder(player, INSTANT_LOAD);
        return enchantment != null && level(stack, enchantment) > 0;
    }

    public static int maxLoaded(Player player, ItemStack stack) {
        if (isInstantLoad(player, stack)) {
            return 0;
        }
        return switch (multiloadLevel(player, stack)) {
            case 1 -> 16;
            case 2 -> 32;
            default -> 8;
        };
    }

    /** Four ticks per bolt, or two ticks with any Quick Charge level. */
    public static int loadInterval(Player player, ItemStack stack) {
        Holder<Enchantment> quickCharge = holder(player, Enchantments.QUICK_CHARGE);
        return quickCharge != null && level(stack, quickCharge) > 0 ? 2 : 4;
    }

    /**
     * Enforces exclusivity and the one-way level-3 conversion. Safe to call repeatedly.
     */
    public static void normalize(Player player, ItemStack stack) {
        if (!isCrossbow(stack)) {
            return;
        }
        Holder<Enchantment> multiload = holder(player, MULTILOAD);
        Holder<Enchantment> instant = holder(player, INSTANT_LOAD);
        if (multiload == null || instant == null) {
            return;
        }

        ItemEnchantments current = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        int multiloadLevel = current.getLevel(multiload);
        int instantLevel = current.getLevel(instant);
        if (multiloadLevel <= 0 && instantLevel <= 0) {
            return;
        }

        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(current);
        boolean changed = false;
        if (multiloadLevel >= 3) {
            mutable.set(multiload, 0);
            mutable.set(instant, 1);
            changed = true;
        } else if (multiloadLevel > 0 && instantLevel > 0) {
            mutable.set(multiload, 0);
            changed = true;
        }
        if (changed) {
            stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        }

        if (isInstantLoad(player, stack) && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            CrossbowMagazine.clearLoaded(serverPlayer, stack, true);
        }
    }

    private static int level(ItemStack stack, Holder<Enchantment> enchantment) {
        return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
                .getLevel(enchantment);
    }

    private static Holder<Enchantment> holder(Player player, ResourceKey<Enchantment> key) {
        return player.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(key)
                .orElse(null);
    }

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT,
                ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, path));
    }
}

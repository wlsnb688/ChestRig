package com.wlsnb.arrowslot.event;

import com.wlsnb.arrowslot.ArrowSlotMod;
import com.wlsnb.arrowslot.loot.LeafDropRules;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/**
 * Replaces apples dropped from leaves with the configured golden-apple chances.
 * The normal leaf drop probabilities are doubled in {@code BonusLevelTableConditionMixin}.
 */
@EventBusSubscriber(modid = ArrowSlotMod.MOD_ID)
public final class LeafDrops {
    private static final float ENCHANTED_GOLDEN_APPLE_CHANCE = 0.025F;
    private static final float GOLDEN_APPLE_CHANCE = 0.20F;

    private LeafDrops() {
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!event.getState().is(BlockTags.LEAVES) || !(event.getBreaker() instanceof Player)) {
            return;
        }

        if (LeafDropRules.keepsVanillaLeafDrops(event.getTool())) {
            return;
        }

        for (ItemEntity drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            if (!stack.is(Items.APPLE)) {
                continue;
            }

            float roll = event.getLevel().getRandom().nextFloat();
            Item replacement;
            if (roll < ENCHANTED_GOLDEN_APPLE_CHANCE) {
                replacement = Items.ENCHANTED_GOLDEN_APPLE;
            } else if (roll < ENCHANTED_GOLDEN_APPLE_CHANCE + GOLDEN_APPLE_CHANCE) {
                replacement = Items.GOLDEN_APPLE;
            } else {
                replacement = Items.APPLE;
            }

            if (replacement != Items.APPLE) {
                drop.setItem(new ItemStack(replacement, stack.getCount()));
            }
        }
    }
}

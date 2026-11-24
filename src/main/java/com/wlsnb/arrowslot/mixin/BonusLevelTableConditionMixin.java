package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.loot.LeafDropRules;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Doubles every table-bonus chance in leaf loot tables when the leaf is broken
 * without shears or Silk Touch. Leaf saplings, sticks and apples all use this
 * condition, so no individual loot table has to be overridden.
 */
@SuppressWarnings("deprecation")
@Mixin(BonusLevelTableCondition.class)
public abstract class BonusLevelTableConditionMixin {
    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void arrowslot$doubleLeafDropChance(LootContext context, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state == null || !state.is(BlockTags.LEAVES)
                || !(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Player)) {
            return;
        }

        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (LeafDropRules.keepsVanillaLeafDrops(tool)) {
            return;
        }

        BonusLevelTableCondition condition = (BonusLevelTableCondition) (Object) this;
        ItemEnchantments enchantments = tool == null
                ? ItemEnchantments.EMPTY
                : tool.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        int level = enchantments.getLevel(condition.enchantment());
        List<Float> values = condition.values();
        float chance = values.get(Math.min(level, values.size() - 1));
        chance = Math.min(1.0F, chance * 2.0F);
        cir.setReturnValue(context.getRandom().nextFloat() < chance);
    }
}

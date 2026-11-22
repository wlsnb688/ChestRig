package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.equipment.BrokenGear;
import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiConsumer;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    /**
     * Broken armor keeps its curses and Mending, but all other equipment enchantment
     * effects are skipped. This central iteration is used by protection, thorns, frost
     * walker, soul speed, respiration helpers and similar equipment effects.
     */
    @Inject(method = "runIterationOnEquipment", at = @At("HEAD"), cancellable = true)
    private static void arrowslot$skipBrokenArmorEffects(LivingEntity entity, EnchantmentHelper.EnchantmentInSlotVisitor visitor, CallbackInfo ci) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (BrokenGear.isBrokenArmor(stack)) {
                EnchantmentHelper.runIterationOnItem(stack, slot, entity, (holder, level, inUse) -> {
                    if (holder.is(EnchantmentTags.CURSE) || isMending(holder)) {
                        visitor.accept(holder, level, inUse);
                    }
                });
            } else {
                EnchantmentHelper.runIterationOnItem(stack, slot, entity, visitor);
            }
        }
        ci.cancel();
    }

    @Inject(method = "getEnchantmentLevel", at = @At("HEAD"), cancellable = true)
    private static void arrowslot$skipBrokenArmorLevel(Holder<Enchantment> enchantment, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (enchantment.is(EnchantmentTags.CURSE) || isMending(enchantment)) {
            return;
        }

        int level = 0;
        for (ItemStack stack : enchantment.value().getSlotItems(entity).values()) {
            if (BrokenGear.isBrokenArmor(stack)) {
                continue;
            }
            level = Math.max(level, stack.getEnchantmentLevel(enchantment));
        }
        cir.setReturnValue(level);
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("HEAD"), cancellable = true)
    private static void arrowslot$skipBrokenArmorSlotModifiers(ItemStack stack, EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> action, CallbackInfo ci) {
        if (BrokenGear.isBrokenArmor(stack)) {
            ci.cancel();
        }
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V", at = @At("HEAD"), cancellable = true)
    private static void arrowslot$skipBrokenArmorGroupModifiers(ItemStack stack, EquipmentSlotGroup slotGroup, BiConsumer<Holder<Attribute>, AttributeModifier> action, CallbackInfo ci) {
        if (BrokenGear.isBrokenArmor(stack)) {
            ci.cancel();
        }
    }

    private static boolean isMending(Holder<Enchantment> enchantment) {
        return enchantment.unwrapKey().filter(key -> key.equals(Enchantments.MENDING)).isPresent();
    }
}

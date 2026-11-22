package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.equipment.BrokenGear;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IItemExtension.class)
public interface ItemExtensionMixin {
    @Inject(method = "canEquip", at = @At("HEAD"), cancellable = true)
    private void arrowslot$denyBrokenEquipment(ItemStack stack, EquipmentSlot armorType, LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (BrokenGear.isBroken(stack)) {
            cir.setReturnValue(false);
        }
    }
}

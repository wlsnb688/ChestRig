package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.equipment.BrokenGear;
import com.wlsnb.arrowslot.inventory.ChestRig;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmithingTransformRecipe.class)
public abstract class SmithingTransformRecipeMixin {
    /**
     * A damaged diamond item upgraded to its netherite counterpart immediately returns to
     * full durability. The custom components (including chest-rig contents, name and
     * enchantments) are already copied by ItemStack#transmuteCopy.
     */
    @Inject(method = "assemble", at = @At("RETURN"), cancellable = true)
    private void arrowslot$refillUpgradedDiamondGear(SmithingRecipeInput input, HolderLookup.Provider registries, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        ItemStack base = input.base();
        if (result.isEmpty() || !BrokenGear.isDiamondToNetherite(base, result)) {
            return;
        }
        if (base.getDamageValue() > 0) {
            result.setDamageValue(0);
        }
        ChestRig.normalize(result);
        cir.setReturnValue(result);
    }
}

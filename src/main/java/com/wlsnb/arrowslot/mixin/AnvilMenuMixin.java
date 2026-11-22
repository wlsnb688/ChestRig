package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.equipment.BrokenGear;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
    @Inject(method = "createResult", at = @At("RETURN"))
    private void arrowslot$brokenGearRepairCost(CallbackInfo ci) {
        AnvilMenu menu = (AnvilMenu) (Object) this;
        ItemStack left = menu.getSlot(AnvilMenu.INPUT_SLOT).getItem();
        ItemStack right = menu.getSlot(AnvilMenu.ADDITIONAL_SLOT).getItem();
        if (!BrokenGear.isBroken(left) || menu.getSlot(AnvilMenu.RESULT_SLOT).getItem().isEmpty()) {
            return;
        }

        if (ItemStack.isSameItem(left, right) && right.isDamageableItem()) {
            // Combining two identical pieces is the expensive repair route.
            menu.setMaximumCost((long) menu.getCost() * 2L);
        } else if ((right.is(Items.DIAMOND) || right.is(Items.NETHERITE_INGOT))
                && menu.repairItemCountCost > 0) {
            // Unit repair consumes three times the material, but the experience cost is
            // deliberately left untouched.
            menu.repairItemCountCost *= 3;
        }
    }
}

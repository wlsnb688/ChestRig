package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.inventory.RigContentsTooltip;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Adds the chest rig contents preview to the chestplate tooltip image, the same way a
 * shulker box shows the items it stores when hovered in the inventory.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackTooltipMixin {
    @Inject(method = "getTooltipImage", at = @At("RETURN"), cancellable = true)
    private void arrowslot$chestRigContents(CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!RigContentsTooltip.hasContents(stack)) {
            return;
        }

        Optional<TooltipComponent> vanilla = cir.getReturnValue();
        if (vanilla != null && vanilla.isPresent()) {
            return;
        }
        if (stack.has(DataComponents.HIDE_TOOLTIP) || stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)) {
            return;
        }

        cir.setReturnValue(Optional.of(RigContentsTooltip.of(stack)));
    }
}

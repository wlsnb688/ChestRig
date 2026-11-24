package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.ammo.AmmoSelection;
import com.wlsnb.arrowslot.ammo.AmmoSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws crossbow magazine count at the icon's top-right and the selected ammo
 * stack count at the vanilla stack-count position.
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Inject(
            method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At("RETURN")
    )
    private void arrowslot$crossbowAmmoCounts(Font font, ItemStack stack, int x, int y,
                                              String text, CallbackInfo ci) {
        if (stack.isEmpty()) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        GuiGraphics graphics = (GuiGraphics) (Object) this;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 200.0F);
        if (stack.getItem() instanceof CrossbowItem) {
            ChargedProjectiles charged = stack.get(DataComponents.CHARGED_PROJECTILES);
            int loaded = charged == null ? 0 : charged.getItems().size();
            if (loaded > 0) {
                String count = Integer.toString(loaded);
                graphics.drawString(font, count, x + 17 - font.width(count), y + 1, 0xFFFFFF, true);
            }
        }

        if (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem) {
            int selected = AmmoSelection.selected(player, stack);
            if (selected >= 0) {
                ItemStack ammo = AmmoSlots.get(player, selected);
                if (!ammo.isEmpty()) {
                    String count = Integer.toString(ammo.getCount());
                    graphics.drawString(font, count, x + 17 - font.width(count), y + 7, 0xFFFFFF, true);
                }
            }
        }
        graphics.pose().popPose();
    }
}

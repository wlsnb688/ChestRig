package com.wlsnb.arrowslot.mixin;

import com.wlsnb.arrowslot.inventory.ChestRig;
import com.wlsnb.arrowslot.inventory.ChestRigContainer;
import com.wlsnb.arrowslot.inventory.ChestRigSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void arrowslot$addChestRigRow(Inventory inventory, boolean active, Player owner, CallbackInfo ci) {
        InventoryMenu menu = (InventoryMenu) (Object) this;

        // The row is docked at the bottom of the screen and drawn as its own vanilla-styled
        // panel. The slots use screen-relative coordinates; the client screens refresh them
        // every frame from the real window height.
        ChestRigContainer container = new ChestRigContainer(owner);
        for (int i = 0; i < ChestRig.DISPLAY_SLOTS; i++) {
            menu.addSlot(new ChestRigSlot(container, i, ChestRigSlot.ROW_X + i * ChestRigSlot.SLOT_STEP, 0));
        }
    }
}

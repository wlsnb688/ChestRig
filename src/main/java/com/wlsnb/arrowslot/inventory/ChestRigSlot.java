package com.wlsnb.arrowslot.inventory;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * One of the nine chest rig row slots.
 *
 * <p>The row is docked at the bottom of the screen and drawn as its own panel. Its slots use
 * screen-relative coordinates that are refreshed from the client screens every frame, so the
 * hit boxes stay aligned with the panel even though the panel is far outside the vanilla
 * inventory background.</p>
 */
public final class ChestRigSlot extends Slot {
    /** Left edge of the first slot, in screen coordinates. */
    public static final int ROW_X = 8;
    public static final int SLOT_STEP = 18;

    private final int displayIndex;

    public ChestRigSlot(ChestRigContainer container, int displayIndex, int x, int y) {
        super(container, displayIndex, x, y);
        this.displayIndex = displayIndex;
    }

    /** Sets the slot position in screen-relative coordinates. */
    public void setScreenPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private ChestRigContainer rig() {
        return (ChestRigContainer) this.container;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return rig().mayPlace(displayIndex, stack);
    }

    @Override
    public boolean isActive() {
        return rig().isActive(displayIndex);
    }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return rig().emptyIcon(displayIndex);
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }
}

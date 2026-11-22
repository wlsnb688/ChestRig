package com.wlsnb.arrowslot.inventory;

import com.mojang.datafixers.util.Pair;
import com.wlsnb.arrowslot.registry.ModDataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * A view over a chest rig's storage array.
 *
 * <p>The player inventory uses a player-bound view of the equipped chestplate. Other menus
 * bind a view to an explicit chestplate stack. The nine-slot inventory row keeps the
 * display order weapons, arrows, food, universal; the menu container uses storage order.</p>
 *
 * <p>Vanilla menu code mutates the stack returned by {@link #getItem(int)} and then calls
 * {@link #setChanged()}. The source array below makes that contract work while the actual
 * data stays in the data component on the chestplate stack.</p>
 */
public final class ChestRigContainer implements Container {
    private final Player player;
    private final ItemStack boundStack;
    private final int boundStorageIndex;
    private final ItemStack[] source;
    private ItemStack cachedChestplate = ItemStack.EMPTY;
    private ItemContainerContents cachedContents;
    private boolean cacheLoaded;

    /** A nine-slot view of the chestplate currently equipped by the player. */
    public ChestRigContainer(Player player) {
        this.player = player;
        this.boundStack = null;
        this.boundStorageIndex = -1;
        this.source = new ItemStack[ChestRig.DISPLAY_SLOTS];
    }

    /** A single-slot view of the given storage index of an explicit chestplate. */
    public ChestRigContainer(int storageIndex, ItemStack stack) {
        this.player = null;
        this.boundStack = stack;
        this.boundStorageIndex = storageIndex;
        this.source = new ItemStack[] {ItemStack.EMPTY};
        this.ensureCache();
    }

    private ItemStack chestplate() {
        return this.boundStack != null ? this.boundStack : ChestRig.getChestplate(this.player);
    }

    private int searchIndex(int slot) {
        if (this.boundStack != null) {
            return slot == 0 ? this.boundStorageIndex : -1;
        }
        return ChestRig.storageIndex(chestplate(), slot);
    }

    public int storageIndex(int displayIndex) {
        return searchIndex(displayIndex);
    }

    public boolean isActive(int displayIndex) {
        return searchIndex(displayIndex) >= 0;
    }

    public boolean mayPlace(int displayIndex, ItemStack stack) {
        int storageIndex = searchIndex(displayIndex);
        return storageIndex >= 0 && ChestRig.mayPlace(chestplate(), storageIndex, stack);
    }

    public Pair<ResourceLocation, ResourceLocation> emptyIcon(int displayIndex) {
        int storageIndex = searchIndex(displayIndex);
        return storageIndex < 0 ? null : ChestRig.emptyIcon(storageIndex);
    }

    private void ensureCache() {
        ItemStack chestplate = chestplate();
        if (this.boundStack == null) {
            ChestRig.normalize(chestplate);
        }
        ItemContainerContents contents = chestplate.isEmpty() ? null : chestplate.get(ModDataComponents.RIG_CONTENTS.get());
        if (this.cacheLoaded && chestplate == this.cachedChestplate && contents == this.cachedContents) {
            return;
        }

        this.cachedChestplate = chestplate;
        this.cachedContents = contents;
        if (this.boundStack != null) {
            this.source[0] = this.boundStorageIndex < 0 ? ItemStack.EMPTY : ChestRig.get(chestplate, this.boundStorageIndex);
        } else {
            for (int i = 0; i < this.source.length; i++) {
                int storageIndex = ChestRig.storageIndex(chestplate, i);
                this.source[i] = storageIndex < 0 ? ItemStack.EMPTY : ChestRig.get(chestplate, storageIndex);
            }
        }
        this.cacheLoaded = true;
    }

    private void writeCache() {
        this.ensureCache();
        ItemStack chestplate = chestplate();
        if (chestplate.isEmpty() || chestplate != this.cachedChestplate) {
            this.cacheLoaded = false;
            return;
        }

        if (this.boundStack != null) {
            if (this.boundStorageIndex >= 0) {
                ChestRig.set(chestplate, this.boundStorageIndex, this.source[0]);
            }
            return;
        }

        for (int i = 0; i < this.source.length; i++) {
            int storageIndex = ChestRig.storageIndex(chestplate, i);
            if (storageIndex >= 0) {
                ChestRig.set(chestplate, storageIndex, this.source[i]);
            }
        }
    }

    @Override
    public int getContainerSize() {
        return this.source.length;
    }

    @Override
    public boolean isEmpty() {
        this.ensureCache();
        for (ItemStack stack : this.source) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= this.source.length) {
            return ItemStack.EMPTY;
        }
        this.ensureCache();
        return this.source[slot];
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        if (slot < 0 || slot >= this.source.length) {
            return ItemStack.EMPTY;
        }
        this.ensureCache();

        ItemStack current = this.source[slot];
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack taken = current.split(count);
        this.writeCache();
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= this.source.length) {
            return ItemStack.EMPTY;
        }
        this.ensureCache();

        ItemStack current = this.source[slot];
        this.source[slot] = ItemStack.EMPTY;
        this.writeCache();
        return current;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= this.source.length) {
            return;
        }
        this.ensureCache();
        this.source[slot] = stack == null ? ItemStack.EMPTY : stack;
        this.writeCache();
    }

    @Override
    public void clearContent() {
        this.ensureCache();
        for (int i = 0; i < this.source.length; i++) {
            this.source[i] = ItemStack.EMPTY;
        }
        this.writeCache();
    }

    @Override
    public void setChanged() {
        this.writeCache();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    /** Storage contents of a chestplate, in storage order, for tooltip previews. */
    public static NonNullList<ItemStack> previewContents(ItemStack chestplate) {
        return ChestRig.copyContents(chestplate);
    }

    /** The highest storage index that is visible for this chestplate material. */
    public static int lastVisibleStorageIndex(ItemStack chestplate) {
        return ChestRig.visibleStorageCount(chestplate) - 1;
    }
}

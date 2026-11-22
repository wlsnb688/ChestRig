package com.wlsnb.arrowslot.compat;

import com.wlsnb.arrowslot.ArrowSlotMod;
import com.wlsnb.arrowslot.inventory.ChestRig;
import com.wlsnb.arrowslot.inventory.ChestRigMaterial;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds the chest-rig arrow and food summary to the Jade item label of a chestplate item
 * entity. The arrow group is left-aligned and the food group is right-aligned on the same
 * line. This provider is only registered when Jade Addons is installed.
 */
public final class ChestRigJadeProvider implements IEntityComponentProvider {
    public static final ChestRigJadeProvider INSTANCE = new ChestRigJadeProvider();

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, "chest_rig");
    private static final ResourceLocation EMPTY_ARROW =
            ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, "hud/empty_arrow");
    private static final ResourceLocation EMPTY_BREAD =
            ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, "hud/empty_bread");
    private static final ResourceLocation EMPTY_DRUMSTICK =
            ResourceLocation.fromNamespaceAndPath(ArrowSlotMod.MOD_ID, "hud/empty_drumstick");

    private ChestRigJadeProvider() {
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof ItemEntity entity)) {
            return;
        }

        ItemStack chestplate = entity.getItem();
        if (ChestRigMaterial.of(chestplate) == ChestRigMaterial.NONE) {
            return;
        }

        int arrows = ChestRig.totalArrows(chestplate);
        int foodItems = ChestRig.totalFoodItems(chestplate);
        boolean appleSkin = ModList.get().isLoaded("appleskin");
        int foodValue = appleSkin ? ChestRig.totalFoodNutrition(chestplate) : foodItems;

        IElementHelper helper = IElementHelper.get();
        List<IElement> line = new ArrayList<>();

        // Left group: normal arrow when the rig has arrows, hollow arrow otherwise.
        line.add(arrows > 0
                ? helper.item(new ItemStack(Items.ARROW))
                : helper.sprite(EMPTY_ARROW, 18, 18));
        line.add(helper.spacer(2, 0));
        line.add(helper.text(Component.literal(Integer.toString(arrows))));

        // Right group: total nutrition with AppleSkin, otherwise the food item count.
        // The icon matches the displayed unit and is hollow when the rig has no food.
        ResourceLocation emptyFood = appleSkin ? EMPTY_DRUMSTICK : EMPTY_BREAD;
        ItemStack foodIcon = appleSkin ? new ItemStack(Items.COOKED_CHICKEN) : new ItemStack(Items.BREAD);
        line.add((foodItems > 0
                ? helper.item(foodIcon)
                : helper.sprite(emptyFood, 18, 18)).align(IElement.Align.RIGHT));
        line.add(helper.spacer(2, 0).align(IElement.Align.RIGHT));
        line.add(helper.text(Component.literal(Integer.toString(foodValue))).align(IElement.Align.RIGHT));

        tooltip.add(line);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}

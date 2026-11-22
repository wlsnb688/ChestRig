package com.wlsnb.arrowslot.registry;

import com.wlsnb.arrowslot.ArrowSlotMod;
import com.wlsnb.arrowslot.recipe.GearRepairRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ArrowSlotMod.MOD_ID);

    public static final Supplier<RecipeSerializer<GearRepairRecipe>> GEAR_REPAIR =
            RECIPE_SERIALIZERS.register("gear_repair",
                    () -> new SimpleCraftingRecipeSerializer<>(GearRepairRecipe::new));

    private ModRecipes() {
    }
}

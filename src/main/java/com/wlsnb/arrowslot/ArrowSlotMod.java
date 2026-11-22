package com.wlsnb.arrowslot;

import com.wlsnb.arrowslot.network.ModNetwork;
import com.wlsnb.arrowslot.registry.ModDataComponents;
import com.wlsnb.arrowslot.registry.ModRecipes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ArrowSlotMod.MOD_ID)
public final class ArrowSlotMod {
    public static final String MOD_ID = "arrowslot";

    public ArrowSlotMod(IEventBus modBus) {
        ModDataComponents.DATA_COMPONENTS.register(modBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modBus);
        modBus.addListener(ModNetwork::register);
    }
}

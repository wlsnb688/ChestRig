package com.wlsnb.arrowslot.compat;

import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.fml.ModList;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Optional Jade integration. Jade discovers this class through its plugin annotation, so
 * the mod still loads normally when Jade is not installed.
 */
@WailaPlugin
public final class JadeIntegration implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // The two counters are a Jade Addons feature. Without Addons the base Jade item
        // tooltip stays untouched.
        if (!ModList.get().isLoaded("jadeaddons")) {
            return;
        }
        registration.registerEntityComponent(ChestRigJadeProvider.INSTANCE, ItemEntity.class);
    }
}

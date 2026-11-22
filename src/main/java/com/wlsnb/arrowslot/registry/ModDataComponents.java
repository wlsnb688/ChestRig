package com.wlsnb.arrowslot.registry;

import com.mojang.serialization.Codec;
import com.wlsnb.arrowslot.ArrowSlotMod;
import com.wlsnb.arrowslot.component.TemporaryBinding;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ArrowSlotMod.MOD_ID);

    /**
     * A stable, synced identity for a particular bow/crossbow stack. It is only used as a
     * transient map key while the weapon is in the player's inventory; it is never used to
     * persist a selection across sessions.
     */
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> WEAPON_ID =
            DATA_COMPONENTS.registerComponentType("weapon_id", builder -> builder
                    .persistent(UUID_CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC)
            );

    /**
     * Fixed eleven-slot category storage carried by a chestplate. The item component keeps
     * the contents with the chestplate when it is removed, dropped, or upgraded.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> RIG_CONTENTS =
            DATA_COMPONENTS.registerComponentType("rig_contents", builder -> builder
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC)
            );

    /**
     * Only present on broken armor that received the sixty-second respawn binding curse.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TemporaryBinding>> TEMPORARY_BINDING =
            DATA_COMPONENTS.registerComponentType("temporary_binding", builder -> builder
                    .persistent(TemporaryBinding.CODEC)
                    .networkSynchronized(TemporaryBinding.STREAM_CODEC)
            );

    /**
     * Transient marker placed on an ammo copy returned from a chest-rig arrow slot. The
     * projectile code needs it to consume the real chest-rig stack instead of the copy.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RIG_AMMO =
            DATA_COMPONENTS.registerComponentType("rig_ammo", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
            );

    private ModDataComponents() {
    }
}

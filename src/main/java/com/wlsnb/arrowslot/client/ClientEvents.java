package com.wlsnb.arrowslot.client;

import com.wlsnb.arrowslot.ArrowSlotMod;
import com.wlsnb.arrowslot.ammo.AmmoSlots;
import com.wlsnb.arrowslot.client.hud.HudConfigScreen;
import com.wlsnb.arrowslot.equipment.AutoEatVisualState;
import com.wlsnb.arrowslot.equipment.RigWeaponState;
import com.wlsnb.arrowslot.network.RigWeaponActionPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = ArrowSlotMod.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }

        double delta = event.getScrollDeltaY();
        if (delta == 0.0D) {
            return;
        }

        // Arrow switching keeps priority while the ammo HUD is active.
        if (ClientAmmoState.isActuallySneaking(minecraft.player)
                && !AmmoSlots.getHeldWeapon(minecraft.player).isEmpty()) {
            event.setCanceled(true);
            ClientAmmoState.scroll(delta > 0.0D ? -1 : 1);
            return;
        }

        if (RigWeaponClientState.isGMode() && !RigWeaponState.isPending(minecraft.player)) {
            event.setCanceled(true);
            PacketDistributor.sendToServer(new RigWeaponActionPayload(
                    RigWeaponActionPayload.CYCLE,
                    delta > 0.0D ? -1 : 1
            ));
            return;
        }

        if (RigWeaponClientState.isActive()) {
            // After a thrown trident the slot stays selected until the hotbar actually
            // changes, so vanilla scrolling ends the pending empty selection.
            return;
        }
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_MIDDLE || event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        if (ClientAmmoState.resetSelection()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        KeyBindings.migrateLegacyDefaults();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            RigWeaponClientState.reset();
        } else {
            AutoEatVisualState.tick(minecraft.player);
        }
        while (KeyBindings.PREVIOUS.consumeClick()) {
            ClientAmmoState.quickSwitch(-1);
        }
        while (KeyBindings.NEXT.consumeClick()) {
            ClientAmmoState.quickSwitch(1);
        }
        while (KeyBindings.RIG_WEAPON.consumeClick()) {
            PacketDistributor.sendToServer(new RigWeaponActionPayload(
                    RigWeaponActionPayload.SELECT_G,
                    0
            ));
        }
        while (KeyBindings.HUD_CONFIG.consumeClick()) {
            minecraft.setScreen(new HudConfigScreen());
        }
        while (KeyBindings.RELOAD_CROSSBOW.consumeClick()) {
            PacketDistributor.sendToServer(new RigWeaponActionPayload(
                    RigWeaponActionPayload.RELOAD_CROSSBOW,
                    0
            ));
        }
        SneakModeController.tick(minecraft);
        ClientAmmoState.tick();
    }
}
package com.wlsnb.arrowslot.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.wlsnb.arrowslot.ammo.AmmoSlots;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

/**
 * While a bow/crossbow is held, the vanilla toggle-crouch setting is temporarily treated
 * as hold-crouch. The setting itself is never changed; only the current sneak key state is
 * cleared when the physical key is no longer held.
 */
public final class SneakModeController {
    private static boolean wasHoldingWeapon;
    private static boolean wasMainHandWeapon;

    private SneakModeController() {
    }

    public static void tick(Minecraft minecraft) {
        Player player = minecraft.player;
        if (player == null) {
            wasHoldingWeapon = false;
            wasMainHandWeapon = false;
            return;
        }

        boolean holdingWeapon = !AmmoSlots.getHeldWeapon(player).isEmpty();
        boolean mainHandWeapon = AmmoSlots.isWeapon(player.getMainHandItem());
        boolean toggleMode = minecraft.options.toggleCrouch().get();

        if (minecraft.screen != null || !toggleMode) {
            wasHoldingWeapon = holdingWeapon;
            wasMainHandWeapon = mainHandWeapon;
            return;
        }
        if (!holdingWeapon) {
            wasHoldingWeapon = false;
            wasMainHandWeapon = mainHandWeapon;
            return;
        }

        boolean sneaking = ClientAmmoState.isActuallySneaking(player);
        boolean mainHandTransition = !wasMainHandWeapon && mainHandWeapon;
        boolean sneakKeyOn = minecraft.options.keyShift.isDown;
        boolean physicalKeyDown = isPhysicalSneakDown(minecraft);
        boolean canGroundSneak = (!player.getAbilities().flying || player.onGround())
                && !player.isFallFlying()
                && !player.isSwimming();

        // Switching the main hand to a bow/crossbow while toggle-sneaking exits the sneak.
        if (sneaking && (mainHandTransition || !wasHoldingWeapon)) {
            stopSneaking(minecraft, player);
        } else if (sneakKeyOn && !physicalKeyDown && canGroundSneak) {
            // Emulate hold mode: releasing the physical key ends the toggled sneak state.
            stopSneaking(minecraft, player);
        }

        wasHoldingWeapon = true;
        wasMainHandWeapon = mainHandWeapon;
    }

    private static void stopSneaking(Minecraft minecraft, Player player) {
        // ToggleKeyMapping stores its toggled state in the inherited isDown field.
        minecraft.options.keyShift.isDown = false;
        minecraft.options.keyShift.setDown(false);
        player.setShiftKeyDown(false);
    }

    private static boolean isPhysicalSneakDown(Minecraft minecraft) {
        KeyMapping sneakKey = minecraft.options.keyShift;
        InputConstants.Key boundKey = sneakKey.getKey();
        long window = minecraft.getWindow().getWindow();

        if (boundKey.getType() == InputConstants.Type.KEYSYM) {
            return InputConstants.isKeyDown(window, boundKey.getValue());
        }
        if (boundKey.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(window, boundKey.getValue()) == GLFW.GLFW_PRESS;
        }
        return false;
    }
}

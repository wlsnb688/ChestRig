package com.wlsnb.arrowslot.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {
    public static final String CATEGORY = "key.categories.arrowslot";

    // GLFW button 4 (XBUTTON2) and button 5 (XBUTTON1). These are intentionally
    // swapped relative to the physical labels on many mice.
    public static final KeyMapping PREVIOUS = new KeyMapping(
            "key.arrowslot.previous",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_5,
            CATEGORY
    );

    public static final KeyMapping NEXT = new KeyMapping(
            "key.arrowslot.next",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_4,
            CATEGORY
    );

    public static final KeyMapping RIG_WEAPON = new KeyMapping(
            "key.arrowslot.rig_weapon",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );

    public static final KeyMapping HUD_CONFIG = new KeyMapping(
            "key.arrowslot.hud_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            CATEGORY
    );

    public static final KeyMapping RELOAD_CROSSBOW = new KeyMapping(
            "key.arrowslot.reload_crossbow",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            CATEGORY
    );

    private static boolean legacyDefaultsChecked;

    private KeyBindings() {
    }

    /**
     * Existing options.txt files still contain the old defaults (previous = XBUTTON1,
     * next = XBUTTON2). Swap exactly that pair once so players who never customized these
     * keys see the new defaults without losing custom bindings.
     */
    public static void migrateLegacyDefaults() {
        if (legacyDefaultsChecked) {
            return;
        }
        legacyDefaultsChecked = true;

        InputConstants.Key oldPrevious = InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_4);
        InputConstants.Key oldNext = InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_5);
        if (PREVIOUS.getKey().equals(oldPrevious) && NEXT.getKey().equals(oldNext)) {
            PREVIOUS.setKey(oldNext);
            NEXT.setKey(oldPrevious);
        }
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(PREVIOUS);
        event.register(NEXT);
        event.register(RIG_WEAPON);
        event.register(HUD_CONFIG);
        event.register(RELOAD_CROSSBOW);
    }
}
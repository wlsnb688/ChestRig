package com.wlsnb.arrowslot.client.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Client-side layout and visibility settings for the built-in HUD. The defaults follow
 * Inventory HUD+'s default arrangement: inventory at the bottom centre, effects at the
 * top left, and the equipment readout around the hotbar.
 */
public final class HudConfig {
    private static final int CONFIG_VERSION = 2;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve("arrowslot-hud.json");
    private static HudConfig instance;

    public int configVersion = CONFIG_VERSION;
    public boolean enabled = true;

    public boolean inventoryEnabled = true;
    public boolean inventoryMini = false;
    public boolean inventoryVertical = false;
    public boolean inventoryAnchored = true;
    public int inventoryX = 0;
    public int inventoryY = 150;
    public int inventoryAlpha = 0;

    public boolean potionEnabled = true;
    public boolean potionMini = false;
    public boolean potionHorizontal = false;
    public boolean potionLevels = false;
    public boolean potionShowHidden = true;
    public int potionX = 20;
    public int potionY = 20;
    public int potionGap = 0;
    public int potionAlpha = 100;

    public boolean equipmentEnabled = true;
    public boolean showArmor = true;
    public boolean showHeld = true;
    public boolean showDurability = true;
    public boolean showFreeSlots = true;
    public boolean showArrows = true;
    public boolean showEmpty = true;

    private HudConfig() {
    }

    public static HudConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static HudConfig load() {
        HudConfig config = new HudConfig();
        boolean migrated = false;
        try {
            if (Files.isRegularFile(PATH)) {
                String json = Files.readString(PATH, StandardCharsets.UTF_8);
                boolean legacy = !json.contains("\"configVersion\"");
                HudConfig loaded = GSON.fromJson(json, HudConfig.class);
                if (loaded != null) {
                    config = loaded;
                    if (legacy || config.configVersion < CONFIG_VERSION) {
                        // The first version stored a top-left panel layout. Reset it once
                        // rather than making every existing install keep the old layout.
                        config = new HudConfig();
                        migrated = true;
                    }
                }
            }
        } catch (Exception ignored) {
            // A broken config must never prevent the client from starting.
        }
        config.clamp();
        if (migrated) {
            config.save();
        }
        return config;
    }

    public void save() {
        clamp();
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    public void clamp() {
        inventoryX = clamp(inventoryX, -4096, 4096);
        inventoryY = clamp(inventoryY, 0, 4096);
        potionX = clamp(potionX, -4096, 4096);
        potionY = clamp(potionY, -4096, 4096);
        inventoryAlpha = clamp(inventoryAlpha, 0, 100);
        potionAlpha = clamp(potionAlpha, 0, 100);
        potionGap = clamp(potionGap, -5, 5);
    }

    public void resetPositions() {
        inventoryAnchored = true;
        inventoryX = 0;
        inventoryY = 150;
        potionX = 20;
        potionY = 20;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

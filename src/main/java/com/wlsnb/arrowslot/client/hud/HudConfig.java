package com.wlsnb.arrowslot.client.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Client-side layout and visibility settings for the built-in HUD. Inventory and the two
 * equipment panels are independently movable; the potion readout replaces the vanilla
 * top-right effect HUD and is therefore not movable.
 */
public final class HudConfig {
    private static final int CONFIG_VERSION = 7;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve("arrowslot-hud.json");
    private static HudConfig instance;

    public int configVersion = CONFIG_VERSION;
    public boolean enabled = true;

    public boolean inventoryEnabled = false;
    public boolean inventoryMini = false;
    public boolean inventoryVertical = false;
    public boolean inventoryAnchored = true;
    public int inventoryX = 0;
    public int inventoryY = 0;
    public int inventoryAlpha = 0;

    public boolean potionEnabled = true;

    public boolean showEmpty = true;
    public boolean showDurability = true;

    public boolean armorHudEnabled = true;
    public boolean handsHudEnabled = true;
    public boolean equipmentMini = false;

    public boolean leftInfoAnchored = true;
    public int leftInfoX = 0;
    public int leftInfoY = 0;
    public boolean rightInfoAnchored = true;
    public int rightInfoX = 0;
    public int rightInfoY = 0;

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
                    if (legacy || config.configVersion < 2) {
                        // The first version stored a top-left panel layout. Reset it once
                        // rather than making every existing install keep the old layout.
                        config = new HudConfig();
                        migrated = true;
                    } else {
                        if (config.configVersion < 3) {
                            // Inventory HUD is off by default from version 3 onward.
                            config.inventoryEnabled = false;
                        }
                        if (config.configVersion < 7) {
                            // Version 7 adds separate armor/hands switches, a shared mini
                            // mode, bottom-corner positions and the vanilla-style effects.
                            config.inventoryY = 0;
                            config.armorHudEnabled = true;
                            config.handsHudEnabled = true;
                            config.equipmentMini = false;
                            config.leftInfoAnchored = true;
                            config.leftInfoX = 0;
                            config.leftInfoY = 0;
                            config.rightInfoAnchored = true;
                            config.rightInfoX = 0;
                            config.rightInfoY = 0;
                            config.configVersion = 7;
                            migrated = true;
                        } else {
                            if (!json.contains("\"armorHudEnabled\"")) {
                                config.armorHudEnabled = true;
                            }
                            if (!json.contains("\"handsHudEnabled\"")) {
                                config.handsHudEnabled = true;
                            }
                            if (!json.contains("\"leftInfoAnchored\"")) {
                                config.leftInfoAnchored = true;
                            }
                            if (!json.contains("\"rightInfoAnchored\"")) {
                                config.rightInfoAnchored = true;
                            }
                        }
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
        inventoryY = clamp(inventoryY, -4096, 4096);
        inventoryAlpha = clamp(inventoryAlpha, 0, 100);
        leftInfoX = clamp(leftInfoX, -4096, 4096);
        leftInfoY = clamp(leftInfoY, -4096, 4096);
        rightInfoX = clamp(rightInfoX, -4096, 4096);
        rightInfoY = clamp(rightInfoY, -4096, 4096);
    }

    public void resetPositions() {
        inventoryAnchored = true;
        inventoryX = 0;
        inventoryY = 0;
        leftInfoAnchored = true;
        leftInfoX = 0;
        leftInfoY = 0;
        rightInfoAnchored = true;
        rightInfoX = 0;
        rightInfoY = 0;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

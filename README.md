# Chest Rig

[English](README.md) | [简体中文](README.zh-CN.md)

Minecraft Java Edition client/server mod that adds a chestplate-mounted **chest rig**.
The rig stores weapons, arrows and food, and its arrow slots are used by the
per-bow/crossbow ammo selector. Support for additional Minecraft versions and a Fabric
build is planned.

## Chest rig

A chestplate carries its own rig contents. Removing or dropping the chestplate does not
drop the contents separately; they stay inside the item. A diamond chestplate upgraded to
netherite keeps its contents, custom name and enchantments, just like vanilla.

The rig capacity depends on the chestplate material:

| Chestplate | Weapon slots | Arrow slots | Food slots | Universal slots |
| --- | ---: | ---: | ---: | ---: |
| Leather | 1 | 1 | 0 | 0 |
| Chainmail | 1 | 2 | 1 | 0 |
| Iron | 2 | 3 | 1 | 0 |
| Golden | 2 | 3 | 4 | 0 |
| Diamond | 2 | 4 | 3 | 0 |
| Netherite | 2 | 4 | 2 | 1 |

With a chestplate equipped, the survival inventory shows a nine-position chest-rig row
below the hotbar. The vanilla hotbar itself stays in place. Only the slots unlocked by
the current chestplate are active. Empty weapon, arrow and food slots show hollow sword,
arrow and bread icons; the universal slot has no icon.

Weapon slots accept bows, crossbows, swords, axes and tridents. Arrow slots accept
arrows (including modded arrows). Food slots accept items with a food component. The
universal slot accepts any item.

## Ammo selection

- While genuinely sneaking with a bow or crossbow, a HUD appears immediately.
- The HUD lists only slots that currently contain arrow-type items: chest-rig arrow slots
  first, then offhand, hotbar 1-9 and the main inventory.
- The selected slot has a red border. Chest-rig, offhand and hotbar slots are labelled.
- Scrolling cycles the selection. Scrolling is ignored unless the player is genuinely
  sneaking; elytra flight, creative flight and swimming never open the HUD or switch
  ammo.
- Middle-click resets the weapon to its default slot; this is disabled in creative mode.
- The HUD closes when sneaking stops. There is no two-second sneak timeout.
- Quick-switch keys work without sneaking and show the HUD for two seconds. Defaults are
  XBUTTON2 for previous and XBUTTON1 for next, and both are rebindable. Existing
  options.txt files with the old reversed defaults are migrated once.
- Toggle-sneak mode temporarily behaves like hold-sneak while a bow/crossbow is held.
  Switching the main hand to a bow/crossbow while toggle-sneaking exits the sneak. These
  rules do not apply in hold mode.
- A weapon keeps its selection while it remains in the player's inventory. Dropping it,
  putting it in a container, dying without keepInventory, or ending the session loses the
  transient choice. An invalid selection falls back to the default on the next shot.

## Hotbar-right chest-rig weapon slot

- A new slot to the right of the vanilla hotbar shows the active chest-rig weapon.
- Two keys select it: key 0 (a separate rebindable key, hotbar-10 style) and G
  (rebindable toggle).
- G selects the rig weapon and remembers the previous hotbar stack. Pressing G again
  restores the previous hotbar slot and puts the rig weapon back.
- While G selected, scroll or the offhand-swap key cycles through the rig's weapon slots.
  Scroll first checks the ammo HUD, so arrow switching keeps priority.
- While selected by key 0 or by scrolling onto the new slot, scroll and offhand swap use
  vanilla behavior; the controller restores the saved stack when the hotbar selection
  changes.
- The counters right of the slot show the total arrows in the rig's arrow slots on the
  left and total food nutrition (or food count without AppleSkin) on the right. Empty
  states use hollow arrow, bread or drumstick icons.

## Built-in HUD

Chest Rig now provides the HUD features it previously expected from Inventory HUD+:

- **Inventory:** 27 main slots (the hotbar is not duplicated), normal or mini scale, horizontal or vertical, bottom-centre by default with a transparent background.
- **Potion effects:** 60x24 rows with icon, level and either remaining time or a duration bar; normal or mini, vertical or horizontal, top-left by default.
- **Equipment:** helmet, chestplate, leggings, boots, main hand and offhand around the hotbar, with coloured percentages, empty-slot silhouettes, free inventory slots and arrow count.
- **Settings:** press `O` to open the HUD settings. The position editor moves the inventory and potion sections; the equipment readout follows the hotbar like Inventory HUD+'s default.
- **Persistence:** settings are stored in `config/arrowslot-hud.json`.

Inventory HUD+ is declared incompatible and must be removed before launching.

## Zero-durability gear

Tracked gear (armor, shields, tiered weapons/tools, bows, crossbows and tridents) stays in
the world at zero durability instead of disappearing.

- Broken armor and shields keep their enchantment entries but no longer provide armor
  value or non-negative enchantment effects, and cannot block attacks. Curses and Mending
  still work.
- Broken gear cannot be equipped again after being removed. It can be repaired with an
  anvil, a crafting table, or Mending.
- A broken shield is destroyed when it next tries to block an attack.
- The rig automatically eats food when hunger drops below three icons, until hunger is
  above five icons. Low-health auto-eating is enabled only when AppleSkin is installed.
- Arrows picked up while a matching non-full rig arrow slot exists are routed there.
  Ordinary drops go to the universal slot when the hotbar and offhand have no matching
  space; an empty universal slot accepts a Totem of Undying.
- Right-clicking arrows stores them in the rig. Right-clicking a sword or axe stores it,
  or swaps it with the currently displayed weapon slot when both weapon slots are full.
- Hovering a chestplate in the inventory shows its stored rig contents in the item tooltip, like a shulker box. This is a vanilla tooltip component and does not require any optional mod.
- The chest-rig weapon HUD to the right of the hotbar always shows two counters: total rig arrows on the left, and total food nutrition (or food count without AppleSkin) on the right. Hollow icons indicate an empty or missing category. This is implemented inside Chest Rig and does not require another HUD mod.
- With keepInventory off, broken armor is destroyed on death. A broken chestplate drops
  its stored contents instead of the chestplate.
- With keepInventory on, broken armor is kept, and immediately after respawn it receives
  a binding curse that lasts 60 seconds.
- Anvil repairs of broken gear cost double experience when combining two identical
  pieces. Repairing with diamonds or netherite ingots consumes three times the material
  but keeps the normal experience cost.
- Combining two identical tracked items in a crafting table repairs them to half of the
  summed remaining durability, rounded up.
- A damaged diamond item upgraded to netherite immediately returns to full durability.

## Building

Requirements:

- **JDK 21** (the build uses a Java 21 toolchain)
- **Gradle 9.9.1** installed and on your PATH; the wrapper launch scripts are not included
- First build downloads Minecraft, NeoForge and mappings, so it needs network access and takes a few minutes

Targets **Java 21**, **Minecraft 1.21.1**, **NeoForge 21.1.267**, **ModDevGradle 2.0.148**. The Java toolchain is selected by `java_version` in `gradle.properties`, so a future target can change it together with `minecraft_version`. Version-specific Java additions or resource overrides can live in `src/versions/<minecraft_version>/java` and `src/versions/<minecraft_version>/resources`; only the folder matching the current version is added to the source set.

### Command line

The project keeps the wrapper configuration (gradle/wrapper/gradle-wrapper.jar and
gradle-wrapper.properties, pinned to Gradle 9.9.1) but does **not** ship the
gradlew / gradlew.bat launch scripts. Use the gradle executable, which must be
**version 9.9.1**. From the project root:

    gradle build

Use clean to wipe build/ first, whenever stale output is suspected:

    gradle clean build

The result is build/libs/chest_rig-1.0.0.jar. Drop it into the mods folder of a
NeoForge 21.1.267 installation for Minecraft 1.21.1.

Useful variants:

| Command | Result |
| --- | --- |
| gradle build | Compiles, runs checks and packages the jar |
| gradle clean build | Same, after wiping build/ |
| gradle jar | Packages the jar without the check tasks |
| gradle --no-daemon build | One-off build that does not leave a Gradle daemon running |

To recreate the launch scripts at any time, run gradle wrapper --gradle-version 9.9.1
from the project root.

### IntelliJ IDEA

1. **File -> Open** the project folder. IDEA imports it as a Gradle project and runs neoForgeIdeSync, which generates the metadata template.
2. Open the **Gradle** tool window; the project is listed as ChestRig.
3. **build -> build** builds and packages the jar into build/libs.

By default IDEA uses its bundled Gradle distribution. To make it use the version pinned by
the wrapper, select **Use Gradle from -> gradle-wrapper.properties file** under
**Settings -> Build, Execution, Deployment -> Build Tools -> Gradle**.

If the Gradle tool window shows an empty or stale task list, use the **reload** button (the
circular arrow at the top left of the Gradle window) to resynchronise.

### Running the game in development

The ModDevGradle run tasks live in the Gradle tool window under
**Tasks -> mod development**:

- runClient - starts a development client with the mod loaded
- runServer - starts a dedicated development server (--nogui)

Right-click the task and choose **Run After Build**. A plain **Run** only launches the
game and skips the compile tasks, which makes FML fail with
"File ...\build\classes\java\main is not a valid mod file" because the class and resource
outputs do not exist yet.

The same runs are available from the command line:

    gradle runClient
    gradle runServer

Each run has its own versioned game directory. The layout is derived from
`run_root`, `mod_loader` and `minecraft_version` in `gradle.properties`:

| Path | Contents |
| --- | --- |
| `run/client/neoforge/<minecraft_version>/` | Client options, worlds, logs and client mods |
| `run/server/neoforge/<minecraft_version>/` | Server properties, worlds, logs and server mods |
| `run/client/neoforge/<minecraft_version>/mods/` | Dev-only client mod jars |
| `run/server/neoforge/<minecraft_version>/mods/` | Dev-only server mod jars |

Run `gradle printRunLayout` to print the resolved paths for the current
version. To test another Minecraft version, change `minecraft_version` and
provide a matching entry in `devModDownloadsByMinecraftVersion` in
`build.gradle`; the new run directory is created automatically and does not
share options, worlds, logs or mods with this one.

### Troubleshooting

- **"is not a valid mod file"** - build/classes or build/resources was never produced. Run a task through the Gradle tool window with **Run After Build**, or use gradle runClient from the command line.
- **Hotbar or chest-rig slots look misplaced after updating** - run gradle clean build and restart the game so stale slot classes and resources are removed.
- **"Parameter ... could not open ...clientRunVmArgs.txt"** - the run was started from a hand-made IntelliJ Application configuration. Use the ModDevGradle runClient task instead.
- **"Mod file ... not found" / missing resources** - run gradle clean build to wipe stale output.

## Notes

- Both the client and server must install the mod.
- AppleSkin is an optional client integration. Jade, JEI, Jade Addons and AppleSkin are `runtimeOnly` development dependencies, so both `runClient` and `runServer` load them in the dev environment. JEI also requires MezzConfig (and optionally MezzConfigGUI); the `downloadDevMods` Gradle task fetches those jars into the versioned `<gameDir>/mods` directories, reusing the old `run/mods` directory only as a one-time migration source. JEI and Jade Addons are not referenced by code yet, and none of these mods are shipped with Chest Rig.
- **Inventory HUD+ is declared incompatible.** Chest Rig now implements its own inventory, potion, equipment and rig-counter HUD; remove Inventory HUD+ before launching, otherwise FML reports a dependency conflict at startup.
- The current build targets NeoForge 1.21.1; additional Minecraft versions and a Fabric build are planned (see Roadmap).
- The mixin config is declared in META-INF/neoforge.mods.toml; an access transformer exposes AbstractContainerMenu#addSlot, the sneak key's isDown field and the inventory screen layout fields used by the chest-rig row.

## Roadmap

The current release targets NeoForge 1.21.1. Development of additional Minecraft
versions and a Fabric build is planned and will proceed as soon as possible.

## License

Copyright (C) 2026 wlsnb.

Chest Rig is licensed under the **GNU Affero General Public License, version 3 only**
(AGPL-3.0-only). The full license text is in [LICENSE](LICENSE).

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

The AGPL also covers network use: if you modify the mod and let players interact
with it remotely over a network, you must offer those users the Corresponding
Source of your modified version. Released builds are built from this repository,
which serves as the Corresponding Source:
<https://github.com/wlsnb688/ChestRig>.

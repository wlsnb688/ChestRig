# Chest Rig

[English](README.md) | [简体中文](README.zh-CN.md)

Chest Rig is a NeoForge mod for **Minecraft 1.21.1** that mounts a practical chest rig on
any vanilla chestplate. It adds weapon, arrow, food and universal storage, a per-weapon
ammo selector, a compact combat HUD, automatic eating, pickup routing and a set of
zero-durability gear rules. There is no separate chest-rig item: equip a chestplate and
the rig is part of it.

## Requirements

| Item | Version |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.267 or newer 21.1.x |
| Java | 21 |
| Optional | AppleSkin (low-health auto-eating and nutrition display) |

Chest Rig must be installed on **both the client and the server**. It is declared
incompatible with **Inventory HUD+**; remove that mod before launching.

## Installation

1. Install NeoForge 21.1.267 for Minecraft 1.21.1.
2. Put `chest_rig-1.21.1-neoforge-21.1.267-0.1.0-Alpha.jar` into the `mods` folder of the client and the server.
3. Remove Inventory HUD+ if it is present.
4. Start the game. The rig is part of a chestplate, so no extra item or recipe is needed.

## Getting started

Equip any vanilla chestplate. Open the survival inventory and a nine-position chest-rig
row appears at the bottom of the screen. The vanilla hotbar stays where it is. Only the
slots unlocked by the current chestplate material are active.

| Chestplate | Weapon slots | Arrow slots | Food slots | Universal slots |
| --- | ---: | ---: | ---: | ---: |
| Leather | 1 | 1 | 0 | 0 |
| Chainmail | 1 | 2 | 1 | 0 |
| Iron | 2 | 3 | 1 | 0 |
| Golden | 2 | 3 | 4 | 0 |
| Diamond | 2 | 4 | 3 | 0 |
| Netherite | 2 | 4 | 2 | 1 |

Slot rules:

- **Weapon slots** accept bows, crossbows, swords, axes and tridents.
- **Arrow slots** accept arrows, including modded arrows.
- **Food slots** accept items with a food component.
- **Universal slot** accepts anything; only netherite chestplates have one.

The contents belong to the chestplate. Removing or dropping the chestplate keeps the
contents inside the item. Upgrading diamond to netherite preserves the contents, name and
enchantments, and the surplus food layout is normalised automatically.

Hover a chestplate in the inventory to see its stored contents in the tooltip, like a
shulker box.

## Controls

| Action | Default |
| --- | --- |
| Select / release the chest-rig weapon | `G` |
| Cycle rig weapon slots while selected | Scroll wheel or offhand-swap key |
| Cycle ammo while sneaking with a bow/crossbow | Scroll wheel |
| Previous / next ammo slot | Mouse button 5 / mouse button 4 |
| Reset the ammo selection to its default | Middle click |
| Open HUD settings | `O` |
| Store arrows / sword or axe in the rig | Right-click the item |

All keys are rebindable in **Options -> Controls -> Chest Rig**.

## Ammo selection

- While genuinely sneaking with a bow or crossbow, the ammo HUD opens immediately.
- It lists slots that currently contain valid arrows: chest-rig arrow slots first, then
  offhand, hotbar 1-9 and the main inventory.
- Scrolling cycles the selected slot. Scrolling is ignored unless you are genuinely
  sneaking; elytra flight, creative flight and swimming do not open the HUD.
- Middle-click resets the weapon to its default slot. This is disabled in creative mode.
- Quick-switch keys work without sneaking and show the HUD for two seconds.
- Each weapon remembers its selection while it stays in the inventory. Dropping it,
  putting it in a container, dying without keepInventory, or ending the session clears the
  choice. An invalid choice falls back to the default on the next shot.
- Toggle-sneak mode behaves like hold-sneak while a bow or crossbow is held.

## Chest-rig weapon slot

The slot to the right of the hotbar shows the selected chest-rig weapon. It is fixed and
cannot be moved.

- `G` is the only key that selects the rig weapon. Pressing `G` again restores the
  previous hotbar slot and puts the weapon back.
- While selected, scrolling or the offhand-swap key cycles through the rig's weapon slots.
  Scrolling first checks the ammo HUD, so arrow switching keeps priority.
- The slot is hidden when no chestplate is equipped or no weapon is installed.
- If the only weapon was a trident that is currently thrown, the empty slot stays visible
  and selected until `G` is pressed or the hotbar selection changes successfully.
- The selected slot uses a thickened white selection frame, similar to the vanilla hotbar
  selection.
- The counters beside the slot show rig arrows on the left and food nutrition on the right
  (food count without AppleSkin). Hollow icons mean empty or missing.

### Throwing a trident

Throwing a trident from a rig weapon slot is handled specially:

- the trident is removed from the rig, so it cannot be duplicated;
- the thrown entity glows so it is easy to find;
- it remembers its owner, original slot and unique weapon ID;
- picking it up returns it to the original slot, or to another empty rig weapon slot if
  the original is occupied;
- the slot remains selected with an empty hand until `G` or a successful hotbar change.

Riptide and creative-mode throws keep their vanilla behaviour.

## Built-in HUD

Chest Rig includes its own HUD; Inventory HUD+ is not required or compatible.

- **Inventory:** disabled by default. When enabled it shows the 27 main inventory slots,
  normal or mini scale, horizontal or vertical. The anchored layout sits directly above
  the hotbar and never covers it.
- **Effects:** replaces the vanilla top-right effect icons in the same beneficial/harmful
  rows, adding duration and amplifier readouts. It is fixed in place.
- **Armor:** a bottom-left panel shows helmet, chestplate, leggings and boots with
  colour-coded durability percentages. It has its own switch.
- **Hands and counters:** a bottom-right panel shows main hand, offhand, rig arrows and
  food. It has its own switch.
- **Flat counter style:** both equipment panels use bare item icons and shadowed numbers,
  with no panel background or slots. Normal mode is one column of four rows; the shared
  mini mode uses two rows of columns with smaller text.
- **Stackable hands:** stackable main-hand/offhand items show their icon and the count
  beside it instead of the corner stack number.
- **Position editor:** press `O`, then **Edit HUD Positions**. The inventory, armor and
  hands/counter panels can be moved independently. Effects and the weapon slot are fixed.

Settings are stored in `config/arrowslot-hud.json`.

## Auto-eating and pickup routing

- The rig automatically eats food when hunger drops below three icons, until hunger is
  above five icons.
- Each auto-eat uses the vanilla eating animation and movement slowdown.
- Another auto-eat can start no sooner than 5 seconds after the previous one finishes.
- Auto-eaten golden apples block further automatic golden apples for 30 seconds. Enchanted
  golden apples block them for 60 seconds. Other food is still allowed during the lock.
- Low-health auto-eating is enabled only when AppleSkin is installed.
- Arrows and food picked up while a matching, non-full rig slot exists are routed there.
- Ordinary drops go to the universal slot only when the hotbar and offhand have no matching
  space. An empty universal slot accepts a Totem of Undying.

## Zero-durability gear

Tracked gear keeps working at zero durability instead of disappearing: armor, shields,
tiered weapons/tools, bows, crossbows and tridents.

- Broken armor and shields keep their enchantment entries but no longer provide armor
  value or non-negative enchantment effects, and cannot block attacks. Curses and Mending
  still work.
- Broken gear cannot be equipped again after removal. It can be repaired with an anvil, a
  crafting table, or Mending.
- A broken shield is destroyed when it next tries to block an attack.
- With keepInventory off, broken armor is destroyed on death. A broken chestplate drops its
  stored contents instead of the chestplate.
- With keepInventory on, broken armor is kept and receives a 60-second binding curse after
  respawn.
- Anvil repairs of broken gear cost double experience when combining two identical pieces.
  Repairing with diamonds or netherite ingots consumes three times the material but keeps
  the normal experience cost.
- Combining two identical tracked items in a crafting table repairs them to half of the
  summed remaining durability, rounded up.
- A damaged diamond item upgraded to netherite immediately returns to full durability.

## Configuration

The HUD layout is stored in `config/arrowslot-hud.json`. The file is migrated
automatically when the format changes. **Reset HUD Positions** in the `O` menu restores
the default layout.

## Building from source

### Requirements

- JDK 21 (the build uses a Java 21 toolchain)
- Gradle 9.9.1 on `PATH`; the wrapper launch scripts are not included
- Network access for the first build, which downloads Minecraft, NeoForge and mappings

The project targets **Minecraft 1.21.1**, **NeoForge 21.1.267**, **ModDevGradle 2.0.148**
and **Java 21**.

### Build

From the project root:

```
gradle build
```

The jar is written to:

```
build/libs/chest_rig-1.21.1-neoforge-21.1.267-0.1.0-Alpha.jar
```

If you suspect stale output:

```
gradle clean build
```

### Version variables

The build target and the jar name come from `gradle.properties`; no Java source or
metadata file hardcodes them.

| Property | Current value | Purpose |
| --- | --- | --- |
| `minecraft_version` | `1.21.1` | Minecraft target; also selects `run/<side>/<loader>/<version>/` and `src/versions/<version>/` |
| `mod_loader` | `neoforge` | Loader id used in the jar name and run path |
| `loader_version` | `21.1.267` | Exact loader build used for compilation and development runs |
| `minecraft_version_range` | `[1.21.1]` | Game versions declared compatible in mod metadata |
| `loader_version_range` | `[1,)` | Loader versions declared compatible in mod metadata |
| `mod_version` | `0.1.0-Alpha` | This branch's mod version; not a build-matrix dimension |

The jar name is:

```
chest_rig-<minecraft_version>-<mod_loader>-<loader_version>-<mod_version>.jar
```

To build for another game or loader target, change those properties (or pass them with
`-P`) and update the matching ranges, mappings, Java level and source overlay as needed:

```
gradle build -Pminecraft_version=1.21.4 -Ploader_version=21.4.123
```

A real port should live on its own branch. `-P` only changes the values this script
consumes; it does not make incompatible source or metadata compatible. The current build is
NeoForge-only: `mod_loader` labels the package and run directory, but compiling for a
different loader platform requires separate build scripts.

Mod versions are managed per git branch. Do not build several mod versions from one branch;
bump `mod_version` on the branch that releases it.

Useful variants:

| Command | Result |
| --- | --- |
| `gradle build` | Compile, run checks and package the jar |
| `gradle clean build` | Wipe `build/` first, then build |
| `gradle jar` | Package the jar without the check tasks |
| `gradle --no-daemon build` | One-off build without leaving a Gradle daemon |

For development, `gradle runClient` and `gradle runServer` start the versioned development
runs under `run/client/neoforge/1.21.1/` and `run/server/neoforge/1.21.1/`. In IntelliJ,
run the ModDevGradle tasks with **Run After Build**; a plain Run skips compilation.

## Signing the jar (optional)

NeoForge does not require mod jars to be signed. Signing is useful for integrity checks or
private distribution. Sign the **finished** jar and do not modify it afterwards.

1. Create a keystore once. Keep it private and back it up:

```
keytool -genkeypair -v -alias chest_rig -keyalg RSA -keysize 2048 -validity 3650 -keystore chest_rig-signing.jks -storepass CHANGE_ME -keypass CHANGE_ME -dname "CN=wlsnb, OU=Chest Rig, O=wlsnb, L=Unknown, ST=Unknown, C=CN"
```

2. Sign the built jar:

```
jarsigner -keystore chest_rig-signing.jks -storepass CHANGE_ME -keypass CHANGE_ME -sigalg SHA-256withRSA -digestalg SHA-256 build/libs/chest_rig-1.21.1-neoforge-21.1.267-0.1.0-Alpha.jar chest_rig
```

3. Verify:

```
jarsigner -verify -verbose -certs build/libs/chest_rig-1.21.1-neoforge-21.1.267-0.1.0-Alpha.jar
```

A self-signed certificate produces a warning; that is normal for a private mod. If you
rebuild the jar, sign the new jar again. Never commit the keystore or its passwords.

## Troubleshooting

- **The game reports Inventory HUD+ as incompatible:** remove Inventory HUD+ from the
  `mods` folder. Chest Rig implements its own HUD.
- **The HUD is missing during the loading screen:** this is intentional. HUD layers are
  suppressed while the loading overlay or level-loading screen is active.
- **The game says a class or mixin is missing after an update:** run `gradle clean build`
  and restart the game. New mixins are not reloaded by a world reload.
- **The HUD is in the wrong place:** press `O` and reset the HUD positions, or delete
  `config/arrowslot-hud.json` and restart.
- **A build fails with a Java version error:** make sure `java -version` reports 21 and that
  Gradle is running with Java 21.
- **The jar is not found:** run `gradle build`, not only `gradle compileJava`.

## Multiplayer

Install the mod on both sides. The server is authoritative for rig contents, ammo
selection, trident returns and auto-eating; the client only provides the HUD and the
client-side eating animation. Cheat-style clients cannot create rig contents through the
clientbound visual payloads.

## Roadmap

The current release targets NeoForge 1.21.1. Additional Minecraft versions and a Fabric
build are planned.

## AI assistance

Parts of the code, documentation and refactoring in this project were produced with
assistance from DeepSeek's coding agent (**deepseek-flash**) running in **DeepSeek
Harness**. The human maintainer directed the design, reviewed every change and remains
responsible for testing, releases and support. AI output was treated as a draft, not as an
authoritative source.

## License

Copyright (C) 2026 wlsnb.

Chest Rig is licensed under the **GNU Affero General Public License, version 3 only**
(AGPL-3.0-only). The full license text is in [LICENSE](LICENSE).

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

The AGPL also covers network use: if you modify the mod and let players interact with it
remotely over a network, you must offer those users the Corresponding Source of your
modified version. Released builds are built from this repository, which serves as the
Corresponding Source: <https://github.com/wlsnb688/ChestRig>.

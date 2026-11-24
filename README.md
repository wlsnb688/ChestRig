# Chest Rig

Chest Rig is a NeoForge mod for **Minecraft 1.21.1** that turns any vanilla chestplate into a compact chest rig. Weapon, arrow, food and universal storage are part of the chestplate, so there is no separate rig item to craft or carry.

## Requirements

| Item | Version |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.267 or newer 21.1.x |
| Java | 21 |
| Optional | AppleSkin: low-health auto-eating and nutrition display |

Install Chest Rig on **both the client and the server**. It is incompatible with **Inventory HUD+**; remove that mod before launching.

## Installation

1. Install NeoForge 21.1.267 for Minecraft 1.21.1.
2. Put `chest_rig-1.21.1-neoforge-21.1.267-0.1.0-Alpha.jar` in the `mods` folder on both sides.
3. Remove Inventory HUD+ if present.
4. Start the game. Equip a chestplate; no extra item or recipe is needed.

## Getting started

Open the survival inventory with a chestplate equipped. A nine-position rig row appears at the bottom; only slots unlocked by the chestplate material are active.

| Chestplate | Weapon | Arrow | Food | Universal |
| --- | ---: | ---: | ---: | ---: |
| Leather | 1 | 1 | 0 | 0 |
| Chainmail | 1 | 2 | 1 | 0 |
| Iron | 2 | 3 | 1 | 0 |
| Golden | 2 | 3 | 4 | 0 |
| Diamond | 2 | 4 | 3 | 0 |
| Netherite | 2 | 4 | 2 | 1 |

- **Weapon slots** accept bows, crossbows, swords, axes and tridents.
- **Arrow slots** accept arrows, including modded arrows.
- **Food slots** accept items with a food component.
- **Universal slots** accept anything; only netherite chestplates have one.

Rig contents stay inside the chestplate when it is removed or dropped. Hover a chestplate to see its contents in the tooltip. Upgrading diamond to netherite preserves contents, name and enchantments.

## Controls

| Action | Default |
| --- | --- |
| Select / release the rig weapon | `G` |
| Cycle rig weapon slots while selected | Scroll wheel or offhand-swap key |
| Cycle ammo while sneaking with a bow/crossbow | Scroll wheel |
| Previous / next ammo slot | Mouse button 5 / 4 |
| Reset ammo selection | Middle click |
| Open HUD settings | `O` |
| Manually reload the held crossbow | `B` |
| Store arrows, sword or axe in the rig | Right-click the item |

All keys can be rebound in **Options -> Controls -> Chest Rig**.

## Ammo and rig weapon

- Sneak with a bow or crossbow to open the ammo HUD. It lists valid arrow stacks in the rig first, then the offhand, hotbar and main inventory.
- Scroll to cycle ammo, use mouse button 5 / 4 for the previous / next slot, or middle-click to reset the weapon to its default slot.
- Each weapon remembers its ammo choice while it stays in the inventory. Dropping it, storing it in a container, or losing it on death without keepInventory clears the choice.
- Press `G` to select or release the rig weapon. While selected, scroll or use the offhand-swap key to cycle rig weapon slots.
- The slot beside the hotbar is hidden when no chestplate or rig weapon is available. It shows a white selection frame when active.
- A thrown rig trident is removed from the rig, glows on the ground, and returns to its original or another empty rig weapon slot when picked up. Riptide and creative throws keep vanilla behaviour.

### Crossbow magazine

- A crossbow can hold up to eight matching arrows. Right-click an empty crossbow or press `B` to start an automatic reload; it plays the charge animation and is not interrupted by attacking.
- After one charge delay, bolts load every 0.2 seconds, or every 0.1 seconds with Quick Charge, until full. Loading applies the vanilla movement slowdown and prevents sprinting.
- Each attack fires one loaded bolt; Multishot still adds its extra projectiles.
- Starting a reload while full or with a different arrow drops the loaded arrows. Damage interrupts a reload.
- The loaded count appears at the top-right of the crossbow icon, and the selected ammo count at the bottom-right of bow/crossbow icons.
- **Multi-Load** raises the cap to 16 at level I and 32 at level II. At level III it becomes **Instant Load**, which removes the magazine and fires directly; the two enchantments are exclusive.

## HUD

Chest Rig includes its own HUD; Inventory HUD+ is neither required nor compatible.

- The 27-slot inventory overview is **off by default**. It can be shown above the hotbar in normal or mini scale.
- The effects HUD replaces the vanilla top-right effect rows and adds duration and amplifier text.
- Armor and hands/counters are separate movable panels with independent switches.
- Press `O` to change HUD settings or edit panel positions. Effects and the weapon slot are fixed.
- HUD layers are hidden during the loading overlay and level-loading screens.

Settings are stored in `config/arrowslot-hud.json`.

## Auto-eating and pickup routing

- The rig eats from its food slots when hunger is at or below three icons, until hunger is at or above five icons.
- Auto-eating uses the vanilla eating animation and movement slowdown, with a 5-second cooldown between meals.
- Taking damage during that cooldown grants one immediate auto-eat attempt for non-golden food. Golden apples keep their own 30/60-second lock.
- With AppleSkin installed, low-health auto-eating starts at or below three hearts and ends when AppleSkin's estimated health or hunger is full. Without AppleSkin this rule is inactive and does not affect low-hunger auto-eating.
- With several foods available, auto-eating picks the best valid candidate: AppleSkin's estimated healing during low-health recovery, otherwise nutrition and saturation. Foods on item cooldown, locked golden apples and foods that cannot currently be eaten are skipped.
- Picked-up arrows and food go into matching non-full rig slots. Other drops use the universal slot only when the hotbar and offhand have no space.

### Leaves and apples

Breaking leaves without shears or Silk Touch doubles their drop chances. An apple dropped this way has a 20% chance to be a golden apple and a 2.5% chance to be an enchanted golden apple.

## Zero-durability gear

Armor, shields, tiered weapons/tools, bows, crossbows and tridents stay in the world at zero durability instead of disappearing.

- Broken armor and shields lose armor value and non-negative enchantment effects, and cannot block. Curses and Mending still work.
- Broken gear cannot be equipped again after removal. It can be repaired with an anvil, a crafting table, or Mending.
- A broken shield is destroyed when it next tries to block.
- With keepInventory off, broken armor is destroyed on death; a broken chestplate drops its stored contents. With keepInventory on, broken armor is kept and receives a 60-second binding curse.
- Anvil repair of broken gear costs double experience when combining identical pieces; diamond/netherite repair uses three times the material. Combining identical tracked items in a crafting table repairs them to half the summed durability.

## Configuration and troubleshooting

- **Inventory HUD+ conflict:** remove Inventory HUD+.
- **HUD missing during loading:** intentional; it returns when the world is ready.
- **HUD in the wrong place:** press `O` and reset the positions, or delete `config/arrowslot-hud.json`.
- **Missing class or mixin after an update:** run `gradle clean build` and restart the game.

## Building from source

- JDK 21
- Gradle 9.9.1 on `PATH`; no wrapper is included
- Network access on the first build

```
gradle build
```

The jar is written to `build/libs/chest_rig-1.21.1-neoforge-21.1.267-0.1.0-Alpha.jar`. Use `gradle clean build` for stale output. For development, `gradle runClient` and `gradle runServer` use the versioned directories under `run/`.

## Contributing code

Code, documentation and testing contributions are welcome; this is not a sponsorship or donation page.

- Open an issue before a large change.
- Keep pull requests focused and match the existing Java style.
- Register new mixins in `arrowslot.mixins.json`, keep client-only code on the client, and do not commit build output, `run/`, `_ref/`, keystores or credentials.
- Test on both a development client and a dedicated server.
- By submitting a contribution you agree that it is licensed under AGPL-3.0-only.

Source and issue tracker: <https://github.com/wlsnb688/ChestRig>.

## AI assistance

Parts of the code and documentation were drafted with DeepSeek's coding agent (**deepseek-flash**) in **DeepSeek Harness**. The human maintainer reviews all changes and remains responsible for testing and releases.

## License

Chest Rig is licensed under **AGPL-3.0-only**. See [LICENSE](LICENSE) for the full text. Released builds are built from this repository, which serves as the Corresponding Source: <https://github.com/wlsnb688/ChestRig>.

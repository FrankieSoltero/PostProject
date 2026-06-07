# Phase 2b-1 — Loot & Weapons Design

**Date:** 2026-06-07
**Project:** PostProject — *Covid, The Zombie Apocalypse*
**Status:** Approved design, ready for implementation planning
**Predecessors:** Phase 1 (rendering core) and Phase 2a (explore & fight), merged to `main` at `1cca2d9`.

---

## 1. Goal

Add a progression layer to the explore-and-fight game: **loot**. Weapons and
bandages are found in rooms and dropped by defeated zombies. The player carries
**one equipped weapon** that adds to their damage and is swapped when a better
one is found; bandages stock the existing inventory. The HUD shows the equipped
weapon and bandage count.

This is the first increment of Phase 2b (full gameplay parity). It is the
foundation that makes the later boss fights beatable.

### Decisions locked during brainstorming
- **First 2b increment:** Loot & Weapons (before Bosses & Win, and Save/Load).
- **Loot sources:** both — room finds (loot placed in specific rooms, collected
  when the room becomes safe) AND kill drops (a chance on each zombie defeat).
- **Weapon model:** one **equipped weapon** with a damage bonus; a found weapon
  is equipped only if its bonus beats the current one (swap-if-better).

### Deferred (later 2b increments, NOT in this spec)
- The 4 bosses (HiveMind, Zombie Nest, RatKing, Faucci) and boss combat. Boss
  rooms keep the Phase-2a "not yet" hint.
- The find-the-cure win sequence.
- Save / load on the new UI.
- Per-enemy animation choreography; sprite migration.

**Balance note:** with a single equipped weapon (not stacking upgrades) the
player's damage ceiling is modest. Tuning boss HP against that ceiling is an
explicit concern of Phase 2b-2, not this increment.

---

## 2. Weapon model (equipped, swap-if-better)

A small immutable **`Weapon`** value type: a `name()` and an integer `bonus()`
(damage added while equipped). Three tiers, with the bonus rolled within a range
on pickup, so tiers overlap (a fine Knife can beat a rusty Pistol):

| Tier          | Bonus range |
|---------------|-------------|
| Knife         | +5 to +20   |
| Pistol        | +15 to +35  |
| AssaultRifle  | +30 to +55  |

A loot factory rolls a random weapon: pick a tier (weighted or uniform — uniform
is fine), then roll the bonus uniformly within that tier's range.

The player starts **unarmed** (no weapon, bonus 0 — "fists"). Picking up a weapon:
- if its `bonus()` > the player's current weapon bonus → **equip it**, log
  `"You equip the <name> (+<bonus>)!"`;
- else → discard it, log `"You found a <name> (+<bonus>) but your current weapon is better."`

### Effective damage via polymorphism
- `Character.getEffectiveDamage()` returns `baseDamage` (the default; NPCs use
  this — they have no weapons).
- `Player` overrides `getEffectiveDamage()` to return `baseDamage + weaponBonus`.
- `Character.attack(...)` changes from using `this.baseDamage` to
  `getEffectiveDamage()`. This is behavior-preserving: `weaponBonus` starts at 0,
  and `getEffectiveDamage()` for an NPC equals `baseDamage`, so existing combat
  math and tests are unchanged until a weapon is equipped.
- Leveling (`levelingUp`) still scales only innate `baseDamage`, not the weapon
  bonus.

`Player` gains: `equipWeapon(Weapon w)` (the swap-if-better logic, writing to a
`MessageLog`), `getWeaponName()` (the equipped weapon's name, or a sentinel like
`"Fists"`/`"none"` when unarmed), and `getWeaponBonus()`.

### Retirement of the old weapon classes
The existing `Knife`/`Pistols`/`AssaultRifle` classes and the `Weapons`
interface implement a *stacking* `pickUpItem` (permanent `modifyDamage`) that
does not fit the equipped model. They are **replaced** by the `Weapon` value
type + loot factory. Their tests (`KnifeTest`, `PistolTest`, `ARTest`) are
replaced by tests for the new model. (If simplest, the old classes are deleted;
the plan decides delete-vs-repurpose, but the equipped model is the source of
truth.)

---

## 3. Loot sources

`Room` already has `hasWeapon`/`hasItem` int flags with `setWeapon`/`setItem`/
`removeWeapon`/`removeItem`/`hasWeapon()`/`hasItem()` — reused as-is.

### 3.1 Room finds (placed at load)
`GameState.loadHospital` places loot in specific rooms, ported from the
original's `level1()` placements (boss/cure specifics excluded):

- **Weapon rooms:** 1, 3, 6, 7, 8, 9, 11, 15, 16, 17, 20, 21.
- **Bandage (item) rooms:** 1, 3, 4, 7, 11, 17, 20, 21.

(A room may have both — e.g. room 1 has a weapon and a bandage.)

### 3.2 Collection — when a room becomes safe
A room's loot is collected the moment the room is **safe**: either the player
enters a room with no zombie, or the player clears the room's last zombie.
A new `GameState.collectRoomLoot()` is called at both points (in `tryEncounter`
when no combat starts, and in `onEnemyDefeated` when the room clears). It:
- if `currentRoom.hasWeapon() == 1`: roll a random weapon, `player.equipWeapon`,
  `currentRoom.removeWeapon()`;
- if `currentRoom.hasItem() == 1`: `player.obtain(new bandage(), log)`,
  `currentRoom.removeItem()`.

### 3.3 Kill drops
In `onEnemyDefeated`, after leveling and before the respawn/clear branch, roll a
drop: a modest chance to drop a **bandage** (added to inventory) and a separate
modest chance to drop a **weapon** (equip-if-better). Concrete chances are set in
the plan (e.g. ~1-in-3 bandage, ~1-in-4 weapon) — log each drop.

---

## 4. HUD additions

The viewport and log are unchanged. The two HUD rows grow to show gear:

```
+--------------------------------------------------+
| Hallway                       Lv.3   Bandages:2  |   room . level . bandages
| HP [|||||||.....] 92/150     DMG 117  AssaultRifle|   hp . effective dmg . weapon
+--------------------------------------------------+
```

- Row 0: room name (left); right side now shows `Lv.N` and `Bandages:<count>`.
- Row 1: HP bar + cur/max (left); right side shows `DMG <effective>` and the
  equipped weapon name (or "Fists"/"Unarmed" when none).
- `DMG` uses `player.getEffectiveDamage()` (base + weapon). Bandage count comes
  from the player's consumable inventory size.

Right-aligned text must not overrun the 60-column width or collide; the plan
positions each field explicitly.

---

## 5. Architecture (files)

- **`adventure_game.Weapon`** (new, model, Swing-free) — immutable value type:
  `name()`, `bonus()`. Plus a loot factory (a static method, e.g.
  `Weapon.randomWeapon()`) using `GameRandom.rand`.
- **`Character`** — add `getEffectiveDamage()` (returns `baseDamage`); change
  `attack` to use it.
- **`Player`** — `weaponBonus`/`weaponName` fields; override
  `getEffectiveDamage()`; `equipWeapon(Weapon, MessageLog)`; `getWeaponName()`;
  `getWeaponBonus()`; a `bandageCount()` (or reuse `items.size()` via a getter).
- **`GameState`** — `placeRoomLoot()` (called in `loadHospital`),
  `collectRoomLoot()` (called from `tryEncounter` on a safe room and from
  `onEnemyDefeated` on clear), kill-drop rolls in `onEnemyDefeated`.
- **`Scene`** — HUD shows effective DMG + weapon name + bandage count.
- **Retire** `items/Knife.java`, `items/Pistols.java`, `items/AssaultRifle.java`,
  `items/Weapons.java` and their tests, superseded by `Weapon`.

The model stays Swing-free; only `GameApp` touches Swing (unchanged this
increment beyond what `Scene` already drives).

---

## 6. Testing

All loot/weapon logic is Swing-free and unit-tested:

- **Weapon model:** `randomWeapon` returns a weapon whose bonus is within a
  known tier range; `Weapon` exposes name + bonus.
- **Equip logic:** equipping a stronger weapon raises `getEffectiveDamage()` and
  changes `getWeaponName()`; equipping a weaker weapon leaves both unchanged and
  logs the rejection; `getEffectiveDamage() == baseDamage + weaponBonus`.
- **Attack uses effective damage:** with a weapon equipped, an attack draws on
  `baseDamage + weaponBonus` (the existing randomized modifier still applies);
  with no weapon, behavior is identical to before (regression guard).
- **Room loot:** a room flagged with a weapon/bandage grants it on
  `collectRoomLoot()` and the room's flag is cleared (no double-collect).
- **Kill drops:** seeding `GameRandom` so a drop occurs, defeating an enemy adds
  a bandage / equips a weapon.
- **HUD:** `Scene` render shows the bandage count and (when armed) the weapon
  name and the effective DMG.

GUI (`GameApp`) is verified by a manual run + screenshot, as in prior phases.

---

## 7. Acceptance

Launching the app and playing:
- Explore and clear a supply/weapon room → a weapon is found and (if better)
  equipped; the HUD's `DMG` and weapon name update; a bandage room raises the
  HUD bandage count.
- Defeating zombies occasionally drops a bandage or weapon (logged), and a
  better weapon swaps in while a worse one is rejected.
- `DMG` in the HUD reflects base + equipped weapon; attacks hit harder once
  armed.
- All unit tests pass; no Swing imports in any model class (`GameApp` remains
  the only Swing user).

---

## 8. Risks & mitigations

- **Touching `Character.attack`** — risk of changing combat math. *Mitigation:*
  route through `getEffectiveDamage()` which defaults to `baseDamage`; keep the
  existing randomized modifier untouched; a regression test asserts unarmed
  behavior is unchanged.
- **Retiring the old weapon classes** — they are referenced by the retired
  reference `GameWindow.java.txt` (out of build) and old tests. *Mitigation:*
  delete the classes and their tests together; confirm nothing in `src` still
  imports them.
- **Double-collecting room loot** — `collectRoomLoot` could fire twice for one
  room. *Mitigation:* it removes the room's flag on collection, so a second call
  is a no-op; a test covers it.
- **HUD overflow** — added fields could overrun 60 columns. *Mitigation:*
  explicit column positions; a render test checks the fields are present.

---

## 9. Terminology

- **Equipped weapon** — the single `Weapon` the player currently wields; its
  `bonus()` adds to effective damage.
- **Effective damage** — `baseDamage + weaponBonus` (what `attack` uses).
- **Loot room** — a room flagged (`hasWeapon`/`hasItem`) to grant loot when safe.
- **Kill drop** — loot granted by chance when a zombie is defeated.

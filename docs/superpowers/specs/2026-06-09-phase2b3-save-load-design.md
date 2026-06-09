# Phase 2b-3 — Save / Load Design

**Date:** 2026-06-09
**Project:** PostProject — *Covid, The Zombie Apocalypse*
**Status:** Approved design, ready for implementation planning
**Predecessors:** Phase 1 (rendering core), Phase 2a (explore & fight), Phase 2b-1
(loot & weapons), Phase 2b-2 (bosses & the win) — all merged to `main`.

---

## 1. Goal

Let the player **save the current run to a named slot and load it back later**, on
the new Swing UI. The old reference saved only `playerLevel:roomNumber` to one file
— losing the equipped weapon, bandages, and which rooms were cleared. This
increment persists the **full run state** across **multiple named slots** in a
plain-text format, restoring a run faithfully.

This is the final Phase 2b increment; after it the gameplay loop is feature-complete
(explore → loot → fight zombies → bosses → win, with save/resume).

### Decisions locked during brainstorming (all user-approved)

- **Full run state** — player (level, HP, max HP, levelUpXp, base damage, equipped
  weapon, bandage count), the current room, and every room's progress flags
  (cleared/loot/cure). Not the minimal level+room of the reference.
- **Multiple named save slots** — one file per slot under `saves/`.
- **Save & Load buttons in Explore mode** — user-controlled. No autosave; no save
  during combat.
- **Plain-text `key:value` format** — written/parsed by a Swing-free helper,
  consistent with the codebase's text-file style (map, art) and easy to unit-test.
  Not Java serialization (which would couple model classes to a serial form and is
  brittle across changes).

### Deferred (NOT in this spec — YAGNI)

- Autosave / save-on-room-change.
- Delete-slot UI, rename-slot, overwrite confirmation (re-saving a name just
  overwrites).
- Saving mid-combat (save is Explore-only, so there is no live enemy to persist).
- Save-format versioning/migration (single current format; a malformed/old file is
  reported as an error, not migrated).

---

## 2. Architecture (files, all model code Swing-free)

- **`adventure_game.SaveData`** (new) — an immutable, Swing-free value object holding
  exactly what a save captures: the player fields, the current room number, and the
  25 rooms' flags. It is the boundary type between `GameState` and `SaveGame`.
- **`adventure_game.SaveGame`** (new) — Swing-free I/O helper:
  - `save(GameState state, Path file)` — writes `state.toSaveData()` as text.
  - `load(Path file)` — parses the text into a `SaveData`, returns
    `GameState.fromSave(saveData)`.
  - `listSlots(Path dir)` — returns the slot names (filenames without extension) of
    `*.txt` files in the saves directory, or an empty list if the dir is absent.
  - `slotFile(Path dir, String slot)` — resolves a slot name to its `Path`.
- **`adventure_game.GameState`** — gains `toSaveData()` (snapshot current state) and a
  static `fromSave(SaveData)` (rebuild a fresh map via `HospitalMap.load`, re-apply
  the saved per-room flags, install the restored player, set the current room, mode
  `EXPLORE`). Both live inside `GameState`, preserving encapsulation of its private
  fields (`player`, `rooms`, `currentRoom`, `enemy`, `mode`).
- **`adventure_game.Player`** — gains a static `fromSave(...)` factory; **`Character`**
  gains minimal restore support so a saved player round-trips exactly (see §4).
- **`adventure_game.GameApp`** (Swing) — two Explore-panel buttons wired to
  `JOptionPane` dialogs (see §5). The only Swing-touching change.

Boundary clarity: `SaveGame` knows the file format; `SaveData` is the dumb DTO;
`GameState` knows how to snapshot/rebuild itself; `GameApp` knows the dialogs. Each
unit is testable in isolation, and only `GameApp` imports Swing.

---

## 3. Save file format

One file per slot: `saves/<slotname>.txt`, UTF-8 **without BOM** (same convention as
art/map files; Java `Files.readAllLines` does not strip a BOM). Three line kinds,
`:`-delimited:

```
player:<name>:<health>:<maxHealth>:<level>:<levelUpXp>:<baseDamage>:<weaponBonus>:<weaponName>:<bandages>
room:<currentRoomNumber>
flags:<r0>;<r1>;<r2>; ... ;<r24>
```

- The `player` line carries the nine player fields in the fixed order shown.
- The `room` line is the current room number (== list index).
- The `flags` line has 25 semicolon-separated room records, each a comma-separated
  4-tuple `hasNPC,hasWeapon,hasItem,hasCure` using the raw flag ints
  (`hasNPC` ∈ {1,4,-1}; the others ∈ {1,-1}). Index in the list == room number.

Constraints: a slot name is restricted to safe characters (letters, digits, space,
`-`, `_`); the writer trims and rejects empty names. `weaponName` will not contain
`:` (current names are "Fists"/"Knife"/"Pistol"/"AssaultRifle"), so `:` stays a safe
delimiter; the parser splits the player line with a known field count to stay robust.

---

## 4. State capture & restore

### 4.1 Snapshot (`GameState.toSaveData`)
Reads via existing getters: `player.getName/getHealth/getMaxHealth/getLevel/
getlevelUpXp/getBaseDamage/getWeaponBonus/getWeaponName/bandageCount`,
`currentRoom.getRoomNumber()`, and for each room its `hasNPC()/hasWeapon()/hasItem()/
hasCure()`.

### 4.2 Restore (`GameState.fromSave`)
1. `rooms = HospitalMap.load(HOSPITAL_PATH)` — a fresh, fully-wired map.
2. For each room, apply the saved 4-tuple using the existing mutators: `hasNPC` →
   `setNPC()` (1) / `setBossNPC()` (4) / `removeNPC()` (-1); `hasWeapon` →
   `setWeapon()`/`removeWeapon()`; `hasItem` → `setItem()`/`removeItem()`; `hasCure`
   → `setRoomCure()` when 1.
3. `player = Player.fromSave(...)` from the saved fields.
4. `currentRoom = rooms.get(savedRoomNumber)`; `enemy = null`; `mode = EXPLORE`.

### 4.3 Player round-trip
The reference lost everything but level/room; we restore exactly. `Player.fromSave`
reconstructs all nine fields:
- `Player` static `fromSave(name, health, maxHealth, level, levelUpXp, baseDamage,
  weaponBonus, weaponName, bandages, MessageLog sink)`.
- It needs to set `Character`'s private vitals (`health`, `levelUpXp`) and a max
  health/level/baseDamage that differ from the plain constructor. So `Character`
  gains a small protected restore hook (e.g. `restoreVitals(int maxHealth, int
  health, int level, int levelUpXp, int baseDamage)`) and `Player` a
  `restoreWeapon(int bonus, String name)`; bandages are re-added via the existing
  `obtain(new bandage(), sink)` loop. These are the minimum new mutators; they are
  used only by the save/restore path.

---

## 5. UI integration (`GameApp`, Swing-only)

Two buttons added to the Explore panel (which currently holds the four move buttons
+ Create Bandage). The Explore panel grows to fit them (e.g. a 3×3 grid).

- **Save Game** → `JOptionPane.showInputDialog` prompts for a slot name → on a
  non-empty name, `SaveGame.save(state, SaveGame.slotFile(SAVES_DIR, name))` and log
  `"Game saved to slot '<name>'."`. Creates `saves/` if absent.
- **Load Game** → `SaveGame.listSlots(SAVES_DIR)`; if empty, log `"No saved games
  found."`; else `JOptionPane.showInputDialog` with a dropdown (selection values) of
  slot names → load the chosen file → **replace the live `GameState`** with the
  restored one and `redraw()`. Log `"Loaded slot '<name>'."`.

Both are guarded to Explore mode (consistent with the existing button guards). The
saves directory is `saves/` at the project root (sibling of `assets/`), created on
first save. Loading swaps the `GameState` reference the app renders; the engine/Scene
are unaffected since they read from whatever `GameState` they are handed each redraw.

Replacing the `GameState`: `GameApp` currently holds `state` as `final`. It becomes
non-final (or the app holds a small mutable holder) so Load can install the restored
instance; `redraw()` already reads the current `state`. The combat/explore/dead/won
button panels reference `state::method`; to keep those working after a swap, the
button actions call through the current `state` field (e.g. `() -> state.method()`),
not a captured snapshot — the plan adjusts any captured references accordingly.

---

## 6. Testing

All save/load logic is Swing-free and unit-tested (writing to a JUnit temp dir):

- **Round-trip:** build a `GameState`, mutate it into a representative mid-run state
  (level up the player, equip a weapon, add bandages, clear a regular room, collect
  a room's loot, defeat a boss room, move to a non-entrance room), `SaveGame.save`
  then `SaveGame.load`; assert the restored player fields all match and the restored
  rooms' flags match (including a cleared room == -1, a boss room still 4, the cure
  room still has cure, a collected-loot room cleared of its flags).
- **Current room restored:** the loaded state's current room equals the saved one.
- **`listSlots`:** writing two slot files into a temp dir makes `listSlots` return
  both names; an absent dir returns empty.
- **Malformed/missing file:** `load` on a missing or garbage file throws a clear,
  catchable exception (e.g. `IOException`/`IllegalArgumentException`) rather than
  corrupting state.
- **Player round-trip unit:** `Player.fromSave` reproduces health, maxHealth, level,
  levelUpXp, effective damage (base + weapon), weapon name, and bandage count.
- **No Swing imports** in `SaveData`/`SaveGame`/`GameState`/`Player`/`Character`.

GUI (the two buttons + `JOptionPane` dialogs and the live `GameState` swap) is
verified by a manual run, as in prior phases.

---

## 7. Acceptance

- In Explore, **Save Game** prompts for a name and writes `saves/<name>.txt`; the log
  confirms the save.
- **Load Game** lists saved slots, and choosing one restores the run: the player's
  level/HP/weapon/bandages, the current room, and which rooms are cleared/looted plus
  the cure/boss state all come back; play continues from there.
- Saving under an existing name overwrites that slot; loading with no saves logs a
  friendly message.
- All unit tests pass; no Swing imports outside `GameApp` (+ engine render).
- Save files are UTF-8 without BOM.

---

## 8. Risks & mitigations

- **Player state fidelity** (private `Character` vitals). *Mitigation:* a minimal,
  purpose-named restore hook on `Character` + `Player.fromSave`; a unit test asserts
  exact round-trip.
- **`GameState` swap on Load** breaking button wiring that captured the old instance.
  *Mitigation:* make `state` a mutable field and have button actions call through it;
  a manual run confirms post-load buttons act on the restored game.
- **Delimiter collision** (`:` in a name/weapon). *Mitigation:* restrict slot names
  to a safe charset; parse the player line by fixed field count; current weapon names
  are delimiter-free.
- **Malformed/foreign files** in `saves/`. *Mitigation:* `load` fails fast with a
  catchable exception; `GameApp` catches it and logs an error instead of crashing.
- **Missing `saves/` dir.** *Mitigation:* `listSlots` returns empty for an absent
  dir; `save` creates the dir.

---

## 9. Terminology

- **Slot** — a named save file `saves/<name>.txt`.
- **Run state** — the full snapshot: player + current room + all room flags.
- **Room flags** — `hasNPC`/`hasWeapon`/`hasItem`/`hasCure`, the per-room progress.
- **Snapshot / restore** — `GameState.toSaveData()` / `GameState.fromSave(...)`.

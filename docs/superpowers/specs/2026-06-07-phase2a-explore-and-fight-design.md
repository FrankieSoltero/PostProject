# Phase 2a — Explore & Fight Design

**Date:** 2026-06-07
**Project:** PostProject — *Covid, The Zombie Apocalypse*
**Status:** Approved design, ready for implementation planning
**Predecessor:** Phase 1 (rendering core + vertical slice), merged to `main` at `5827890`.

---

## 1. Goal

Make the game **explorable and fightable** on the Phase-1 rendering engine. The
player walks the full 25-room hospital with `N/S/E/W`, each room rendered with
its own ASCII backdrop; entering a room that contains zombies triggers an
in-viewport encounter; the player fights with **Attack / Defend / Create
Bandage / Use Bandage** and **levels up on kills**. A defeated zombie may be
followed by another in the same room (ported behavior) or the room clears.

This is the first Phase-2 increment. It deliberately stops short of full game
parity so it stays a coherent, testable, demoable milestone.

### Decisions locked during brainstorming
- **Scope:** the "explore & fight" playable loop (not full parity in one spec,
  not content-only).
- **Combat depth:** the original's core survival loop — Attack, Defend, Create
  Bandage, Use Bandage — plus leveling on kills. Weapons/loot deferred.
- **Room backdrops:** a unique hand-authored backdrop per room (25 total),
  produced by parallel subagents using the `ascii-art` / `room-and-map` skills.

### Deferred to later increments (NOT in this spec)
- Weapons and loot pickups.
- The 4 bosses (HiveMind, Faucci, Zombie Nest, RatKing) and boss combat.
- Save/load on the new UI.
- The find-the-cure win sequence.
- Sprite migration.

Boss rooms (10, 19, 22, 24) are **enterable but inert** this increment: entering
logs a deferral hint (e.g. *"Something massive stirs in the dark… not yet."*)
and does not start a fight.

---

## 2. Source of truth: the reference

Almost all gameplay logic already exists in `reference/GameWindow.java.txt` (the
original Swing game, parked out of the build in Phase 1). Phase 2a **ports** that
logic onto the new model/engine rather than inventing it. Relevant reference
pieces:

- **Movement:** per-direction handlers check `currentRoom.isRoom<Dir>Null()`,
  else set `currentRoom = currentRoom.get<Dir>Room()`; then run an
  encounter check.
- **Encounter:** `NPCSBattleSet` — when `currentRoom.hasNPC() == 1`, pick a
  random regular NPC (`rand.nextInt(12)` over the 12 regular zombies) and start
  combat. `hasNPC() == 4` is a boss room (deferred here).
- **Combat actions:** Attack (`player.attack(op)` then `op.takeTurn(player)`),
  Defend (`op.takeTurn(player)` then `player.defend(op)`), Use Bandage (consume
  then `op.takeTurn`), Create Bandage (`player.obtain(new bandage())` then
  `op.takeTurn`).
- **On victory** (reference's `windowClosed`): `player.levelModifier(...)`, a
  random chance (`rand.nextInt(2)+1`) that another zombie remains vs the room
  clears (`currentRoom.removeNPC()`). Loot branches here are **deferred**.
- **Roster & stats:** `createNPCS1` — Walker 100hp/15dmg, Creeper 250hp/18dmg,
  Sprinter 500hp/25dmg, with random per-instance levels and `levelingUp`
  scaling.
- **Room population:** `level1()` sets which rooms contain regular NPCs (rooms
  1–9, 11–18, 20, 21) and bosses (10, 19, 22, 24). We port the **regular** NPC
  placements; boss/item/weapon placements are deferred.

The existing model already supports the mechanics: `Character.attack/defend`,
the condition system (vulnerable/invincible/stunned), `levelModifier/levelingUp`,
`bandage.consume`, `Room` exits and `hasNPC`/`setNPC`/`removeNPC`.

---

## 3. Architecture — extend, don't rebuild

The Phase-1 model/engine/entry-point split holds. Phase 2a grows three seams.

### 3.1 `GameState` — modes + ported gameplay (Swing-free)

Add an explicit mode and the ported methods.

```
enum Mode { EXPLORE, COMBAT }
```

- `Mode getMode()` — current mode (start in EXPLORE; no auto-encounter at the
  Entrance, which has no zombie).
- `boolean move(Direction dir)` — if the current room's exit in `dir` is null,
  append a "You cannot go that way." message and return false; else set
  `currentRoom` to that room, append the room description, run
  `tryEncounter()`, return true.
- `void tryEncounter()` — if the new room contains a regular zombie
  (`currentRoom.hasNPC() == 1`), spawn a randomized zombie (type + level), set it
  as `enemy`, switch to COMBAT, and log its appearance. If it is a boss room
  (`hasNPC() == 4`), log the deferral hint and stay in EXPLORE. Otherwise stay in
  EXPLORE.
- Combat verbs (each resolves one exchange, then checks victory/defeat):
  - `void playerAttack()` (exists — generalize to use the current `enemy`).
  - `void playerDefend()` — enemy takes its turn, player defends (ported order).
  - `void useBandage()` — if the player has a bandage, consume it and the enemy
    takes its turn; else log "no bandages".
  - `void createBandage()` — `player.obtain(new bandage())`, enemy takes its
    turn.
- `void onEnemyDefeated()` — `player.levelModifier(...)`; random chance another
  zombie remains in the room (re-spawn `enemy`, stay COMBAT) vs room clears
  (`currentRoom.removeNPC()`, `enemy = null`, mode → EXPLORE).
- Defeat: when the player dies, switch to a terminal `DEAD`-style state (for this
  increment: log death and disable actions; full game-over/restart UI is part of
  a later increment — keep it minimal, e.g. a logged death message and ignore
  further combat input).

`GameState` also gains the **regular-NPC room population** ported from `level1()`
(rooms 1–9, 11–18, 20, 21 get `setNPC()`), called inside `loadHospital()`.

A small `Direction` enum (NORTH/SOUTH/EAST/WEST) replaces the per-direction
button duplication and gives `move` a clean signature.

### 3.2 Engine — render the current room and the typed enemy

- **`RoomArt` registry** (new, `adventure_game.engine`): loads
  `assets/art/rooms/room<N>.txt` by room number (lazy or eager), returning the
  matching `ArtAsset`; a blank/generic fallback if a file is missing. This
  replaces Scene's single hard-coded backdrop.
- **Enemy art by type:** a lookup `enemyArtFor(String typeName)` →
  `ArtAsset` for Walker/Creeper/Sprinter (and a fallback). Lives with the Scene
  or a small `EntityArt` helper.
- **`Scene` changes:** `render` selects the backdrop from `RoomArt` using
  `state.getCurrentRoom().getRoomNumber()`, and (in COMBAT) the enemy art from
  `enemyArtFor(enemy type)`. Still 1-v-1 in the viewport — no multi-entity
  layout needed yet. The HUD/log/viewport regions are unchanged.

### 3.3 `GameApp` — mode-based button sets

- **EXPLORE buttons:** `North / South / East / West` + `Create Bandage`.
- **COMBAT buttons:** `Attack / Defend / Use Bandage / Create Bandage`.
- The button row swaps based on `state.getMode()` after each action/redraw
  (e.g. a `CardLayout` or rebuilding the south panel). Movement buttons call
  `state.move(dir)`; combat buttons drive the combat verbs, each wrapped so the
  attack/defend animation burst plays then settles into the resolved state
  (reuse the Phase-1 `AnimationPlayer` pattern).
- The redraw cycle is unchanged: action → state update → optional animation →
  settle → `redraw()` (which composes HUD/viewport/log from `GameState`).

---

## 4. Content to author (via skills, fanned out)

Produced by parallel subagents, each given the relevant skill so output matches
the project standard; all `.txt` files **UTF-8 without BOM**.

- **3 zombie arts** (`ascii-art`, 5×5): Walker, Creeper, Sprinter — distinct
  silhouettes. Saved under `assets/art/`.
- **Animations** (`ascii-animation`), as `Animations` factory methods + unit
  tests: per zombie a **lunge/attack**, a **hit-flash**, and a
  **death/collapse**; plus a player **hit-flash** when damaged. (Player strike
  already exists.)
- **25 room backdrops** (`ascii-art` + `room-and-map`), under
  `assets/art/rooms/room<N>.txt`, each labeled with the room's name and matching
  its exits, sized to the viewport (≤13 rows, ≤60 cols).

The `room-and-map` skill is extended to cover the `assets/art/rooms/room<N>.txt`
location/convention.

---

## 5. Encounter & combat model (faithful port)

- Zombie **type and level randomized** per encounter, using the reference stats
  and `levelingUp` scaling.
- After a kill, a random chance (ported `rand.nextInt(2)+1`) that another zombie
  is present (re-encounter) vs the room clears.
- **Defend** uses the existing block→invincible+counter-buff / stumble→vulnerable
  logic in `Character.defend`.
- **Bandages** heal per existing `bandage.consume` (including the ≥maxHealth−20
  no-heal rule and the level-10 stronger heal).
- **Leveling** uses existing `levelModifier` (3 kills → level up → `levelingUp`
  stat growth).

No new combat math is introduced — only wiring of existing model behavior.

---

## 6. Testing & hardening

All gameplay logic is Swing-free and unit-tested:

- **Movement:** `move` follows each exit correctly and is blocked at walls
  (returns false, no room change) for the Entrance's null directions.
- **Encounter:** entering a populated room enters COMBAT with a non-null enemy;
  entering an empty/boss room stays EXPLORE.
- **Combat:** attack reduces enemy health and enemy retaliates; defend applies a
  condition; create/use bandage change inventory/health as expected; a fatal
  blow triggers `onEnemyDefeated` (level up; room clears or re-encounters).
- **Leveling:** three kills raise the player's level and stats.
- **Map-parser hardening** (final-review follow-up, justified now that
  navigation depends on every room's exits): a test asserting the map loads
  exactly 25 rooms and that a sample of exits — including reciprocal pairs — are
  wired as the map file specifies.
- **Content checks:** each art/backdrop file loads BOM-free at its standard
  size (a small asset-sanity test over `assets/art/` + `assets/art/rooms/`).

GUI classes (`GameApp` button-mode swap, `RenderPanel`) are verified by a manual
run + screenshot (as in Phase 1).

---

## 7. Acceptance

Launching the app (`./run.ps1`) and playing:

- Start at the **Hospital Entrance** in EXPLORE mode; `N/S/E/W` move between
  rooms and each room shows its **own backdrop**.
- Moving into a populated room starts a **COMBAT** encounter with a randomized
  zombie shown in the viewport; the button row switches to combat actions.
- **Attack / Defend / Use Bandage / Create Bandage** all work, with the
  attack/defend animation playing then settling; the HP bar and log update.
- Defeating a zombie **levels the player up** and either re-encounters another
  zombie or **clears the room** (mode returns to EXPLORE).
- Entering a **boss room** shows the deferral hint and does not start a fight.
- All unit tests pass; **no Swing imports** in any model class (`GameApp`
  remains the only Swing user in the `adventure_game` package).

---

## 8. Risks & mitigations

- **Porting drift** — re-wiring combat could change behavior. *Mitigation:* port
  method bodies faithfully from the reference; cover each verb with a unit test;
  keep existing legacy tests green.
- **Content volume (25 backdrops)** — tedium/throughput. *Mitigation:* fan out
  authoring across parallel subagents using the skills; an asset-sanity test
  guards size/BOM consistency.
- **Mode/UI desync** — button set not matching the mode. *Mitigation:* derive the
  visible button set purely from `state.getMode()` on every redraw; cover mode
  transitions with unit tests on `GameState`.
- **Boss-room dead-ends** — some regions sit behind boss rooms. *Mitigation:* the
  deferral hint keeps boss rooms passable to *enter* but does not block earlier
  exploration; full boss traversal is a later increment.

---

## 9. Terminology

- **Mode** — `EXPLORE` (navigating rooms) vs `COMBAT` (fighting one zombie);
  drives both gameplay branching and the visible button set.
- **Encounter** — the transition from EXPLORE to COMBAT when entering a populated
  room.
- **RoomArt registry** — maps a room number to its backdrop `ArtAsset`.
- **Regular zombie** — Walker/Creeper/Sprinter (non-boss).

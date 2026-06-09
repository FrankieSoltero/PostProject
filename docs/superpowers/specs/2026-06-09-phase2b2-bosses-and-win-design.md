# Phase 2b-2 — Bosses & the Win Design

**Date:** 2026-06-09
**Project:** PostProject — *Covid, The Zombie Apocalypse*
**Status:** Approved design, ready for implementation planning
**Predecessors:** Phase 1 (rendering core), Phase 2a (explore & fight), Phase 2b-1
(loot & weapons) — all merged to `main`.

---

## 1. Goal

Close out the core game loop: four **phased boss fights** and the **win
sequence**. Today the four boss rooms (10, 19, 22, 24) just log a "something
massive stirs… not yet" hint. This increment makes them real, escalating fights
that the leveled-and-armed player can win, and turns the final boss room into the
victory: find the cure, end the game on a dedicated win screen.

This is the gameplay-parity capstone of Phase 2b. After it, the loop is
complete: explore → loot → fight zombies → level up → beat four bosses → win.

### Decisions locked during brainstorming (all user-approved)

- **Full phased boss system** (the most ambitious option), not single-HP-bar
  bosses.
- **Phase trigger = HP thresholds + per-turn attack pattern.** Each boss has 3
  phases gated at 66% and 33% of max HP. Each phase is a fixed, *repeating*
  `Move[]` pattern that a per-boss turn counter cycles through. The pattern
  itself is the tell — the player learns it. (Deliberately **not** wind-up
  telegraphs.)
- **Shared move vocabulary + one signature per boss.** Shared: `STRIKE` (normal,
  1.0× effective damage) and `SLAM` (~1.8×, the beat to Defend on). Signature:
  - **HiveMind** = `SUMMON` — modeled as a 3-turn chip-damage threat, **not** a
    second combatant (keeps the 1v1 loop intact).
  - **Zombie Nest** = `SWARM` — three rapid small hits in one turn.
  - **RatKing** = `ENRAGE` — a permanent +50% damage buff applied on entering
    phase 3.
  - **Faucci** = `HEAL` — restores its own HP instead of attacking that turn.
- **Epic reference HP, winnable by leveling.** Bosses are constructed at their
  *literal* final stats — do **NOT** re-run `levelingUp()` on them. (The
  reference does, which inflates Faucci to ~327k = unwinnable.) Winnability rests
  on the player's compounding damage (+10%/level) plus the Defend → 2× next-hit
  buff already in combat.
- **Win = a dedicated `Mode.WON` + victory screen** with cure ASCII art and final
  stats, mirroring how `DEAD` is handled in `GameApp`'s CardLayout. The combat
  banner gains a phase indicator (e.g. `Faucci Lv.20 [P2/3]`).

### Boss roster (rooms, stats, signature) — locked

Stats and room→boss mapping match the original source
(`reference/GameWindow.java.txt`: `createNPCS1` for stats,
`NPCSBattleSet` lines 675–689 for the room mapping). Mapping confirmed with the
user 2026-06-09 (the handoff had rooms 19/22 transposed; corrected here).

| Room | Boss        | HP     | Level | Base dmg | Signature |
|------|-------------|--------|-------|----------|-----------|
| 10   | HiveMind    | 1,000  | 6     | 30       | `SUMMON`  |
| 19   | Zombie Nest | 5,000  | 10    | 50       | `SWARM`   |
| 22   | RatKing     | 7,500  | 15    | 50       | `ENRAGE`  |
| 24   | Faucci      | 20,000 | 20    | 50       | `HEAL` (+ holds the cure) |

Difficulty ramps monotonically: 1,000 → 5,000 → 7,500 → 20,000. Faucci is a
deliberate ~80+ turn war of attrition.

### Deferred (NOT in this spec)

- Save / load on the new UI.
- Per-enemy animation choreography / sprite migration beyond the boss sprites
  this increment introduces.
- Any post-win meta (new game+, score table).

---

## 2. Boss model (`Boss extends NPC`, data-driven phases)

A `Boss` **is an** `NPC` so `GameState`'s existing `enemy.takeTurn(...)` call
site works unchanged through polymorphism. The boss adds phase state on top.

### 2.1 `Move` — the attack vocabulary

A `Move` enum (or small value type) names what the boss does on a turn:

- `STRIKE` — attack at 1.0× effective damage.
- `SLAM` — attack at `SLAM_MULTIPLIER` (≈1.8×); the punish window — the player
  learns to Defend the turn `SLAM` is due.
- `SUMMON` — apply a damage-over-time "horde" threat: chip damage of
  `ceil(baseDamage * SUMMON_CHIP_FRACTION)` (≈0.5×) per turn for
  `SUMMON_TURNS` (3) following turns, stacking is capped at one active horde.
- `SWARM` — `SWARM_HITS` (3) consecutive small hits in one turn, each at
  `SWARM_HIT_FRACTION` (≈0.5×) effective damage.
- `ENRAGE` — apply a permanent ×`ENRAGE_MULTIPLIER` (1.5) to the boss's effective
  damage; takes no attack action the turn it triggers (it is an on-enter effect,
  see 2.3, not a pattern slot).
- `HEAL` — restore `ceil(maxHealth * HEAL_FRACTION)` (≈3%) HP instead of
  attacking, clamped to max; logs the heal.

All numeric constants live in one named-constants block (see 2.5) so the balance
pass tunes in one place.

### 2.2 `Phase` — a gated, repeating pattern

A `Phase` is an immutable record of:

- `hpGatePercent` — the max-HP fraction **at or below which** this phase is
  active (phase 1 = 1.00, phase 2 = 0.66, phase 3 = 0.33).
- `pattern` — a non-empty `Move[]` cycled by `pattern[turnInFight % pattern.length]`.
- `onEnter` — an optional one-shot effect fired the first time the boss crosses
  into this phase (used by RatKing's `ENRAGE`; null for the others).

### 2.3 `Boss` behavior

`Boss` holds: `Phase[] phases` (ordered hardest-gate-last), an `int phaseIndex`
(current, starts 0), an `int turnInFight` counter, and any signature state
(active-horde turns remaining for SUMMON, enraged flag).

`Boss` overrides `takeTurn(target, log)`:

1. **Advance phase by HP.** Compute `hpPercent = currentHealth / maxHealth`.
   While the next phase's `hpGatePercent >= hpPercent`, advance `phaseIndex` and
   fire that phase's `onEnter` (once). This handles big single hits that skip a
   threshold.
2. **Resolve any active horde** (SUMMON DoT): if turns remain, apply chip damage
   to the target and decrement.
3. **Execute this turn's move** = `phases[phaseIndex].pattern[turnInFight % len]`,
   dispatching by `Move` (see 2.1). `STRIKE`/`SLAM`/`SWARM`/`HEAL` resolve
   immediately; `SUMMON` arms the horde.
4. `turnInFight++`.

Damage application reuses the existing `Character`/`NPC` attack math
(`getEffectiveDamage()` + the existing randomized modifier + the player's Defend
mitigation), scaled by the move's multiplier. `Boss` overrides
`getEffectiveDamage()` only to fold in the ENRAGE multiplier when enraged;
otherwise it equals `baseDamage` (no weapons for NPCs).

**Constructed at literal stats.** `Boss` sets HP/level/baseDamage directly and
**never** calls `levelingUp()`. A code comment states why (the reference's
triple-level inflation makes Faucci unwinnable).

### 2.4 Per-boss phase patterns (illustrative; finalized in the balance pass)

The patterns below are the *learnable* tells. Exact contents are confirmed during
the balance pass against a leveled player; the structure is locked.

- **HiveMind** (room 10) — teaches Defend-on-SLAM and the horde threat:
  - P1: `[STRIKE, STRIKE, SLAM]`
  - P2: `[STRIKE, SUMMON, SLAM]`
  - P3: `[SUMMON, SLAM, STRIKE, SLAM]`
- **Zombie Nest** (room 19) — swarm pressure:
  - P1: `[STRIKE, SWARM, STRIKE]`
  - P2: `[SWARM, SLAM, STRIKE]`
  - P3: `[SWARM, SLAM, SWARM, SLAM]`
- **RatKing** (room 22) — `onEnter` P3 fires `ENRAGE`:
  - P1: `[STRIKE, STRIKE, SLAM]`
  - P2: `[STRIKE, SLAM, STRIKE, SLAM]`
  - P3 (enraged): `[SLAM, STRIKE, SLAM]`
- **Faucci** (room 24) — attrition; heals as it weakens:
  - P1: `[STRIKE, SLAM, STRIKE]`
  - P2: `[STRIKE, HEAL, SLAM]`
  - P3: `[HEAL, SLAM, STRIKE, SLAM]`

### 2.5 `BossFactory.forRoom(int room)`

A factory builds the four bosses from a single named-constants block (HP, level,
dmg, the multipliers/fractions/counts from 2.1, and the `Phase[]` per boss).
`forRoom(n)` returns the boss for rooms 10/19/22/24 and throws / returns null for
any other room (callers only invoke it on boss rooms). Centralizing here keeps
`GameState` free of boss tuning data.

---

## 3. Wiring into `GameState`

`GameState` already declares `BOSS_ROOMS = {10, 19, 22, 24}` and a
`hasNPC()==4` branch that logs the "not yet" hint. Changes:

### 3.1 Mode

`enum Mode { EXPLORE, COMBAT, DEAD, WON }` — add `WON`.

### 3.2 Starting a boss fight

In `tryEncounter()` (the current `hasNPC()==4` seam), instead of the "not yet"
log: build the boss via `BossFactory.forRoom(currentRoom.getRoomNumber())`, set
it as the current enemy, enter `Mode.COMBAT`, and log a boss-intro line. The
existing combat loop (attack/defend/use-item → `enemy.takeTurn`) then drives the
fight unchanged, because `Boss` is an `NPC`.

### 3.3 Defeating a boss / winning

`onEnemyDefeated()` currently has a 50/50 respawn for regular zombies. Add a
branch up front: **if the defeated enemy is a `Boss`** (or the room is a boss
room), do **not** respawn. Then:

- **Cure room (24, `currentRoom.hasCure()`):** transition to `Mode.WON`, log the
  victory line, and let the UI show the win screen. No respawn, no further
  combat.
- **Non-cure boss rooms (10, 19, 22):** clear the room (`removeNPC`), award the
  boss bounty (mirror the reference's "you level up" — apply the player level-up
  effect; concrete count set in the plan, reference applies it 3×), log the
  victory, return to `Mode.EXPLORE`.

`Room` already has unused `setRoomCure()`/`hasCure()`. `loadHospital` calls
`setRoomCure()` on room 24 (alongside the existing boss-room setup).

---

## 4. UI — phase indicator + win screen (`Scene` / `GameApp`)

Model stays Swing-free; only `GameApp` (+ engine render panel) imports Swing.

### 4.1 Combat banner phase indicator

The existing enemy combat banner (`Scene`, shows `Name Lv.N` + HP bar) appends a
phase indicator for bosses: `Faucci Lv.20 [P2/3]`. Regular zombies show no
indicator. `Scene` reads the current phase from the boss via a small accessor
(e.g. `getPhaseLabel()` returning `"P2/3"`, empty for non-bosses) so `Scene`
needs no `instanceof` ladder beyond one check.

### 4.2 Win screen (`Mode.WON`)

A dedicated victory screen, built like the existing `DEAD` screen in `GameApp`'s
CardLayout:

- Cure ASCII art (new `assets/art/cure.txt`, authored via the `ascii-art` skill,
  UTF-8 **without BOM**).
- A victory message: *"You secured the cure. Humanity has a chance."*
- Final run stats (player level, effective damage, HP, bandages remaining).

`GameApp` switches to the WON card when `GameState.getMode() == WON`, exactly as
it switches to the DEAD card on death.

---

## 5. Architecture (files)

- **`adventure_game.Move`** (new) — the attack-vocabulary enum.
- **`adventure_game.Phase`** (new, immutable) — `hpGatePercent`, `Move[] pattern`,
  optional `onEnter`.
- **`adventure_game.Boss`** (new, `extends NPC`, Swing-free) — phase state,
  `takeTurn` override, `getEffectiveDamage` (ENRAGE), `getPhaseLabel()`,
  signature-move state.
- **`adventure_game.BossFactory`** (new) — `forRoom(int)` + the named-constants
  block (stats, multipliers, per-boss `Phase[]`).
- **`GameState`** — `Mode.WON`; boss-start in `tryEncounter`; boss-defeat/win in
  `onEnemyDefeated`; `setRoomCure()` on room 24 in `loadHospital`.
- **`Scene`** — phase indicator on the boss banner.
- **`GameApp`** — WON card + victory screen wiring.
- **`assets/art/cure.txt`** (new) + four boss sprites (new) — via `ascii-art`.

---

## 6. Boss sprites (open coordination with the `ascii-art` skill)

Regular entities are 5×5 per the `ascii-art` skill. Bosses are meant to read as
bigger and more imposing — target **≤8 wide × ≤6 tall**. The `ascii-art` skill
currently mandates 5×5 for entities, so before authoring boss sprites the plan
must either (a) add a documented "boss" size exception to the `ascii-art` skill,
or (b) confirm bosses fit the viewport at the larger footprint and the asset
sanity tests accept them. Either way: UTF-8 without BOM; the existing
`allArtFilesAreBomFree` and viewport-fit asset tests must still pass.

---

## 7. Testing (Swing-free, unit-tested)

- **Phase advance by HP:** a boss at >66% is in P1; dropping it to ≤66% advances
  to P2 (and ≤33% → P3); a single large hit that skips a threshold still lands in
  the correct phase and fires its `onEnter` exactly once.
- **Pattern cycling:** with a fixed phase, consecutive `takeTurn` calls produce
  the pattern in order and wrap (`turn % len`).
- **SLAM** deals ≈`SLAM_MULTIPLIER`× a STRIKE (compare against a seeded baseline,
  accounting for the existing randomized modifier).
- **SUMMON** arms a horde that applies chip damage on the following
  `SUMMON_TURNS` turns and then stops; it does not create a second combatant.
- **SWARM** applies `SWARM_HITS` hits in one turn.
- **ENRAGE** (RatKing) permanently raises effective damage once P3 is entered,
  and fires only once.
- **HEAL** (Faucci) raises the boss's HP (clamped to max) and skips its attack
  that turn.
- **Literal stats:** a `BossFactory` boss has exactly the table-2 HP/level/dmg
  (proves `levelingUp` was not applied — Faucci is 20,000, not ~327k).
- **Win transition:** defeating the room-24 boss sets `Mode.WON` and does not
  respawn; defeating a non-cure boss returns to `EXPLORE`, clears the room, and
  does not respawn.
- **Phase label:** `Boss.getPhaseLabel()` reports `"P2/3"` etc.; a regular `NPC`
  yields no indicator.
- **Scene:** the combat banner shows the phase indicator for a boss; the WON
  state / cure art loads.

GUI (`GameApp` win card) is verified by a manual run + screenshot, as in prior
phases.

---

## 8. Acceptance

Launching the app and playing through:

- Entering a boss room starts a real fight (no more "not yet" hint); the banner
  shows `Name Lv.N [P1/3]`.
- As the boss loses HP it crosses into P2 then P3; the pattern visibly changes,
  and each boss's signature move appears (HiveMind summons a horde, Zombie Nest
  swarms, RatKing enrages at low HP, Faucci heals).
- A leveled, armed player can defeat all four in order; Faucci is a long fight
  but winnable.
- Defeating Faucci (room 24) shows the cure victory screen with final stats;
  the other three return to exploration with a level-up reward.
- All unit tests pass; no Swing imports outside `GameApp` (+ engine render);
  all art files BOM-free and viewport-fitting.

---

## 9. Risks & mitigations

- **Boss balance (winnability).** *Mitigation:* literal stats (no `levelingUp`);
  a dedicated balance pass tunes the constants block against a leveled player;
  unit tests pin the stats and multipliers so tuning is one-place and visible.
- **Phase-skip on big hits.** A single hit could jump two thresholds. *Mitigation:*
  the `while` advance loop in `takeTurn` walks every crossed gate and fires each
  `onEnter` once.
- **Touching `onEnemyDefeated` respawn logic.** *Mitigation:* the boss branch is
  added *before* the existing 50/50 respawn and returns early; a regression test
  confirms regular-zombie respawn is unchanged.
- **Boss sprite size vs `ascii-art` 5×5 rule.** *Mitigation:* resolved in §6
  before authoring; asset sanity tests are the gate.
- **`SUMMON` tempting a second combatant** (breaking the 1v1 loop). *Mitigation:*
  it is strictly a DoT on the boss; tested to not spawn an enemy.

---

## 10. Terminology

- **Phase** — one of a boss's 3 HP-gated stages, each with its own repeating
  move pattern.
- **Move** — a single boss action: `STRIKE`, `SLAM`, `SUMMON`, `SWARM`,
  `ENRAGE`, `HEAL`.
- **Pattern** — the fixed, repeating `Move[]` a phase cycles each turn; the
  player's tell.
- **Signature move** — the one distinctive move per boss.
- **Horde (SUMMON)** — a multi-turn chip-damage effect, not a second enemy.
- **Literal stats** — boss HP/level/damage set directly, with `levelingUp`
  deliberately skipped.

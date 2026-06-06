# Zombie Game — Rendering Core (Phase 1) Design

**Date:** 2026-06-06
**Project:** PostProject — *Covid, The Zombie Apocalypse*
**Status:** Approved design, ready for implementation planning

---

## 1. Background & goal

The project is an existing turn-based zombie RPG written in Java/Swing. The
fiction and game model already exist and work: a player (Mick), zombie NPCs
(Walker / Creeper / Sprinter), bosses (HiveMind, Faucci, RatKing, Zombie Nest),
a 25-room hospital map loaded from a `.txt` file, turn-based combat, items
(bandages, antibiotics), weapons (Knife, Pistols, AssaultRifle), and a
find-the-cure win condition.

What does **not** feel like a game is the presentation. Everything the player
sees is produced by `JTextArea.append(...)` — an append-only scrolling log in
two bare windows (a game window and a separate battle window), each just
buttons plus a text box.

**Goal of this project (long term):** turn the game into an animated,
text-rendered ("ASCII") experience with a polished single-screen UI, built so
that a later migration from ASCII glyphs to image sprites is mostly a renderer
swap.

**Goal of this spec (Phase 1):** build the rendering core and prove it with one
thin end-to-end vertical slice, while decoupling the game model from the UI.

### Decisions locked during brainstorming

- **Platform:** stay in **Java + Swing**. Preserves the existing model classes
  and coursework; sprites later are a `drawImage()` swap on the same panel.
- **Animation model:** **event-driven with animation bursts** (not a continuous
  real-time loop). The screen is static between actions; an action plays a short
  scripted frame sequence, then settles.
- **Layout:** **one unified screen** with fixed regions (HUD, viewport, log,
  action buttons). Combat happens *inside* the viewport — no separate window.
- **Skill suite:** four project-local skills (ascii-art, ascii-animation,
  room-and-map, hud-component), each created the first time real work demands
  it, then reused for bulk authoring.

---

## 2. Overall program decomposition (context only — not all in this spec)

The full effort is a program of sub-projects, each with its own spec:

1. **Rendering core + vertical slice + model decoupling** ← *this spec*
2. Bulk authoring: all entities, items, rooms, animations (uses the skills)
3. Full HUD / menus polish, full combat parity, save/load on the new UI
4. Sprite migration (renderer swaps glyphs → image tiles; model + loop unchanged)

The skill suite is built incrementally across 1–3, not up front.

---

## 3. Architecture: model / renderer split

Split the codebase into a **model** (pure game logic, no Swing) and a
**renderer** (draws the model as a character grid), connected by a **redraw
cycle** rather than a continuous loop.

```
   MODEL (no Swing)                 RENDERER (Swing)
   ┌──────────────┐                ┌─────────────────────┐
   │ GameState    │   reads from   │ Screen (char grid)  │
   │  player      │ ◄───────────── │ RenderPanel(JPanel) │
   │  rooms/NPCs  │                │ AnimationPlayer     │
   │  combat      │   emits        │ Scene / regions     │
   │  messageLog  │ ─ messages ──► │ ArtAsset / Animation│
   └──────────────┘                └─────────────────────┘
```

### What survives

`Character`, `Player`, `NPC`, `Room`, and the `items` package are kept. They are
mostly pure logic and form a good foundation.

### The keystone change: decouple model from UI

Today, model methods write directly into the UI, e.g.:

```java
public void attack(Character other, JTextArea output) {
    ...
    output.append(this.name + " dealt " + damage + " ...\n");
}
```

This must change so the model **emits messages/events** and the renderer decides
how to present them (e.g. an attack message becomes a swing animation + a damage
flash + a log line). Concretely:

- Model methods stop taking `JTextArea` and stop calling `append`.
- The model owns a **message log** (a list of strings, or small event objects)
  that it appends to.
- The renderer reads the message log to update the on-grid LOG region, and reads
  combat events to trigger animations.

This decoupling is the prerequisite for all animation and is the riskiest part of
Phase 1 because it touches existing methods (`attack`, `defend`, `takeTurn`,
`levelingUp`, `obtain`, item `consume`, etc.). It is done carefully, preserving
existing behavior (verified against existing JUnit tests in
`src/adventure_game/Test/`).

### GameState

A new model class (`GameState`, or a repurposed `Game`) holds: `player`, the
room map, the NPC roster, `currentRoom`, current combat state, and the message
log. The game *logic* currently embedded in `GameWindow`'s button listeners
(move N/S/E/W, start/step combat, use item, save) moves here as plain methods
with **no Swing imports**.

---

## 4. The rendering core (new classes)

Four small, single-purpose classes.

### 4.1 `Screen` — the frame buffer
A fixed character grid.
- State: `char[][] cells` of width × height. (A parallel attribute/color layer
  is added later for sprites — same shape, out of scope now.)
- Methods: `clear()`, `put(int x,int y,char c)`, `drawText(int x,int y,String s)`,
  `drawBox(int x,int y,int w,int h)`, `blit(ArtAsset art,int x,int y)`.
- Pure data + drawing primitives. No Swing.

### 4.2 `RenderPanel extends JPanel` — the surface
- Draws the `Screen` in a **monospaced font**, cell by cell, in
  `paintComponent`.
- Uses `FontMetrics` to size each cell and the panel.
- This panel is (most of) the window's content.

### 4.3 `ArtAsset` and `Animation` — the content
- `ArtAsset`: a loaded art piece — aligned text lines plus width/height (and an
  anchor point). Loaded from `assets/art/*.txt`.
- `Animation`: an ordered list of frames; each frame references an `ArtAsset`
  (or offset) and a duration in milliseconds. Loaded from an asset file.

### 4.4 `AnimationPlayer` — the burst
- Wraps a `javax.swing.Timer`.
- `play(Animation anim, Runnable onSettle)`: advances frames, repainting each,
  then calls `onSettle` (which triggers a full scene redraw).
- This is the "animation burst" mechanism. Event-driven: only runs when an
  action triggers it.

---

## 5. The unified screen

One window composes four regions into the `Screen` on every redraw:

```
┌──────────────────────────────────────┐
│ HUD:  room name · Lv · HP bar · DMG   │  ← drawn on grid
├──────────────────────────────────────┤
│ VIEWPORT: room backdrop + @ + zombies │  ← drawn on grid
│           (animations play here)      │
├──────────────────────────────────────┤
│ LOG: last N narration lines           │  ← drawn on grid
├──────────────────────────────────────┤
│ [N][S][E][W]  [Attack][Item][Save]    │  ← real JButtons
└──────────────────────────────────────┘
```

- **HUD, VIEWPORT, LOG** are drawn onto the `Screen` grid each redraw.
- **Action buttons** are real `JButton`s in a thin row below the grid. This
  reuses the existing, working button logic and stays clickable/accessible.
  Keyboard controls (N/S/E/W, A=attack) are a trivial later addition.
- **Combat is not a separate window.** The zombie animates in the viewport and
  the action button row swaps to combat actions (Attack / Defend / Item).

### The redraw cycle
```
action (button) → GameState updates → produces messages/events
   → if an event yields an animation:
        AnimationPlayer.play(anim, settle)
        settle → redraw()
   → else:
        redraw()

redraw(): Scene recomposes the whole Screen from GameState, RenderPanel repaints.
```
A `Scene` (or a set of region renderers) owns the region rectangles and the
`render(GameState)` that composes HUD + viewport + log into the `Screen`.

---

## 6. The skill suite

Four project-local skills under `.claude/skills/`. Each bakes in the project's
**standard sizes, asset file format, and style rules**, plus a checklist, so
output is consistent every time. Each is created the first time the work demands
it, against real engine code, then reused for bulk authoring.

1. **`ascii-art`** — design an entity / item / boss as grid-aligned ASCII art at
   a standard size; save as an `assets/art/*.txt` asset.
2. **`ascii-animation`** — turn a base `ArtAsset` + an action (attack swing,
   lunge, hit-flash, death/collapse, idle) into a timed frame sequence in the
   `Animation` file format.
3. **`room-and-map`** — design a room backdrop *and* write its map `.txt` line
   (room number, name, bio, N/S/E/W exits, spawns), consistent across the
   25-room hospital.
4. **`hud-component`** — build reusable on-grid UI pieces (health/XP bars,
   bordered panels, menus, the action row) in one consistent style.

Standard sizes and the exact asset/animation file formats are finalized during
implementation of the vertical slice (the first real client) and then encoded
into the skills.

---

## 7. Phase 1 vertical slice (the deliverable)

One thin slice that proves the entire approach end-to-end:

> Load the hospital map → render **one room** on the grid with HUD + log →
> player `@` and **one Walker** `z` in the viewport → press **Attack** → an
> animation burst plays *in the viewport* → log + HP bar update → settle.

Building exactly this forces every core piece into existence (frame buffer,
animation player, scene regions, model decoupling) and triggers the first real
use of skills 1, 2, and 4 (player art, zombie art, attack animation, health bar)
plus one room from skill 3. After the slice works, the rest is bulk authoring.

### Acceptance criteria
- The game **builds and runs** from this repository with a documented command
  (the current `AdventureGame\...` hardcoded paths are corrected to
  repo-relative; a build/run command is established early).
- A single window opens with the four-region unified layout.
- The HUD shows room name, level, an HP **bar**, and damage.
- The viewport shows the room backdrop with `@` and one `z`.
- Pressing **Attack** plays a multi-frame animation in the viewport, then the HP
  bar and LOG update and the scene settles.
- Existing model behavior is preserved: existing JUnit tests in
  `src/adventure_game/Test/` still pass (after their `JTextArea` coupling is
  updated to the new message-emitting model).
- No Swing imports remain in `Character`, `Player`, `NPC`, `Room`, or `items`.

### Out of scope for Phase 1
- All other entities, items, rooms, and animations beyond the one room / one
  Walker / attack used in the slice.
- Full combat parity (defend, item-in-combat, bosses, despawn/loot rules),
  save/load on the new UI, menus, keyboard input.
- Sprites / color.

---

## 8. Risks & mitigations

- **Decoupling regression** — touching `attack`/`defend`/`takeTurn`/`levelingUp`
  risks changing combat math. *Mitigation:* keep the existing JUnit tests green;
  change signatures (drop `JTextArea`, append to message log) without changing
  arithmetic.
- **Does it even run here?** — paths are hardcoded to a non-existent
  `AdventureGame\` prefix. *Mitigation:* first implementation task is to get a
  clean build + run from repo root with corrected paths.
- **Monospaced alignment** — proportional fonts misalign ASCII. *Mitigation:*
  `RenderPanel` explicitly uses a monospaced font and `FontMetrics`-based cell
  sizing.
- **Swing threading** — `Timer` callbacks and repaints must stay on the EDT.
  *Mitigation:* use `javax.swing.Timer` (fires on EDT) and standard
  `repaint()`.

---

## 9. Terminology

- **Frame buffer / `Screen`** — the off-screen character grid that is fully
  recomposed each redraw (the opposite of append-only).
- **Animation burst** — a short, event-triggered frame sequence that plays then
  settles back to a static scene.
- **Settle** — the full scene redraw that runs after an animation completes.
- **Region** — a fixed rectangle of the screen (HUD, viewport, log).

---
name: hud-component
description: Use when building a reusable on-grid UI piece (bar, panel, menu, button row) for the PostProject zombie game, so HUD elements share one style and render through Screen.
---

# HUD Component Authoring (PostProject)

Build reusable text UI pieces as pure functions returning Strings (or
drawing onto a `Screen`), added to `adventure_game.engine.HudComponents`.

## Style rules
- ASCII only. Bars use `[`...`]` with `|` (filled) and `.` (empty).
- Panels/boxes use `+ - |` (matches `Screen.drawBox`).
- Components are pure and deterministic: same inputs -> same string. No I/O.
- Clamp numeric inputs (never overflow the declared width).

## Output
- A static method on `HudComponents` with a focused signature
  (e.g. `healthBar(int current, int max, int width)`).
- A unit test covering full / empty / mid / clamped cases.

## Checklist
- [ ] Pure function, deterministic, clamped.
- [ ] ASCII-only, width-exact output.
- [ ] Unit test with boundary cases.

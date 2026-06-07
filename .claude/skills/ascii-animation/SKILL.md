---
name: ascii-animation
description: Use when authoring an animation (attack, lunge, hit-flash, death, idle) for the PostProject zombie game, to emit a frame sequence in the engine's AnimationFrame format.
---

# ASCII Animation Authoring (PostProject)

Turn a base art + an action into an `Animation` (a `List<AnimationFrame>`),
added as a factory method on `adventure_game.engine.Animations`.

## Frame format
`new AnimationFrame(ArtAsset art, int dx, int dy, int durationMs)`
- `dx,dy`: column/row offset from the actor's blit position (Scene adds them).
- `durationMs`: 50-120 ms per frame is the readable range for bursts.

## Conventions
- An action is one factory method returning `new Animation(frames)`.
- Bursts settle: the LAST frame is the impact/peak; the engine resolves game
  state on settle, so do not encode the "after" state in frames.
- Keep arts ASCII-only and small; reuse `assets/art` pieces where possible.
- 3-6 frames per burst.

## Checklist
- [ ] Factory method added to `Animations`.
- [ ] Offsets move in the intended direction; last frame is the peak.
- [ ] Per-frame durations in 50-120 ms.
- [ ] A unit test asserts size, direction (dx), and the final frame's art.

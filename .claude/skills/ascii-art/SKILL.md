---
name: ascii-art
description: Use when authoring ASCII art for an entity, item, or boss in the PostProject zombie game, so every sprite is grid-aligned, correctly sized, and drops straight into the engine.
---

# ASCII Art Authoring (PostProject)

Produce ASCII art that the engine's `ArtAsset`/`Screen.blit` can render.

## Project standard
- **Entities** (player, zombies, bosses): exactly **5 columns wide x 5 rows tall**.
- **Items**: **3 wide x 3 tall**.
- Use **space for transparency** — spaces are NOT drawn, so the backdrop shows through. Pad every line to the full width with spaces.
- Anchor: bottom-centered (feet on the last row).
- ASCII only (printable 0x20-0x7E). No box-drawing/Unicode (the render font is Monospaced).

## Output
- Save to `assets/art/<name>.txt`, one art line per file line, no trailing blank line beyond the row count.
- After saving, state the file path and the exact width x height.

## Checklist
- [ ] Every line is the standard width (pad with spaces).
- [ ] Row count equals the standard height.
- [ ] Only printable ASCII used.
- [ ] Reads as the intended subject at a glance.
- [ ] Saved under `assets/art/` and size reported.

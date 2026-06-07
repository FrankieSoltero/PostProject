---
name: room-and-map
description: Use when adding or editing a room in the PostProject hospital - authors the viewport backdrop art AND the matching map .txt lines (room entry + exits) so geometry and data stay consistent.
---

# Room & Map Authoring (PostProject)

A room needs two artifacts that MUST agree:

## 1. Backdrop art -> assets/art/room<N>.txt
- Width = viewport width (currently 60 columns).
- Height <= viewport rows (currently 13: Scene rows 3-15).
- ASCII only; space = transparent (player/enemy composite on top).
- A floor line near the bottom so characters stand on something.
- IMPORTANT: save as UTF-8 WITHOUT BOM (Java Files.readAllLines does not strip a BOM).

## 2. Map data -> data/levels/Hospital Map/The-Hospital.txt
Two sections, separated by `#` comment lines (do not renumber existing rooms):
- Room entry: `N:Room Name:Room bio sentence(s).`
- Exit line (after the `#  E   N   W   S` header): `N: E: N: W: S:` where each
  is a target room number or `-1` for no exit.

## Consistency checklist
- [ ] Backdrop exits visually match the exit line directions.
- [ ] Room number identical in both files; not already used.
- [ ] Exit targets are valid room numbers that exist.
- [ ] Reciprocal exits agree (if A's east is B, B's west should be A) unless a one-way is intended.
- [ ] Bio mentions the real exits.

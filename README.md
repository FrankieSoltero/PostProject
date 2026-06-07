# PostProject
This is a text game that I was originally working on for my second semester computer science class. I decided I liked what the idea was and am working on implementing an interface that would make it UI text based game instead of a terminal text based game. 
I started working on the project in the beginning of march. The most recent commit is th beginning of adding User Interface. I look to complete the interface
by the end of april - may. The whole project is in Java and I worked with my professor on files: Character, NPC, Player, and Game but only the entercombat
loop and the main game. I changed the file to run the Terminal output to the GameWindow File

## Building & running (Phase 1)

Requires JDK 17+ on PATH. From the repository root, in PowerShell:

- `./build.ps1` — compile to `out/`
- `./test.ps1` — compile and run the JUnit suite
- `./run.ps1` — launch the game (`adventure_game.GameApp`)

The Phase 1 vertical slice renders one room with the player and one Walker;
**Attack** plays an animation burst and resolves the hit. See
`docs/superpowers/specs/2026-06-06-zombie-game-rendering-core-design.md` and
`docs/superpowers/plans/2026-06-06-rendering-core-vertical-slice.md`.

### Phase 2a — Explore & Fight

You now walk the full 25-room hospital with N/S/E/W (each room has its own
backdrop), get pulled into encounters with Walkers, Creepers, and Sprinters, and
fight with Attack / Defend / Create Bandage / Use Bandage while leveling up on
kills. Bosses, loot, save/load, and the win sequence are deferred to a later
increment. See `docs/superpowers/specs/2026-06-07-phase2a-explore-and-fight-design.md`.

### Phase 2b-1 — Loot & Weapons

Weapons (Knife / Pistol / AssaultRifle) and bandages are now found in rooms and
dropped by defeated zombies. You carry one equipped weapon that adds to your
damage and is swapped when you find a better one; the HUD shows your equipped
weapon, effective damage, and bandage count. Bosses, the win sequence, and
save/load are still to come. See
`docs/superpowers/specs/2026-06-07-phase2b1-loot-and-weapons-design.md`.

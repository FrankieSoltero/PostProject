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

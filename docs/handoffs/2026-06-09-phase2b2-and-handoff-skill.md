# Handoff — PostProject session (Phase 2b-2 design + agent-handoff skill)        (2026-06-09 · branch main)

🎯 Goal
Two threads this session: (1) **build the `agent-handoff` skill** — DONE & deployed; (2) **design Phase 2b-2 "Bosses & the Win"** — design fully approved, spec not yet written. The open work is Phase 2b-2: write the spec → implementation plan → implement.

📍 Status
- `agent-handoff` skill: **complete.** Lives at `~/.claude/skills/agent-handoff/SKILL.md` (global, ~4.1KB). TDD-verified (RED baselines + GREEN write/resume tests both passed). Not committed (it's outside this repo; `~/.claude` likely isn't a git repo).
- Phase 2b-2: **design approved by user, zero code, no spec/plan files yet.** Parked mid-brainstorming when the user pivoted to build the skill. Next concrete step = write the spec doc.
- Repo: clean tree on `main`, **55/55 tests green** (ran `./test.ps1` this session). `main` is ~21 commits ahead of `origin/main` (unpushed — user commits/pushes only when asked).

🧠 Decisions (Phase 2b-2 design — all user-approved; capture these, they live only in conversation)
- **Full phased boss system.** Why: user explicitly chose the most ambitious option.
- **Phase trigger = HP thresholds + per-turn attack pattern.** 3 phases gated at 66% and 33% max HP. Each phase = a fixed, repeating `Move[]` pattern the player learns; a per-boss turn counter cycles it. (NOT wind-up telegraphs — the pattern itself is the tell.)
- **Signature move per boss:** shared vocab `STRIKE` (normal) + `SLAM` (~1.8×, the beat to Defend on), PLUS one signature each — HiveMind=**Summon** (modeled as a 3-turn chip-damage threat, NOT a 2nd combatant, to keep the 1v1 loop), Zombie Nest=**Swarm** (3 rapid small hits in one turn), RatKing=**Enrage** (permanent +50% dmg on entering <33%), Faucci=**Heal** (restores HP instead of attacking).
- **Keep epic reference HP, require leveling.** Why: user's explicit call. Bosses constructed at *literal* final stats — do NOT re-run `levelingUp()` on them (the reference does, which inflates Faucci to ~327k = unwinnable). Final stats: HiveMind room10 1000hp/lv6/30dmg; Zombie Nest room22 5000/lv10/50; RatKing room19 7500/lv15/50; Faucci room24 20000/lv20/50. Winnability confirmed: player base dmg compounds +10%/level and a successful Defend grants a 2× next-hit buff → ~300-500/hit when leveled; Faucci is a deliberate ~80+ turn war of attrition.
- **Architecture: `Boss extends NPC`** with data-driven `Phase[]` (each Phase = {hpGatePercent, Move[] pattern, onEnter effect}). `Boss` overrides `takeTurn` to advance phase on gate-crossing then run `pattern[turn % len]`. A `BossFactory.forRoom(n)` builds the four from a named-constants block. Polymorphism means `GameState`'s existing `enemy.takeTurn(...)` just works.
- **Win = dedicated `Mode.WON` + victory screen** with cure ASCII art ("You secured the cure. Humanity has a chance."), final stats. Mirror how `DEAD` is handled in `GameApp`'s CardLayout. Enemy combat banner gains a phase indicator (e.g. `Faucci Lv.20 [P2/3]`).

📂 Files (verified to exist this session)
- `src/adventure_game/GameState.java:15` — `Mode { EXPLORE, COMBAT, DEAD }` enum (add `WON`).
- `src/adventure_game/GameState.java` — `tryEncounter()` `hasNPC()==4` branch currently logs "Something massive stirs… not yet." (the seam to replace); `onEnemyDefeated()` (~line 169) has the 50/50 respawn — bosses must branch here to NOT respawn + win-check.
- `src/adventure_game/NPC.java` / `Character.java` — combat base; `Boss extends NPC`.
- `src/adventure_game/Room.java` — already has unused `setRoomCure()`/`hasCure()`; `loadHospital` must set cure on room 24.
- `src/adventure_game/engine/Scene.java` — viewport/HUD render; add WON screen + phase indicator on the enemy banner.
- `reference/GameWindow.java.txt` — original boss/cure logic (rooms, stats, win string) for reference only.
- `~/.claude/skills/agent-handoff/SKILL.md` — the finished skill (separate thread; no further work needed).

✅ Next steps (Phase 2b-2, in order)
1. Write the spec → `docs/superpowers/specs/2026-06-09-phase2b2-bosses-and-win-design.md` (capture the Decisions above verbatim), then have the user review it.
2. Invoke `superpowers:writing-plans` for the implementation plan (TDD increments: Boss+phases model → wire into GameState → WON mode + win transition → cure victory screen/art → balance pass).
3. Implement via subagent-driven-development (user's consistent preference), keeping the model Swing-free and all tests green; author boss sprites + `cure.txt` via the `ascii-art` skill.

❓ Open questions
- Exact tuning numbers for SLAM multiplier, Summon chip dmg, Swarm hit count/scaling, Faucci heal amount, and phase pattern contents per boss — proposed illustratively in the design; finalize in the spec.
- Boss sprite size: design says ≤8 wide × ≤6 tall (bigger than the 5×5 regular entities) — confirm against `ascii-art` skill constraints (it currently mandates 5×5 for entities; bosses may need a skill note or exception).

⚠️ Gotchas / conventions
- Art/`.txt` files MUST be UTF-8 **without BOM** (Java `Files.readAllLines` doesn't strip it; corrupts rendering). Write via `[System.IO.File]::WriteAllLines(path, lines, (New-Object System.Text.UTF8Encoding($false)))`.
- Commits are GPG-signed via **PowerShell** (native Windows GnuPG), not Bash. Don't habitually `gpgconf --kill gpg-agent` — the cache TTL is ~400 days; killing it flushes it and causes the signing failures. Only reset the agent on a literal "socket file removed" error.
- Model/View separation: only `GameApp` (+ engine `RenderPanel`/`AnimationPlayer`) may import `javax.swing`. Keep `Boss`/`GameState` Swing-free.
- HUD "Bandages" counter uses `items.size()` (over-counts if non-bandage consumables are ever added) — known non-blocking follow-up.
- This handoff file is untracked scratch; consider gitignoring `.claude/handoff/`.

▶️ Resume
- Branch: `main` (create a `phase2b2-bosses` branch before implementing, per the project's branch-per-phase pattern).
- Verify command: `./test.ps1` → last run this session **55 tests, 0 failed** (all green).
- Run the app: `./run.ps1`.
- Start by: writing the Phase 2b-2 spec from the Decisions section above; user prefers brainstorm→spec→plan→subagent-execution→local merge.

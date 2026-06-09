# Phase 2b-2 — Bosses & the Win Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the four boss rooms (10, 19, 22, 24) into real phased boss fights and add the find-the-cure win sequence, completing the core game loop.

**Architecture:** A new `Boss extends NPC` holds a data-driven `Phase[]` (3 HP-gated stages, each a repeating `Move[]` pattern); `Boss.takeTurn` advances the phase by HP then runs the next patterned move, so `GameState`'s existing `enemy.takeTurn(...)` drives bosses unchanged via polymorphism. A `BossFactory.forRoom(n)` builds the four at literal stats (no `levelingUp`). Win/clear is decided by a pure `BossEncounter.onDefeat(...)` returning the next `Mode` (new `Mode.WON`). The win screen + phase indicator render through the existing `Scene`/`GameApp` (Swing only in `GameApp`).

**Tech Stack:** Java (no build tool; `build.ps1` compiles all of `src` to `out`), JUnit 5 (console launcher jar), PowerShell scripts (`build.ps1` / `test.ps1`).

**Spec:** `docs/superpowers/specs/2026-06-09-phase2b2-bosses-and-win-design.md`

---

## Conventions for every task

- **Run the full suite:** `.\test.ps1` (compiles, then runs all tests; ~150ms).
- **Run one test class:** `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.<ClassName> --disable-banner`
- **RED for a brand-new test class:** because `build.ps1` compiles tests together with `src`, a test that references a not-yet-created class makes `build.ps1` fail with a *compile error* (e.g. `cannot find symbol: class Boss`). That compile failure IS the RED signal. Once the production class exists but the behavior is unimplemented, RED becomes an assertion failure.
- Production model classes (`Move`, `Phase`, `Boss`, `BossFactory`, `BossEncounter`, `GameState`) must stay **Swing-free** — no `javax.swing` / `java.awt` imports. Only `GameApp` (and engine render panel) may import Swing.
- Tests live in `src/adventure_game/Test/` and are in package `adventure_game.Test` (so they see only **public** API of `adventure_game`).
- Commit after each task with the shown message.

---

## File Structure

**New production files:**
- `src/adventure_game/Move.java` — enum of boss actions.
- `src/adventure_game/Phase.java` — immutable HP-gated stage (gate %, optional on-enter move, pattern).
- `src/adventure_game/Boss.java` — `extends NPC`; phase state, `takeTurn` override, signature moves, `getPhaseLabel()`.
- `src/adventure_game/BossFactory.java` — `forRoom(int)` + tuning constants + per-boss `Phase[]`.
- `src/adventure_game/BossEncounter.java` — pure `onDefeat(Room, Player, MessageLog) -> GameState.Mode`.

**New test files:**
- `src/adventure_game/Test/BossTest.java`
- `src/adventure_game/Test/BossFactoryTest.java`
- `src/adventure_game/Test/BossEncounterTest.java`

**Modified files:**
- `src/adventure_game/GameState.java` — `Mode.WON`; boss start in `tryEncounter`; boss branch in `onEnemyDefeated`; `setRoomCure()` on room 24 in `loadHospital`; `getRoom(int)` accessor.
- `src/adventure_game/engine/Scene.java` — `bannerFor(NPC)` phase indicator; `winSummary(Player)`; cure-art field + WON render.
- `src/adventure_game/GameApp.java` — boss sprites in `enemyArt`; WON card; pass cure art to `Scene`.
- `src/adventure_game/Test/SceneTest.java` — update `buildScene()` for new `Scene` ctor arg; banner + winSummary tests.
- `src/adventure_game/Test/GameStateTest.java` — cure/boss placement tests.

**New assets (Task 12):**
- `assets/art/cure.txt`, `assets/art/hivemind.txt`, `assets/art/zombienest.txt`, `assets/art/ratking.txt`, `assets/art/faucci.txt`.

---

## Task 1: Boss skeleton — `Move`, `Phase`, literal stats, phase-by-HP advance, STRIKE, phase label

**Files:**
- Create: `src/adventure_game/Move.java`
- Create: `src/adventure_game/Phase.java`
- Create: `src/adventure_game/Boss.java`
- Test: `src/adventure_game/Test/BossTest.java`

- [ ] **Step 1: Write the failing test**

Create `src/adventure_game/Test/BossTest.java`:

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.Boss;
import adventure_game.MessageLog;
import adventure_game.Move;
import adventure_game.Phase;
import adventure_game.Player;

public class BossTest {

    private static Boss threePhaseStriker(int hp) {
        Phase[] phases = {
            new Phase(1.00, null, new Move[]{Move.STRIKE}),
            new Phase(0.66, null, new Move[]{Move.STRIKE}),
            new Phase(0.33, null, new Move[]{Move.STRIKE}),
        };
        return new Boss("Tester", hp, 5, 20, phases);
    }

    @Test
    void constructedAtLiteralStatsWithoutLeveling() {
        Boss b = new Boss("Faucci", 20000, 20, 50,
                new Phase[]{ new Phase(1.00, null, new Move[]{Move.STRIKE}) });
        assertEquals(20000, b.getMaxHealth(), "literal HP, no levelingUp inflation");
        assertEquals(20000, b.getHealth());
        assertEquals(50, b.getBaseDamage());
        assertEquals(20, b.getLevel());
    }

    @Test
    void startsInPhaseOne() {
        Boss b = threePhaseStriker(1000);
        assertEquals("P1/3", b.getPhaseLabel());
    }

    @Test
    void advancesPhaseAsHealthCrossesGates() {
        Boss b = threePhaseStriker(1000);
        Player target = new Player("Mick", 100000, 0, 1);
        MessageLog log = new MessageLog();

        b.modifyHealth(-400);          // 600/1000 = 60% -> at/below 0.66 gate
        b.takeTurn(target, log);
        assertEquals("P2/3", b.getPhaseLabel());

        b.modifyHealth(-400);          // 200/1000 = 20% -> at/below 0.33 gate
        b.takeTurn(target, log);
        assertEquals("P3/3", b.getPhaseLabel());
    }

    @Test
    void strikeDamagesTheTarget() {
        Boss b = threePhaseStriker(1000);
        Player target = new Player("Mick", 100000, 0, 1);
        int before = target.getHealth();
        b.takeTurn(target, new MessageLog());
        assertTrue(target.getHealth() < before, "STRIKE should deal damage");
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `.\build.ps1`
Expected: FAIL — compile errors (`cannot find symbol: class Move / Phase / Boss`).

- [ ] **Step 3: Create the `Move` enum**

Create `src/adventure_game/Move.java`:

```java
package adventure_game;

/** The vocabulary of boss actions. Shared: STRIKE/SLAM. Signature: SUMMON, SWARM, ENRAGE, HEAL. */
public enum Move {
    STRIKE,
    SLAM,
    SUMMON,
    SWARM,
    ENRAGE,
    HEAL
}
```

- [ ] **Step 4: Create the `Phase` value type**

Create `src/adventure_game/Phase.java`:

```java
package adventure_game;

/**
 * One HP-gated stage of a boss fight. Active when the boss's HP fraction is at or
 * below {@code gatePercent}. {@code pattern} is the repeating per-turn move list;
 * {@code onEnter} (nullable) is a one-shot move applied the first time the boss
 * crosses into this phase (used for ENRAGE).
 */
public final class Phase {
    private final double gatePercent;
    private final Move onEnter;     // nullable
    private final Move[] pattern;

    public Phase(double gatePercent, Move onEnter, Move[] pattern) {
        if (pattern == null || pattern.length == 0) {
            throw new IllegalArgumentException("pattern must be non-empty");
        }
        this.gatePercent = gatePercent;
        this.onEnter = onEnter;
        this.pattern = pattern.clone();
    }

    public double gatePercent() { return gatePercent; }
    public Move onEnter() { return onEnter; }
    public Move moveForTurn(int turn) { return pattern[turn % pattern.length]; }
}
```

- [ ] **Step 5: Create the `Boss` class (skeleton: phase advance + STRIKE)**

Create `src/adventure_game/Boss.java`:

```java
package adventure_game;

/**
 * A phased boss. Is-an {@link NPC}, so GameState's enemy.takeTurn(...) drives it
 * polymorphically. Constructed at LITERAL stats — never call levelingUp() on a
 * Boss (the reference levels bosses 3x, which inflates Faucci to ~327k = unwinnable).
 */
public class Boss extends NPC {

    private final Phase[] phases;   // index 0 = phase 1 (gate 1.0), hardest gate last
    private int phaseIndex = 0;
    private int turnInFight = 0;

    public Boss(String name, int health, int level, int baseDamage, Phase[] phases) {
        super(name, health, level, baseDamage);
        if (phases == null || phases.length == 0) {
            throw new IllegalArgumentException("a boss needs at least one phase");
        }
        this.phases = phases.clone();
    }

    /** e.g. "P2/3" for the combat banner. */
    public String getPhaseLabel() {
        return "P" + (phaseIndex + 1) + "/" + phases.length;
    }

    @Override
    public void takeTurn(Character other, MessageLog output) {
        if (this.isStunned()) {
            this.decreaseTurnsStunned();
            return;
        }
        advancePhaseByHp(output);
        Move move = phases[phaseIndex].moveForTurn(turnInFight);
        executeMove(move, other, output);
        turnInFight++;
    }

    private void advancePhaseByHp(MessageLog output) {
        while (phaseIndex + 1 < phases.length
                && getHealth() <= phases[phaseIndex + 1].gatePercent() * getMaxHealth()) {
            phaseIndex++;
            applyOnEnter(phases[phaseIndex], output);
        }
    }

    private void applyOnEnter(Phase phase, MessageLog output) {
        // ENRAGE on-enter added in Task 6.
    }

    private void executeMove(Move move, Character other, MessageLog output) {
        switch (move) {
            case STRIKE:
            default:
                this.attack(other, output);
                break;
        }
    }
}
```

- [ ] **Step 6: Run to verify it passes**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.BossTest --disable-banner`
Expected: PASS (4 tests).

- [ ] **Step 7: Run full suite (regression guard)**

Run: `.\test.ps1`
Expected: all prior tests still pass plus the 4 new ones.

- [ ] **Step 8: Commit**

```powershell
git add src/adventure_game/Move.java src/adventure_game/Phase.java src/adventure_game/Boss.java src/adventure_game/Test/BossTest.java
git commit -m "feat: Boss skeleton with HP-gated phases and STRIKE"
```

---

## Task 2: `SLAM` (heavy ~1.8x hit)

**Files:**
- Modify: `src/adventure_game/BossFactory.java` (n/a yet) — actually `src/adventure_game/Boss.java`
- Test: `src/adventure_game/Test/BossTest.java`

SLAM reuses the existing `Character` temp damage buff (a multiplicative factor applied to the next attack, then reset to 1.0 — see `Character.setTempDamageBuff` / `attack`).

- [ ] **Step 1: Add the failing test**

Append to `BossTest.java` (inside the class):

```java
    @Test
    void slamHitsHarderThanStrike() {
        // Seed shared RNG so STRIKE and SLAM see the same attack modifier.
        adventure_game.GameRandom.rand = new java.util.Random(42);
        Player t1 = new Player("A", 100000, 0, 1);
        Boss striker = new Boss("S", 1000, 5, 100,
                new Phase[]{ new Phase(1.00, null, new Move[]{Move.STRIKE}) });
        striker.takeTurn(t1, new MessageLog());
        int strikeDmg = 100000 - t1.getHealth();

        adventure_game.GameRandom.rand = new java.util.Random(42);
        Player t2 = new Player("B", 100000, 0, 1);
        Boss slammer = new Boss("L", 1000, 5, 100,
                new Phase[]{ new Phase(1.00, null, new Move[]{Move.SLAM}) });
        slammer.takeTurn(t2, new MessageLog());
        int slamDmg = 100000 - t2.getHealth();

        assertTrue(slamDmg > strikeDmg, "SLAM (" + slamDmg + ") should exceed STRIKE (" + strikeDmg + ")");
        assertEquals((int) (strikeDmg * 1.8), slamDmg, 2, "SLAM ~= 1.8x STRIKE");
    }
```

- [ ] **Step 2: Run to verify it fails**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.BossTest --disable-banner`
Expected: FAIL — `slamHitsHarderThanStrike` (SLAM currently falls through to a plain STRIKE, so `slamDmg == strikeDmg`).

- [ ] **Step 3: Implement SLAM**

In `src/adventure_game/Boss.java`, add a constant and a SLAM case. Add near the top of the class body:

```java
    static final double SLAM_MULTIPLIER = 1.8;
```

Change `executeMove` to:

```java
    private void executeMove(Move move, Character other, MessageLog output) {
        switch (move) {
            case SLAM:
                this.setTempDamageBuff(SLAM_MULTIPLIER);
                this.attack(other, output);
                break;
            case STRIKE:
            default:
                this.attack(other, output);
                break;
        }
    }
```

- [ ] **Step 4: Run to verify it passes**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.BossTest --disable-banner`
Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add src/adventure_game/Boss.java src/adventure_game/Test/BossTest.java
git commit -m "feat: boss SLAM heavy attack (1.8x)"
```

---

## Task 3: `SUMMON` (multi-turn horde chip damage, not a 2nd combatant)

SUMMON arms a "horde" that gnaws the target for `SUMMON_TURNS` following turns. Chip is resolved at the **start** of each `takeTurn` (so the turn SUMMON is cast deals no chip; the next 3 turns do). It is pure damage-over-time on the boss — never a second enemy.

**Files:**
- Modify: `src/adventure_game/Boss.java`
- Test: `src/adventure_game/Test/BossTest.java`

- [ ] **Step 1: Add the failing test**

Append to `BossTest.java`:

```java
    @Test
    void summonArmsAThreeTurnHordeThatChipsThenStops() {
        Player target = new Player("Mick", 100000, 0, 1);
        MessageLog log = new MessageLog();
        // Pattern: SUMMON once, then STRIKE forever. baseDamage 0 so STRIKE deals
        // ~0 and we observe only the horde chip.
        Boss b = new Boss("Hive", 1000, 6, 0, new Phase[]{
            new Phase(1.00, null, new Move[]{Move.SUMMON, Move.STRIKE}) });
        // STRIKE with baseDamage 0 -> 0 dmg; chip = ceil(effective*0.5) but effective
        // is 0 here, so give the boss real damage to see chip. Rebuild with dmg 40.
        b = new Boss("Hive", 1000, 6, 40, new Phase[]{
            new Phase(1.00, null, new Move[]{Move.SUMMON, Move.STRIKE, Move.STRIKE, Move.STRIKE}) });

        assertEquals(0, b.activeHordeTurns());
        b.takeTurn(target, log);                  // turn0: SUMMON -> arms 3
        assertEquals(3, b.activeHordeTurns());

        b.takeTurn(target, log);                  // turn1: chip + STRIKE -> horde 2
        assertEquals(2, b.activeHordeTurns());
        b.takeTurn(target, log);                  // turn2 -> horde 1
        assertEquals(1, b.activeHordeTurns());
        b.takeTurn(target, log);                  // turn3 -> horde 0
        assertEquals(0, b.activeHordeTurns());

        int hpAfter = target.getHealth();
        b.takeTurn(target, log);                  // turn4: SUMMON again (pattern wraps), no chip this turn
        assertEquals(3, b.activeHordeTurns(), "SUMMON re-arms");
    }
```

- [ ] **Step 2: Run to verify it fails**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.BossTest --disable-banner`
Expected: FAIL — `cannot find symbol: method activeHordeTurns()` (compile error), i.e. RED.

- [ ] **Step 3: Implement SUMMON + horde**

In `src/adventure_game/Boss.java`:

Add constants and field near the other fields:

```java
    static final int SUMMON_TURNS = 3;
    static final double SUMMON_CHIP_FRACTION = 0.5;

    private int hordeTurns = 0;
```

Add the test accessor:

```java
    /** Remaining turns the summoned horde will chip the target. */
    public int activeHordeTurns() { return hordeTurns; }
```

In `takeTurn`, resolve the horde right after advancing the phase and before the move:

```java
    @Override
    public void takeTurn(Character other, MessageLog output) {
        if (this.isStunned()) {
            this.decreaseTurnsStunned();
            return;
        }
        advancePhaseByHp(output);
        resolveHorde(other, output);
        Move move = phases[phaseIndex].moveForTurn(turnInFight);
        executeMove(move, other, output);
        turnInFight++;
    }

    private void resolveHorde(Character other, MessageLog output) {
        if (hordeTurns > 0) {
            int chip = (int) Math.ceil(getEffectiveDamage() * SUMMON_CHIP_FRACTION);
            other.modifyHealth(-chip);
            hordeTurns--;
            output.append("The summoned horde gnaws at " + other.getName()
                    + " for " + chip + " damage.");
        }
    }
```

Add the SUMMON case to `executeMove` (before `default`):

```java
            case SUMMON:
                hordeTurns = SUMMON_TURNS;
                output.append(getName() + " summons a horde from the dark!");
                break;
```

- [ ] **Step 4: Run to verify it passes**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.BossTest --disable-banner`
Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add src/adventure_game/Boss.java src/adventure_game/Test/BossTest.java
git commit -m "feat: boss SUMMON horde damage-over-time"
```

---

## Task 4: `SWARM` (three small hits in one turn)

**Files:**
- Modify: `src/adventure_game/Boss.java`
- Test: `src/adventure_game/Test/BossTest.java`

- [ ] **Step 1: Add the failing test**

Append to `BossTest.java`:

```java
    @Test
    void swarmAppliesThreeHitsInOneTurn() {
        Player target = new Player("Mick", 100000, 0, 1);
        MessageLog log = new MessageLog();
        Boss b = new Boss("Nest", 5000, 10, 50, new Phase[]{
            new Phase(1.00, null, new Move[]{Move.SWARM}) });

        int logBefore = log.size();
        int hpBefore = target.getHealth();
        b.takeTurn(target, log);

        // Three "dealt ... damage" lines from three attacks this single turn.
        long dmgLines = log.lines().stream()
                .filter(s -> s.contains("dealt") && s.contains("damage to Mick")).count();
        assertEquals(3, dmgLines, "SWARM = 3 hits");
        assertTrue(target.getHealth() < hpBefore);
    }
```

- [ ] **Step 2: Run to verify it fails**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.BossTest --disable-banner`
Expected: FAIL — only 1 damage line (SWARM falls through to one STRIKE).

- [ ] **Step 3: Implement SWARM**

In `src/adventure_game/Boss.java` add constants:

```java
    static final int SWARM_HITS = 3;
    static final double SWARM_HIT_FRACTION = 0.5;
```

Add the SWARM case to `executeMove` (before `default`):

```java
            case SWARM:
                for (int i = 0; i < SWARM_HITS; i++) {
                    this.setTempDamageBuff(SWARM_HIT_FRACTION);
                    this.attack(other, output);
                }
                break;
```

- [ ] **Step 4: Run to verify it passes**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.BossTest --disable-banner`
Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add src/adventure_game/Boss.java src/adventure_game/Test/BossTest.java
git commit -m "feat: boss SWARM three-hit turn"
```

---

## Task 5: `HEAL` (restore HP instead of attacking)

**Files:**
- Modify: `src/adventure_game/Boss.java`
- Test: `src/adventure_game/Test/BossTest.java`

- [ ] **Step 1: Add the failing test**

Append to `BossTest.java`:

```java
    @Test
    void healRestoresBossHpAndDoesNotAttack() {
        Player target = new Player("Mick", 100000, 0, 1);
        MessageLog log = new MessageLog();
        Boss b = new Boss("Faucci", 20000, 20, 50, new Phase[]{
            new Phase(1.00, null, new Move[]{Move.HEAL}) });
        b.modifyHealth(-10000);            // 10000/20000
        int hpBefore = b.getHealth();
        int targetBefore = target.getHealth();

        b.takeTurn(target, log);

        assertTrue(b.getHealth() > hpBefore, "HEAL raises boss HP");
        assertEquals(targetBefore, target.getHealth(), "HEAL turn deals no damage");
        // ceil(20000 * 0.03) = 600
        assertEquals(hpBefore + 600, b.getHealth());
    }

    @Test
    void healIsClampedToMaxHealth() {
        Boss b = new Boss("Faucci", 20000, 20, 50, new Phase[]{
            new Phase(1.00, null, new Move[]{Move.HEAL}) });
        b.modifyHealth(-100);              // 19900/20000; +600 would overshoot
        b.takeTurn(new Player("Mick", 100, 0, 1), new MessageLog());
        assertEquals(20000, b.getHealth());
    }
```

- [ ] **Step 2: Run to verify it fails**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.BossTest --disable-banner`
Expected: FAIL — HEAL falls through to STRIKE (boss HP unchanged, target damaged).

- [ ] **Step 3: Implement HEAL**

In `src/adventure_game/Boss.java` add constant:

```java
    static final double HEAL_FRACTION = 0.03;
```

Add the HEAL case to `executeMove` (before `default`). `Character.modifyHealth` already clamps to `maxHealth`:

```java
            case HEAL:
                int heal = (int) Math.ceil(getMaxHealth() * HEAL_FRACTION);
                this.modifyHealth(heal);
                output.append(getName() + " regenerates " + heal + " health!");
                break;
```

- [ ] **Step 4: Run to verify it passes**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.BossTest --disable-banner`
Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add src/adventure_game/Boss.java src/adventure_game/Test/BossTest.java
git commit -m "feat: boss HEAL self-regeneration"
```

---

## Task 6: `ENRAGE` (on-enter permanent +50% damage)

ENRAGE is a phase **on-enter** effect (not a pattern slot). Entering an ENRAGE phase permanently multiplies the boss's effective damage by 1.5, once.

**Files:**
- Modify: `src/adventure_game/Boss.java`
- Test: `src/adventure_game/Test/BossTest.java`

- [ ] **Step 1: Add the failing test**

Append to `BossTest.java`:

```java
    @Test
    void enrageOnEnterPermanentlyRaisesEffectiveDamageOnce() {
        Player target = new Player("Mick", 100000, 0, 1);
        MessageLog log = new MessageLog();
        Boss b = new Boss("RatKing", 1000, 15, 50, new Phase[]{
            new Phase(1.00, null, new Move[]{Move.STRIKE}),
            new Phase(0.33, Move.ENRAGE, new Move[]{Move.STRIKE}),
        });

        assertEquals(50, b.getEffectiveDamage(), "not enraged yet");
        b.modifyHealth(-800);              // 200/1000 = 20% <= 0.33 -> enter ENRAGE phase
        b.takeTurn(target, log);
        assertEquals(75, b.getEffectiveDamage(), "enraged = 50 * 1.5");

        b.takeTurn(target, log);           // still phase 2
        assertEquals(75, b.getEffectiveDamage(), "ENRAGE fires only once");
    }
```

- [ ] **Step 2: Run to verify it fails**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.BossTest --disable-banner`
Expected: FAIL — effective damage stays 50 (no enrage handling).

- [ ] **Step 3: Implement ENRAGE**

In `src/adventure_game/Boss.java` add constant + field:

```java
    static final double ENRAGE_MULTIPLIER = 1.5;

    private boolean enraged = false;
```

Override effective damage:

```java
    @Override
    public int getEffectiveDamage() {
        int base = getBaseDamage();
        return enraged ? (int) (base * ENRAGE_MULTIPLIER) : base;
    }
```

Implement `applyOnEnter` (replace the stub):

```java
    private void applyOnEnter(Phase phase, MessageLog output) {
        if (phase.onEnter() == Move.ENRAGE && !enraged) {
            enraged = true;
            output.append(getName() + " ENRAGES! Its blows now land far harder.");
        }
    }
```

- [ ] **Step 4: Run to verify it passes**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.BossTest --disable-banner`
Expected: PASS.

- [ ] **Step 5: Run full suite**

Run: `.\test.ps1`
Expected: all green (Boss model complete).

- [ ] **Step 6: Commit**

```powershell
git add src/adventure_game/Boss.java src/adventure_game/Test/BossTest.java
git commit -m "feat: boss ENRAGE on-enter damage buff"
```

---

## Task 7: `BossFactory.forRoom` — the four bosses with phase patterns

Builds each boss at literal stats (Task 1's no-leveling guarantee) with the spec's
phase patterns. Mapping (locked to the reference / map lore): 10=HiveMind,
19=Zombie Nest, 22=RatKing, 24=Faucci.

**Files:**
- Create: `src/adventure_game/BossFactory.java`
- Test: `src/adventure_game/Test/BossFactoryTest.java`

- [ ] **Step 1: Write the failing test**

Create `src/adventure_game/Test/BossFactoryTest.java`:

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.Boss;
import adventure_game.BossFactory;

public class BossFactoryTest {

    @Test
    void hiveMindRoom10() {
        Boss b = BossFactory.forRoom(10);
        assertEquals("HiveMind", b.getName());
        assertEquals(1000, b.getMaxHealth());
        assertEquals(6, b.getLevel());
        assertEquals(30, b.getBaseDamage());
        assertEquals("P1/3", b.getPhaseLabel());
    }

    @Test
    void zombieNestRoom19() {
        Boss b = BossFactory.forRoom(19);
        assertEquals("Zombie Nest", b.getName());
        assertEquals(5000, b.getMaxHealth());
        assertEquals(10, b.getLevel());
    }

    @Test
    void ratKingRoom22() {
        Boss b = BossFactory.forRoom(22);
        assertEquals("RatKing", b.getName());
        assertEquals(7500, b.getMaxHealth());
        assertEquals(15, b.getLevel());
    }

    @Test
    void faucciRoom24AtLiteralTwentyThousand() {
        Boss b = BossFactory.forRoom(24);
        assertEquals("Faucci", b.getName());
        assertEquals(20000, b.getMaxHealth(), "literal, NOT levelingUp-inflated");
        assertEquals(20, b.getLevel());
    }

    @Test
    void unknownRoomThrows() {
        assertThrows(IllegalArgumentException.class, () -> BossFactory.forRoom(5));
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `.\build.ps1`
Expected: FAIL — `cannot find symbol: class BossFactory`.

- [ ] **Step 3: Create `BossFactory`**

Create `src/adventure_game/BossFactory.java`:

```java
package adventure_game;

/**
 * Builds the four bosses at their literal final stats (no levelingUp) with the
 * Phase 2b-2 phase patterns. Room mapping matches the original source/map lore:
 * 10=HiveMind, 19=Zombie Nest, 22=RatKing, 24=Faucci (cure room).
 */
public final class BossFactory {

    private BossFactory() {}

    public static Boss forRoom(int room) {
        switch (room) {
            case 10: return new Boss("HiveMind",    1000,  6, 30, hiveMindPhases());
            case 19: return new Boss("Zombie Nest", 5000, 10, 50, zombieNestPhases());
            case 22: return new Boss("RatKing",     7500, 15, 50, ratKingPhases());
            case 24: return new Boss("Faucci",     20000, 20, 50, faucciPhases());
            default: throw new IllegalArgumentException("No boss defined for room " + room);
        }
    }

    private static Phase[] hiveMindPhases() {
        return new Phase[]{
            new Phase(1.00, null, new Move[]{Move.STRIKE, Move.STRIKE, Move.SLAM}),
            new Phase(0.66, null, new Move[]{Move.STRIKE, Move.SUMMON, Move.SLAM}),
            new Phase(0.33, null, new Move[]{Move.SUMMON, Move.SLAM, Move.STRIKE, Move.SLAM}),
        };
    }

    private static Phase[] zombieNestPhases() {
        return new Phase[]{
            new Phase(1.00, null, new Move[]{Move.STRIKE, Move.SWARM, Move.STRIKE}),
            new Phase(0.66, null, new Move[]{Move.SWARM, Move.SLAM, Move.STRIKE}),
            new Phase(0.33, null, new Move[]{Move.SWARM, Move.SLAM, Move.SWARM, Move.SLAM}),
        };
    }

    private static Phase[] ratKingPhases() {
        return new Phase[]{
            new Phase(1.00, null,        new Move[]{Move.STRIKE, Move.STRIKE, Move.SLAM}),
            new Phase(0.66, null,        new Move[]{Move.STRIKE, Move.SLAM, Move.STRIKE, Move.SLAM}),
            new Phase(0.33, Move.ENRAGE, new Move[]{Move.SLAM, Move.STRIKE, Move.SLAM}),
        };
    }

    private static Phase[] faucciPhases() {
        return new Phase[]{
            new Phase(1.00, null, new Move[]{Move.STRIKE, Move.SLAM, Move.STRIKE}),
            new Phase(0.66, null, new Move[]{Move.STRIKE, Move.HEAL, Move.SLAM}),
            new Phase(0.33, null, new Move[]{Move.HEAL, Move.SLAM, Move.STRIKE, Move.SLAM}),
        };
    }
}
```

- [ ] **Step 4: Run to verify it passes**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.BossFactoryTest --disable-banner`
Expected: PASS (5 tests).

- [ ] **Step 5: Commit**

```powershell
git add src/adventure_game/BossFactory.java src/adventure_game/Test/BossFactoryTest.java
git commit -m "feat: BossFactory builds the four bosses with phase patterns"
```

---

## Task 8: `BossEncounter.onDefeat` — win/clear decision (pure, testable)

The post-defeat outcome as a pure function: apply the boss bounty (3× `levelModifier`,
matching the reference), clear the room, and return the next `Mode` — `WON` if the
room holds the cure, else `EXPLORE`. This is the win logic, unit-tested without
needing to navigate to a boss room.

**Files:**
- Create: `src/adventure_game/BossEncounter.java`
- Test: `src/adventure_game/Test/BossEncounterTest.java`

Note: this references `GameState.Mode.WON`, which does not exist until Task 9. To
keep this task self-contained and green, **add `WON` to the enum as the first step
here** (the rest of the GameState wiring is Task 9).

- [ ] **Step 1: Add `WON` to the Mode enum**

In `src/adventure_game/GameState.java` line 15, change:

```java
    public enum Mode { EXPLORE, COMBAT, DEAD }
```
to:
```java
    public enum Mode { EXPLORE, COMBAT, DEAD, WON }
```

- [ ] **Step 2: Write the failing test**

Create `src/adventure_game/Test/BossEncounterTest.java`:

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.BossEncounter;
import adventure_game.GameState;
import adventure_game.MessageLog;
import adventure_game.Player;
import adventure_game.Room;

public class BossEncounterTest {

    @Test
    void nonCureBossRoomReturnsExploreAndClearsRoom() {
        Room room = new Room(22, "RatKing", "lair");
        room.setBossNPC();
        Player p = new Player("Mick", 150, 0, 75);
        GameState.Mode next = BossEncounter.onDefeat(room, p, new MessageLog());
        assertEquals(GameState.Mode.EXPLORE, next);
        assertEquals(-1, room.hasNPC(), "boss room cleared (no respawn)");
    }

    @Test
    void cureRoomReturnsWon() {
        Room room = new Room(24, "Fauccis Lair", "cure");
        room.setBossNPC();
        room.setRoomCure();
        Player p = new Player("Mick", 150, 0, 75);
        GameState.Mode next = BossEncounter.onDefeat(room, p, new MessageLog());
        assertEquals(GameState.Mode.WON, next);
    }

    @Test
    void awardsBossBountyOfThreeLevelTicks() {
        Room room = new Room(10, "Cure Lab", "x");
        room.setBossNPC();
        Player p = new Player("Mick", 150, 0, 75); // level 0, levelUpXp 0
        BossEncounter.onDefeat(room, p, new MessageLog());
        // 3 levelModifier ticks -> one level up, xp counter back to 0.
        assertEquals(1, p.getLevel());
        assertEquals(0, p.getlevelUpXp());
    }
}
```

- [ ] **Step 3: Run to verify it fails**

Run: `.\build.ps1`
Expected: FAIL — `cannot find symbol: class BossEncounter`.

- [ ] **Step 4: Create `BossEncounter`**

Create `src/adventure_game/BossEncounter.java`:

```java
package adventure_game;

/**
 * Pure resolution of a defeated boss: award the bounty, clear the room, and decide
 * the next mode. Kept separate from GameState so the win/clear decision is unit-
 * testable without navigating to a boss room.
 */
public final class BossEncounter {

    private BossEncounter() {}

    /** Apply the boss-kill reward and return the next game mode. */
    public static GameState.Mode onDefeat(Room room, Player player, MessageLog log) {
        log.append("You have slain a boss. You grow stronger.");
        player.levelModifier(log);
        player.levelModifier(log);
        player.levelModifier(log);
        room.removeNPC();
        if (room.hasCure()) {
            log.append("You secured the cure. Humanity has a chance.");
            return GameState.Mode.WON;
        }
        log.append("The monstrosity falls. The way ahead is clear.");
        return GameState.Mode.EXPLORE;
    }
}
```

- [ ] **Step 5: Run to verify it passes**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.BossEncounterTest --disable-banner`
Expected: PASS (3 tests).

- [ ] **Step 6: Commit**

```powershell
git add src/adventure_game/GameState.java src/adventure_game/BossEncounter.java src/adventure_game/Test/BossEncounterTest.java
git commit -m "feat: Mode.WON and BossEncounter win/clear resolution"
```

---

## Task 9: Wire bosses into `GameState` (start fight, defeat branch, cure placement, room accessor)

**Files:**
- Modify: `src/adventure_game/GameState.java`
- Test: `src/adventure_game/Test/GameStateTest.java`

- [ ] **Step 1: Add the failing tests**

Append to `src/adventure_game/Test/GameStateTest.java` (inside the class):

```java
    @Test
    void cureIsPlacedInRoomTwentyFour() throws Exception {
        GameState gs = GameState.loadHospital();
        assertTrue(gs.getRoom(24).hasCure(), "Faucci's Lair holds the cure");
    }

    @Test
    void bossRoomsAreFlaggedAsBosses() throws Exception {
        GameState gs = GameState.loadHospital();
        for (int r : new int[]{10, 19, 22, 24}) {
            assertEquals(4, gs.getRoom(r).hasNPC(), "room " + r + " is a boss room");
        }
    }
```

- [ ] **Step 2: Run to verify it fails**

Run: `.\build.ps1`
Expected: FAIL — `cannot find symbol: method getRoom(int)`.

- [ ] **Step 3: Add the `getRoom` accessor**

In `src/adventure_game/GameState.java`, in the accessors block (near line 187), add:

```java
    /** Read-only access to a room by its number (== list index). */
    public Room getRoom(int roomNumber) { return rooms.get(roomNumber); }
```

- [ ] **Step 4: Place the cure at load**

In `loadHospital()` (after `gs.populateZombies();`, around line 36) add:

```java
        gs.rooms.get(24).setRoomCure();
```

- [ ] **Step 5: Run to verify the two new tests pass**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.GameStateTest --disable-banner`
Expected: PASS (existing + 2 new).

- [ ] **Step 6: Start the boss fight in `tryEncounter`**

In `tryEncounter()`, replace the `npc == 4` branch (currently logs "Something massive stirs..."):

```java
        } else if (npc == 4) {
            enemy = BossFactory.forRoom(currentRoom.getRoomNumber());
            mode = Mode.COMBAT;
            log.append("Something massive blocks your path: " + enemy.getName() + "!");
```

- [ ] **Step 7: Branch on boss defeat in `onEnemyDefeated`**

In `onEnemyDefeated()`, insert the boss branch immediately after the death log line
(`log.append(enemy.getName() + " has died.");`, line 170), before the regular
`player.levelModifier(log);`:

```java
        if (enemy instanceof Boss) {
            Mode next = BossEncounter.onDefeat(currentRoom, player, log);
            enemy = null;
            mode = next;
            return;
        }
```

- [ ] **Step 8: Run the full suite (regression + boss wiring compiles)**

Run: `.\test.ps1`
Expected: all green. (Boss-start and win transition through the live UI loop are
verified by the manual run in Task 13; the decision logic is covered by
`BossEncounterTest`, and placement by the tests above.)

- [ ] **Step 9: Commit**

```powershell
git add src/adventure_game/GameState.java src/adventure_game/Test/GameStateTest.java
git commit -m "feat: wire bosses and cure into GameState explore/combat loop"
```

---

## Task 10: Phase indicator on the combat banner (`Scene.bannerFor`)

Extract the enemy banner string into a public static helper so the boss phase
indicator (`[P2/3]`) is unit-testable with a real `Boss`, then use it in `render`.

**Files:**
- Modify: `src/adventure_game/engine/Scene.java`
- Test: `src/adventure_game/Test/SceneTest.java`

- [ ] **Step 1: Add the failing test**

Append to `src/adventure_game/Test/SceneTest.java` (inside the class):

```java
    @Test
    void bannerForRegularEnemyHasNoPhaseIndicator() {
        adventure_game.NPC walker = new adventure_game.NPC("Walker", 100, 2, 15);
        assertEquals("Walker Lv.2", Scene.bannerFor(walker));
    }

    @Test
    void bannerForBossShowsPhaseIndicator() {
        adventure_game.Boss boss = adventure_game.BossFactory.forRoom(24);
        String banner = Scene.bannerFor(boss);
        assertTrue(banner.startsWith("Faucci Lv.20"), banner);
        assertTrue(banner.contains("[P1/3]"), banner);
    }
```

- [ ] **Step 2: Run to verify it fails**

Run: `.\build.ps1`
Expected: FAIL — `cannot find symbol: method bannerFor`.

- [ ] **Step 3: Add `bannerFor` and use it in render**

In `src/adventure_game/engine/Scene.java`, add the import near the other
`adventure_game` imports (top of file):

```java
import adventure_game.Boss;
```

Add the public helper (e.g. just below the constructor):

```java
    /** Combat banner: "Name Lv.N", plus "[P2/3]" for bosses. */
    public static String bannerFor(NPC enemy) {
        String banner = enemy.getName() + " Lv." + enemy.getLevel();
        if (enemy instanceof Boss) {
            banner += " [" + ((Boss) enemy).getPhaseLabel() + "]";
        }
        return banner;
    }
```

In `render`, replace the banner line (currently
`s.drawText(bx, VIEW_TOP + 1, enemy.getName() + " Lv." + enemy.getLevel());`) with:

```java
            s.drawText(bx, VIEW_TOP + 1, bannerFor(enemy));
```

- [ ] **Step 4: Run to verify it passes**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.SceneTest --disable-banner`
Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add src/adventure_game/engine/Scene.java src/adventure_game/Test/SceneTest.java
git commit -m "feat: phase indicator on the boss combat banner"
```

---

## Task 11: Win screen — `Scene.winSummary` + cure-art field + WON render

Add a testable `winSummary(Player)` (the victory message + final stats lines), give
`Scene` a cure-art asset, and render a dedicated win screen when `mode == WON`. This
changes the `Scene` constructor (adds a `cureArt` parameter), so `GameApp` and the
test's `buildScene()` are updated.

**Files:**
- Modify: `src/adventure_game/engine/Scene.java`
- Modify: `src/adventure_game/Test/SceneTest.java`
- Modify: `src/adventure_game/GameApp.java` (constructor call only; full WON wiring in Task 12)

- [ ] **Step 1: Add the failing test**

Append to `src/adventure_game/Test/SceneTest.java` (inside the class):

```java
    @Test
    void winSummaryShowsVictoryLineAndFinalStats() {
        GameState gs = adventure_game.GameState.loadHospital();
        java.util.List<String> lines = Scene.winSummary(gs.getPlayer());
        String joined = String.join("\n", lines);
        assertTrue(joined.contains("secured the cure"), joined);
        assertTrue(joined.contains("Lv."), joined);
        assertTrue(joined.contains("DMG"), joined);
    }
```

Also update `buildScene()` in the same file to pass a cure-art asset to the new
constructor (do this in Step 3 once the constructor changes; noted here so the file
compiles).

- [ ] **Step 2: Run to verify it fails**

Run: `.\build.ps1`
Expected: FAIL — `cannot find symbol: method winSummary`.

- [ ] **Step 3: Implement `winSummary`, the cure-art field, and WON render**

In `src/adventure_game/engine/Scene.java`:

Add a field and constructor parameter. Change the field block + constructor to:

```java
    private final RoomArt roomArt;
    private final ArtAsset cureArt;

    public Scene(int width, int height, ArtAsset playerArt,
                 Map<String, ArtAsset> enemyArt, ArtAsset enemyFallback,
                 RoomArt roomArt, ArtAsset cureArt) {
        this.width = width;
        this.height = height;
        this.playerArt = playerArt;
        this.enemyArt = enemyArt;
        this.enemyFallback = enemyFallback;
        this.roomArt = roomArt;
        this.cureArt = cureArt;
    }
```

Add the win-summary helper:

```java
    /** Victory message + final run stats for the win screen. */
    public static List<String> winSummary(Player p) {
        List<String> lines = new java.util.ArrayList<>();
        lines.add("You secured the cure. Humanity has a chance.");
        lines.add("");
        lines.add("Final stats:");
        lines.add("  Lv." + p.getLevel());
        lines.add("  DMG " + p.getEffectiveDamage() + "  (" + p.getWeaponName() + ")");
        lines.add("  HP " + p.getHealth() + "/" + p.getMaxHealth());
        lines.add("  Bandages " + p.bandageCount());
        return lines;
    }
```

At the very start of `render`, after `s.clear();`, short-circuit to the win screen:

```java
        if (state.getMode() == GameState.Mode.WON) {
            s.blit(cureArt, Math.max(0, (width - cureArt.width()) / 2), VIEW_TOP);
            List<String> summary = winSummary(state.getPlayer());
            for (int i = 0; i < summary.size(); i++) {
                s.drawText(2, LOG_TOP + 1 + i, summary.get(i));
            }
            return;
        }
```

- [ ] **Step 4: Update `buildScene()` in `SceneTest.java`**

Change the return line in `buildScene()` to pass a small cure-art stub:

```java
        ArtAsset cure = ArtAsset.fromLines("CURE");
        return new Scene(60, 24, player, enemyArt, walker, roomArt, cure);
```

- [ ] **Step 5: Update `GameApp` constructor call (keep it compiling)**

In `src/adventure_game/GameApp.java`, the `scene = new Scene(...)` call now needs a
cure-art argument. For now pass a placeholder asset (real `cure.txt` arrives in
Task 12). Add before the `scene = ...` line:

```java
        ArtAsset cureArt = ArtAsset.fromLines("(cure)");
```
and change the constructor call to:
```java
        scene = new Scene(COLS, ROWS, playerArt, enemyArt, enemyFallback, roomArt, cureArt);
```

- [ ] **Step 6: Run to verify it passes**

Run: `.\test.ps1`
Expected: all green (SceneTest updated, GameApp compiles).

- [ ] **Step 7: Commit**

```powershell
git add src/adventure_game/engine/Scene.java src/adventure_game/Test/SceneTest.java src/adventure_game/GameApp.java
git commit -m "feat: win screen rendering (cure art + final stats) on Mode.WON"
```

---

## Task 12: Boss sprites, cure art, and `GameApp` wiring (sprites + WON card)

Author the ASCII art and finish the Swing wiring: boss sprites keyed by name, the
real cure art, and the WON south-panel card.

**Files:**
- Create: `assets/art/cure.txt`, `assets/art/hivemind.txt`, `assets/art/zombienest.txt`, `assets/art/ratking.txt`, `assets/art/faucci.txt`
- Modify: `src/adventure_game/GameApp.java`
- Possibly modify: the `ascii-art` skill (boss-size note) — see Step 1

- [ ] **Step 1: Author the ASCII art via the `ascii-art` skill**

Invoke the `ascii-art` skill (it governs sprite sizing/format and BOM-free writing).
Per spec §6, regular entities are 5×5 but bosses are larger — author boss sprites at
**≤8 wide × ≤6 tall** and add a one-line "boss exception" note to the skill if it
hard-mandates 5×5. Author:
- `assets/art/hivemind.txt`, `assets/art/zombienest.txt`, `assets/art/ratking.txt`,
  `assets/art/faucci.txt` (≤8×6 each).
- `assets/art/cure.txt` (a vial/cross motif; keep ≤ ~40 wide × ~8 tall so it fits
  the viewport area above the log).

**Critical:** all files must be UTF-8 **without BOM** — Java's `Files.readAllLines`
does not strip a BOM and it corrupts rendering. Write with:
`[System.IO.File]::WriteAllLines(path, lines, (New-Object System.Text.UTF8Encoding($false)))`.
(The `ascii-art` skill / project convention covers this.)

- [ ] **Step 2: Verify the art is BOM-free and loads**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.AssetSanityTest --disable-banner`
Expected: PASS — `allArtFilesAreBomFree` covers the new files (it walks all of
`assets/art`). If it fails for a new file, rewrite that file BOM-free.

- [ ] **Step 3: Wire boss sprites + real cure art + WON card into `GameApp`**

In `src/adventure_game/GameApp.java`:

Add boss sprites to the `enemyArt` map (after the existing `enemyArt.put(...)` lines,
keys must equal the boss `getName()`):

```java
        enemyArt.put("HiveMind", ArtAsset.fromFile("assets/art/hivemind.txt"));
        enemyArt.put("Zombie Nest", ArtAsset.fromFile("assets/art/zombienest.txt"));
        enemyArt.put("RatKing", ArtAsset.fromFile("assets/art/ratking.txt"));
        enemyArt.put("Faucci", ArtAsset.fromFile("assets/art/faucci.txt"));
```

Replace the Task-11 placeholder cure art with the real file:

```java
        ArtAsset cureArt = ArtAsset.fromFile("assets/art/cure.txt");
```

Register a WON card in the south panel (after the DEAD card line, ~line 60):

```java
        south.add(buildWonPanel(), GameState.Mode.WON.name());
```

Add the panel builder (next to `buildDeadPanel`):

```java
    private JPanel buildWonPanel() {
        JPanel p = new JPanel(new GridLayout(1, 1));
        p.add(new JLabel("  You win! You secured the cure. Close the window to quit.  "));
        return p;
    }
```

- [ ] **Step 4: Run the full suite (everything compiles, model green)**

Run: `.\test.ps1`
Expected: all green.

- [ ] **Step 5: Commit**

```powershell
git add assets/art/cure.txt assets/art/hivemind.txt assets/art/zombienest.txt assets/art/ratking.txt assets/art/faucci.txt src/adventure_game/GameApp.java
git commit -m "feat: boss sprites, cure art, and WON panel wiring"
```

---

## Task 13: Balance pass + manual verification

Confirm the fights are winnable and the win flow works end-to-end in the real app.

**Files:** none (tuning edits to `Boss.java` constants / `BossFactory` patterns only if needed)

- [ ] **Step 1: Full automated suite**

Run: `.\test.ps1`
Expected: every test green (target: prior 55 + the new Boss/BossFactory/BossEncounter/
Scene/GameState tests).

- [ ] **Step 2: Manual playthrough**

Run: `.\run.ps1`
Walk to a boss room (e.g. clear toward room 10) and verify:
- The fight starts (no "not yet" hint); the banner shows `HiveMind Lv.6 [P1/3]`.
- As HP drops, the indicator advances `[P2/3]` → `[P3/3]` and the move pattern
  changes; the signature move appears in the log (summon/swarm/enrage/heal as
  appropriate to the boss).
- A leveled, armed player can win; on defeating Faucci (room 24) the WON screen
  shows the cure art + final stats and movement/combat stop.

- [ ] **Step 3: Balance tuning (only if needed)**

If a boss is unwinnable or trivial, adjust the constants in `Boss.java`
(`SLAM_MULTIPLIER`, `SUMMON_*`, `SWARM_*`, `ENRAGE_MULTIPLIER`, `HEAL_FRACTION`) or
the per-boss patterns in `BossFactory`. Re-run `.\test.ps1` (some Boss unit tests
assert specific multipliers, e.g. SLAM≈1.8 and HEAL=600 at 20000 max — update those
asserts in lock-step if you change the constant). Commit any tuning:

```powershell
git add src/adventure_game/Boss.java src/adventure_game/BossFactory.java src/adventure_game/Test/BossTest.java
git commit -m "balance: tune boss damage/pattern constants"
```

- [ ] **Step 4: Final confirmation**

Run: `.\test.ps1`
Expected: all green. Phase 2b-2 complete: explore → loot → fight → level → beat four
bosses → win.

---

## Self-Review (performed against the spec)

- **Spec coverage:** Boss model (§2) → Tasks 1–6; BossFactory + roster/patterns (§2.4–2.5) → Task 7; GameState wiring incl. Mode.WON, boss start, defeat/win, cure placement (§3) → Tasks 8–9; phase indicator (§4.1) → Task 10; win screen (§4.2) → Tasks 11–12; sprites + ascii-art exception (§6) → Task 12; balance (§9 risk) → Task 13. All covered.
- **Signature mapping (locked):** room 19 = Zombie Nest (5000), room 22 = RatKing (7500) — asserted in `BossFactoryTest`.
- **Type consistency:** `Move`, `Phase(double, Move, Move[])` with `gatePercent()/onEnter()/moveForTurn(int)`, `Boss(String,int,int,int,Phase[])` with `getPhaseLabel()/activeHordeTurns()/getEffectiveDamage()`, `BossFactory.forRoom(int)`, `BossEncounter.onDefeat(Room,Player,MessageLog)→GameState.Mode`, `Scene.bannerFor(NPC)`, `Scene.winSummary(Player)`, `Scene` ctor with trailing `ArtAsset cureArt`, `GameState.getRoom(int)` — names are consistent across all tasks.
- **No placeholders:** every code step shows complete code; every test step shows the test; every run step shows the exact command + expected result.
- **Test seam note:** the boss-fight *start* and live WON transition go through Swing-driven navigation and are verified by the Task 13 manual run; the decision/placement/label/summary logic is fully unit-tested (BossEncounter, BossFactory, GameState placement, Scene helpers). This split is intentional and matches how prior phases verified GUI.

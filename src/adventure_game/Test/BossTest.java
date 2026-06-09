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

    @Test
    void summonArmsAThreeTurnHordeThatChipsThenStops() {
        Player target = new Player("Mick", 100000, 0, 1);
        MessageLog log = new MessageLog();
        // Pattern: SUMMON, then STRIKEs. The horde chip is observed via the turn
        // counter (activeHordeTurns) rather than HP, so STRIKE damage is irrelevant.
        Boss b = new Boss("Hive", 1000, 6, 40, new Phase[]{
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
}

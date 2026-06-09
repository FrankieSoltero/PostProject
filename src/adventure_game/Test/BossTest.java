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
}

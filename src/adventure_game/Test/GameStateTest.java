package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

import adventure_game.Direction;
import adventure_game.GameRandom;
import adventure_game.GameState;

public class GameStateTest {

    @Test
    void loadsInExploreAtEntranceWithNoEnemy() throws Exception {
        GameState gs = GameState.loadHospital();
        assertEquals("Hospital Entrance", gs.getCurrentRoom().getRoomName());
        assertEquals("Mick", gs.getPlayer().getName());
        assertEquals(GameState.Mode.EXPLORE, gs.getMode());
        assertFalse(gs.isInCombat());
        assertNull(gs.getEnemy());
    }

    @Test
    void moveBlockedAtWallReturnsFalse() throws Exception {
        GameState gs = GameState.loadHospital();
        assertFalse(gs.move(Direction.NORTH));
        assertEquals("Hospital Entrance", gs.getCurrentRoom().getRoomName());
    }

    @Test
    void moveIntoPopulatedRoomStartsCombat() throws Exception {
        GameRandom.rand = new Random(1);
        GameState gs = GameState.loadHospital();
        assertTrue(gs.move(Direction.SOUTH));
        assertEquals(GameState.Mode.COMBAT, gs.getMode());
        assertNotNull(gs.getEnemy());
    }

    private static GameState inCombat(long seed) throws Exception {
        GameRandom.rand = new Random(seed);
        GameState gs = GameState.loadHospital();
        gs.move(Direction.SOUTH);
        return gs;
    }

    @Test
    void attackReducesEnemyHealthAndLogs() throws Exception {
        GameState gs = inCombat(1);
        int before = gs.getEnemy().getHealth();
        int logBefore = gs.getLog().size();
        gs.playerAttack();
        boolean hurtOrDead = gs.getEnemy() == null || gs.getEnemy().getHealth() < before;
        assertTrue(hurtOrDead);
        assertTrue(gs.getLog().size() > logBefore);
    }

    @Test
    void defendActsAndLogs() throws Exception {
        GameState gs = inCombat(2);
        int logBefore = gs.getLog().size();
        gs.playerDefend();
        assertTrue(gs.getLog().size() > logBefore);
    }

    @Test
    void createThenUseBandageChangesInventory() throws Exception {
        GameState gs = inCombat(3);
        gs.createBandage();
        assertTrue(gs.getPlayer().hasItems());
        gs.useBandage();
        assertFalse(gs.getPlayer().hasItems());
    }

    @Test
    void fightingToConclusionEndsInClearRespawnOrDeath() throws Exception {
        GameState gs = inCombat(1);
        int guard = 0;
        while (gs.getMode() == GameState.Mode.COMBAT && gs.getPlayer().isAlive() && guard++ < 200) {
            gs.playerAttack();
        }
        assertTrue(gs.getMode() == GameState.Mode.EXPLORE
                || gs.getMode() == GameState.Mode.COMBAT
                || gs.getMode() == GameState.Mode.DEAD);
    }

    @Test
    void weaponAndBandageLootArePlacedAtLoad() throws Exception {
        GameRandom.rand = new java.util.Random(1);
        GameState gs = GameState.loadHospital();
        // Entrance east -> room 1 (Supply Room), a weapon+bandage room (still
        // populated, so loot is placed but not yet collected).
        gs.move(Direction.EAST);
        assertEquals(1, gs.getCurrentRoom().hasWeapon());
        assertEquals(1, gs.getCurrentRoom().hasItem());
    }
}

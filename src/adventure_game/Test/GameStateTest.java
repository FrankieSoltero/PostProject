package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.GameState;

public class GameStateTest {

    @Test
    void loadsHospitalWithEntrancePlayerAndEnemy() throws Exception {
        GameState gs = GameState.loadHospital();
        assertEquals("Hospital Entrance", gs.getCurrentRoom().getRoomName());
        assertEquals("Mick", gs.getPlayer().getName());
        assertNotNull(gs.getEnemy());
        assertTrue(gs.isInCombat());
    }

    @Test
    void playerAttackDamagesEnemyAndLogs() throws Exception {
        GameState gs = GameState.loadHospital();
        int before = gs.getEnemy().getHealth();
        int logBefore = gs.getLog().size();
        gs.playerAttack();
        assertTrue(gs.getEnemy().getHealth() < before || !gs.isInCombat());
        assertTrue(gs.getLog().size() > logBefore);
    }
}

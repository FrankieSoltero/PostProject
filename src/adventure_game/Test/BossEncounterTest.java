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

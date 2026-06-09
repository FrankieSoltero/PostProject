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

package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

import adventure_game.GameRandom;
import adventure_game.Loot;
import adventure_game.MessageLog;
import adventure_game.Player;
import adventure_game.Room;

public class LootTest {

    @Test
    void collectRoomGrantsWeaponAndBandageThenClearsFlags() {
        GameRandom.rand = new Random(1);
        Room r = new Room(0, "Test Room", "bio");
        r.setWeapon();
        r.setItem();
        Player p = new Player("Mick", 150, 0, 75);
        MessageLog log = new MessageLog();

        Loot.collectRoom(r, p, log);

        assertNotEquals("Fists", p.getWeaponName()); // any weapon (bonus>=5) beats fists
        assertTrue(p.bandageCount() >= 1);            // bandage collected
        assertEquals(-1, r.hasWeapon());              // flag cleared
        assertEquals(-1, r.hasItem());

        // Double-collect is a no-op (flags already cleared).
        String weaponBefore = p.getWeaponName();
        int bandagesBefore = p.bandageCount();
        Loot.collectRoom(r, p, log);
        assertEquals(weaponBefore, p.getWeaponName());
        assertEquals(bandagesBefore, p.bandageCount());
    }

    @Test
    void rollKillDropEventuallyDropsLoot() {
        GameRandom.rand = new Random(1);
        Player p = new Player("Mick", 150, 0, 75);
        MessageLog log = new MessageLog();
        for (int i = 0; i < 100; i++) {
            Loot.rollKillDrop(p, log);
        }
        // Over 100 rolls (~1/3 bandage, ~1/4 weapon each) drops are statistically certain.
        assertTrue(p.bandageCount() > 0, "expected at least one bandage drop");
        assertNotEquals("Fists", p.getWeaponName(), "expected at least one weapon drop");
    }
}

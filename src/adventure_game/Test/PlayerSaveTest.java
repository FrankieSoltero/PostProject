package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.MessageLog;
import adventure_game.Player;

public class PlayerSaveTest {

    @Test
    void fromSaveReproducesAllPlayerState() {
        Player p = Player.fromSave("Mick", 80, 200, 7, 2, 130, 45, "AssaultRifle", 3,
                new MessageLog());
        assertEquals("Mick", p.getName());
        assertEquals(80, p.getHealth());
        assertEquals(200, p.getMaxHealth());
        assertEquals(7, p.getLevel());
        assertEquals(2, p.getlevelUpXp());
        assertEquals(130, p.getBaseDamage());
        assertEquals(45, p.getWeaponBonus());
        assertEquals("AssaultRifle", p.getWeaponName());
        assertEquals(175, p.getEffectiveDamage()); // 130 + 45
        assertEquals(3, p.bandageCount());
    }
}

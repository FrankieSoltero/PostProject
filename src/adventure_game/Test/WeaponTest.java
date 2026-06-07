package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

import adventure_game.GameRandom;
import adventure_game.Weapon;

public class WeaponTest {

    @Test
    void exposesNameAndBonus() {
        Weapon w = new Weapon("Knife", 12);
        assertEquals("Knife", w.name());
        assertEquals(12, w.bonus());
    }

    @Test
    void randomWeaponBonusMatchesItsTier() {
        GameRandom.rand = new Random(42);
        for (int i = 0; i < 200; i++) {
            Weapon w = Weapon.randomWeapon();
            switch (w.name()) {
                case "Knife":        assertTrue(w.bonus() >= 5 && w.bonus() <= 20, "knife " + w.bonus()); break;
                case "Pistol":       assertTrue(w.bonus() >= 15 && w.bonus() <= 35, "pistol " + w.bonus()); break;
                case "AssaultRifle": assertTrue(w.bonus() >= 30 && w.bonus() <= 55, "ar " + w.bonus()); break;
                default: fail("unknown weapon " + w.name());
            }
        }
    }
}

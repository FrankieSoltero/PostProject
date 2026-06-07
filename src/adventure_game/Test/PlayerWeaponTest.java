package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

import adventure_game.GameRandom;
import adventure_game.MessageLog;
import adventure_game.NPC;
import adventure_game.Player;
import adventure_game.Weapon;

public class PlayerWeaponTest {

    @Test
    void startsUnarmedWithEffectiveDamageEqualToBase() {
        Player p = new Player("Mick", 150, 0, 75);
        assertEquals("Fists", p.getWeaponName());
        assertEquals(0, p.getWeaponBonus());
        assertEquals(75, p.getEffectiveDamage());
        assertEquals(0, p.bandageCount());
    }

    @Test
    void equippingStrongerWeaponRaisesDamageAndName() {
        Player p = new Player("Mick", 150, 0, 75);
        MessageLog log = new MessageLog();
        p.equipWeapon(new Weapon("AssaultRifle", 40), log);
        assertEquals("AssaultRifle", p.getWeaponName());
        assertEquals(40, p.getWeaponBonus());
        assertEquals(115, p.getEffectiveDamage());
    }

    @Test
    void weakerWeaponIsRejected() {
        Player p = new Player("Mick", 150, 0, 75);
        MessageLog log = new MessageLog();
        p.equipWeapon(new Weapon("AssaultRifle", 40), log);
        p.equipWeapon(new Weapon("Knife", 10), log);
        assertEquals("AssaultRifle", p.getWeaponName());
        assertEquals(40, p.getWeaponBonus());
        assertEquals(115, p.getEffectiveDamage());
    }

    @Test
    void npcEffectiveDamageIsItsBase() {
        NPC n = new NPC("Walker", 100, 0, 15);
        assertEquals(15, n.getEffectiveDamage());
    }

    @Test
    void attackUsesEffectiveDamageWhenArmed() {
        GameRandom.rand = new Random(5);
        MessageLog log = new MessageLog();

        Player unarmed = new Player("A", 150, 0, 10);
        NPC dummy1 = new NPC("Dummy", 100000, 0, 0);
        unarmed.attack(dummy1, log);
        int dmgUnarmed = 100000 - dummy1.getHealth();

        Player armed = new Player("B", 150, 0, 10);
        armed.equipWeapon(new Weapon("AssaultRifle", 90), log); // effective 100
        NPC dummy2 = new NPC("Dummy", 100000, 0, 0);
        armed.attack(dummy2, log);
        int dmgArmed = 100000 - dummy2.getHealth();

        assertTrue(dmgArmed > dmgUnarmed, "armed " + dmgArmed + " vs unarmed " + dmgUnarmed);
    }
}

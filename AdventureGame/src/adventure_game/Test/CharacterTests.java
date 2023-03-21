package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.Character;
import adventure_game.NPC;
import adventure_game.Player;
import adventure_game.items.antiBiotics;
import adventure_game.items.*;

import org.junit.jupiter.api.BeforeEach;

public class CharacterTests{

    private Character c;
    private Character b;
    private Consumable a;
    @BeforeEach
    void setup(){
        c = new Player("Hero", 100, 0, 10);
        b = new NPC("Enemy", 100, 1, 10);
        a = new antiBiotics();

    }

    @Test
    void testModifyHealth(){
        assertTrue(c.getHealth() == 100);
        c.modifyHealth(-10);
        assertTrue(c.getHealth() == 90);
    }
    @Test
    void testmodifyDamage() {
        assertTrue(c.getBaseDamage() == 10);
        c.modifyDamage(10);
        assertTrue(c.getBaseDamage() == 20);
    }
    @Test
    void testVulnerable() {
        assertFalse(c.isVulnerable());
        c.setAsVulnerable(1);
        assertTrue(c.isVulnerable());
        c.decreaseTurnsVulnerable();
        assertFalse(c.isVulnerable());
    }
    @Test
    void testsInvincible() {
        assertFalse(c.isInvincible());
        c.setAsInvincible(1);
        assertTrue(c.isInvincible());
        c.decreaseTurnsInvincible();
        assertFalse(c.isInvincible());
    }
    @Test
    void testStunned() {
        assertFalse(c.isStunned());
        c.setAsStunned(1);
        assertTrue(c.isStunned());
        c.decreaseTurnsStunned();
        assertFalse(c.isStunned());
    }
    @Test
    void testDamageBuff() {
        assertTrue(c.getDamageBuff() == 1.0);
        c.setTempDamageBuff(3);
        assertTrue(c.getDamageBuff() == 3.0);
    }
    @Test
    void obtainTest() {
        assertFalse(c.hasItems());
        c.obtain(a);
        assertTrue(c.hasItems());
    }
    @Test
    void testAttack() {
        assertTrue(b.getHealth() == 100);
        c.attack(b);
        assertTrue(b.getHealth() != 100);
        b.modifyHealth(100);
        b.setAsInvincible(1);
        c.attack(b);
        assertTrue(b.getHealth() == 100);
        b.setAsVulnerable(1);
        c.attack(b);
        assertTrue(b.getHealth() != 100);
    }
    @Test
    void testLeveling(){
        assertTrue(c.getMaxHealth() == 100);
        assertTrue(c.getBaseDamage() == 10);
        c.levelModifier();
        c.levelModifier();
        c.levelingUp();
        assertTrue(c.getMaxHealth() != 100);
        assertTrue(c.getMaxHealth() == c.getHealth());
        assertTrue(c.getBaseDamage() != 10);
        assertTrue(b.getMaxHealth() == 100);
        assertTrue(b.getBaseDamage() == 10);
        b.levelingUp();
        assertTrue(b.getHealth() != 100);
        assertTrue(b.getBaseDamage() != 10);
    }


}
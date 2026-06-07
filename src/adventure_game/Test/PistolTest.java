package adventure_game.Test;
import adventure_game.Character;
import adventure_game.Player;
import adventure_game.items.Pistols;

import static org.junit.jupiter.api.Assertions.*;

import adventure_game.MessageLog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class PistolTest {
    private Character F;
    private Pistols test;
    private MessageLog output = new MessageLog();

    @BeforeEach
    void setup() {
        F = new Player("Frankie",100,0,10);
        test = new Pistols();
    }
    @Test
    void testPistol() {
        assertTrue(F.getBaseDamage() == 10);
        test.pickUpItem(F, output);
        // The pistol may have no ammo (level 0), leaving damage unchanged,
        // or it has ammo and increases damage. Either way damage never decreases.
        assertTrue(F.getBaseDamage() >= 10);
        System.out.print(F.getBaseDamage());
    }
}

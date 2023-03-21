package adventure_game.Test;
import adventure_game.Character;
import adventure_game.Player;
import adventure_game.items.AssaultRifle;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class ARTest {
    private Character F;
    private AssaultRifle test;

    @BeforeEach
    void setup() {
        F = new Player("Frankie",100,0,10);
        test = new AssaultRifle();
    }
    @Test
    void testPistol() {
        assertTrue(F.getBaseDamage() == 10);
        test.pickUpItem(F);
        assertTrue(F.getBaseDamage() != 10);
        System.out.print(F.getBaseDamage());
    }
}


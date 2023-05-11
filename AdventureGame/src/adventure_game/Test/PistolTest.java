package adventure_game.Test;
import adventure_game.Character;
import adventure_game.Player;
import adventure_game.items.Pistols;

import static org.junit.jupiter.api.Assertions.*;

import javax.swing.JTextArea;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class PistolTest {
    private Character F;
    private Pistols test;
    private JTextArea output;

    @BeforeEach
    void setup() {
        F = new Player("Frankie",100,0,10);
        test = new Pistols();
    }
    @Test
    void testPistol() {
        assertTrue(F.getBaseDamage() == 10);
        test.pickUpItem(F, output);
        assertTrue(F.getBaseDamage() != 10);
        System.out.print(F.getBaseDamage());
    }
}


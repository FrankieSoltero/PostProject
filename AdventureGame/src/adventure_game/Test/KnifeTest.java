package adventure_game.Test;
import adventure_game.Character;
import adventure_game.Player;
import adventure_game.items.Knife;

import static org.junit.jupiter.api.Assertions.*;

import javax.swing.JTextArea;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class KnifeTest {
    private Character F;
    private Knife test;
    private JTextArea output;

    @BeforeEach
    void setup() {
        F = new Player("Frankie",100,10,10);
        test = new Knife();
    }
    @Test
    void testKnife() {
        assertTrue(F.getBaseDamage() == 10);
        test.pickUpItem(F, output);
        assertTrue(F.getBaseDamage() != 10);
    }
}


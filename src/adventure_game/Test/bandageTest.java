package adventure_game.Test;
import adventure_game.Character;
import adventure_game.Player;
import adventure_game.items.bandage;

import static org.junit.jupiter.api.Assertions.*;

import adventure_game.MessageLog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class bandageTest {

    private Character F;
    private bandage test;
    private MessageLog t = new MessageLog();

    
    @BeforeEach
    void setup(){
        // TO-DO 
        // Implement this
        F = new Player("Frankie",200,10,10);
        test = new bandage();

        
        


        

    }

    @Test
    void testbandage(){
        // Level-10 player, max health 200. The bandage heals a random amount,
        // but heals 0 when current health is already >= maxHealth - 20 (=180),
        // and never exceeds maxHealth. These assertions check those invariants
        // deterministically (independent of the random heal amount).
        assertTrue(F.getHealth() == 200);

        // At full health (>= 180), consuming heals nothing.
        test.consume(F, t);
        assertEquals(200, F.getHealth());

        // Drop well below the 180 threshold: a consume must increase health
        // and must never exceed max health.
        F.modifyHealth(-150); // 50
        int before = F.getHealth();
        test.consume(F, t);
        assertTrue(F.getHealth() > before);
        assertTrue(F.getHealth() <= F.getMaxHealth());

        // At 185 (>= 180 threshold) consuming heals nothing: stays exactly 185.
        F.modifyHealth(F.getMaxHealth()); // clamp back to 200
        F.modifyHealth(-15);              // 185
        test.consume(F, t);
        assertEquals(185, F.getHealth());
    }
}
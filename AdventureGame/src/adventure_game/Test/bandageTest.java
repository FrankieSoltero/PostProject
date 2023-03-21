package adventure_game.Test;
import adventure_game.Character;
import adventure_game.Player;
import adventure_game.items.bandage;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class bandageTest {

    private Character F;
    private bandage test;

    
    @BeforeEach
    void setup(){
        // TO-DO 
        // Implement this
        F = new Player("Frankie",200,10,10);
        test = new bandage();

        
        


        

    }

    @Test
    void testbandage(){
        // TO-DO
        // Implement this
        assertTrue(F.getHealth() == 200);
        F.modifyHealth(-100);
        test.consume(F);
        assertFalse(F.getHealth() == 200);
        F.modifyHealth(200);
        F.modifyHealth(-100);
        assertTrue(F.getHealth() == 100);
        test.consume(F);
        assertTrue(F.getHealth() <= 190);
        F.modifyHealth(100);
        F.modifyHealth(-15);
        test.consume(F);
        assertTrue(F.getHealth() == 185);



       

        


    }
}

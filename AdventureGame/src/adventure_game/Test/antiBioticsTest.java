package adventure_game.Test;
import adventure_game.Character;
import adventure_game.Player;
import adventure_game.items.antiBiotics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class antiBioticsTest {

    private Character F;
    private antiBiotics test;

    
    @BeforeEach
    void setup(){
        // TO-DO 
        // Implement this
        F = new Player("Frankie",100,10,10);
        test = new antiBiotics();

        
        


        

    }

    @Test
    void testantiBiotics(){
        // TO-DO
        // Implement this
        assertTrue(F.getHealth() == 100);
        F.modifyHealth(-50);
        //test.consume(F);
        assertFalse(F.getHealth() == 50);



       

        


    }
}

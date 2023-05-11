package adventure_game.items;

import javax.swing.JTextArea;

import adventure_game.Character;
import adventure_game.GameWindow;
 
public class bandage implements Consumable {

    
   /**
     * This method allows for the Character to consume a bandage to
     * heal them instantly during a fight. If hitpoints is greater than
     * max hitpoints - 20 then the owner resets to max health - 20.
     * @param owner of type Character
     */
   
    private int calculateHealing(){
        // Equivalent to rolling 4d4 + 4
        // sum up four random values in the range [1,4] and
        // add 4 to that.
        int points = GameWindow.rand.nextInt(40)+51;
        return points;
    }
    


    
    @Override
    /**
     * The consume method allows for the bandage to heal the Character up to 90 hitpoints
     * @param owner of type Character
     */
    public void consume(Character owner, JTextArea output) {
        int hitPoints = calculateHealing();
        int hitPointsfromMax = owner.getMaxHealth() - 20;

        if (owner.getHealth() >= hitPointsfromMax) {
            hitPoints = 0;
            output.append("You are above " + hitPointsfromMax + " therefore you do not heal\n.");
        }
        owner.modifyHealth(hitPoints);
        output.append("You heal for " + hitPoints + " points, back up to " + owner.getHealth() + "/" + owner.getMaxHealth() + "\n");
        
    }
}
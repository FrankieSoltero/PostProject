package adventure_game.items;

import javax.swing.JTextArea;

import adventure_game.Character;
import adventure_game.GameWindow;

public class Knife implements Weapons {

    /**
     * This method modifies the damage in correlation to the knife level
     * 
     * @param owner of type Character
     */
    public void pickUpItem(Character owner, JTextArea output){
        int level = calculateKnifeLevel();
        int damagePoints;
        
        if (level == 0) {
            output.append("The knife you found is broken. Get Better.\n");
        }
        else if (level == 1) {
            damagePoints = level * 5;
            owner.modifyDamage(damagePoints);
            output.append("You have picked up a level 1 knife. Your damage has increased by " + damagePoints + " hitpoints, your total damage is now " + owner.getBaseDamage() + "\n");
        }
        else if (level == 2) {
            damagePoints = level * 5;
            owner.modifyDamage(damagePoints);
            output.append("You have picked up a level 2 knife. Your damage has increased by " + damagePoints + " hitpoints, your total damage is now " + owner.getBaseDamage() + "\n");
        }
        else if (level == 3) {
            damagePoints = level * 5;
            owner.modifyDamage(damagePoints);
            output.append("You have picked up a level 3 knife. Your damage has increased by " + damagePoints + " hitpoints, your total damage is now " + owner.getBaseDamage() + "\n");
        }
        else if (level == 4) {
            damagePoints = level * 5;
            owner.modifyDamage(damagePoints);
            output.append("You have picked up a level 4 knife. Your damage has increased by " + damagePoints + " hitpoints, your total damage is now " + owner.getBaseDamage() + "\n");
        }
        else {
            damagePoints = level * 5;
            owner.modifyDamage(damagePoints);
            output.append("You have picked up a level 5 knife. Your damage has increased by " + damagePoints + " hitpoints, your total damage is now " + owner.getBaseDamage() + "\n");
        }

    }



    private int calculateKnifeLevel() {
        int p = GameWindow.rand.nextInt(6);
        return p;
    }
}

package adventure_game.items;

import javax.swing.JTextArea;

import adventure_game.Character;
import adventure_game.GameWindow;

public class Pistols implements Weapons {

    @Override
     /**
     * This method allows an Item to be picked up by a character
     * @param owner of type Character
     */
    public void pickUpItem(Character owner, JTextArea output) {
        int level = calculatePistolLevel();
        int damagePoints;
        
        if (level == 0) {
            output.append("The pistol you found has no ammo. Get Better.");
        }
        else if (level == 1) {
            damagePoints = level * 10;
            owner.modifyDamage(damagePoints);
            output.append("You have picked up a level 1 Pistol Your damage has increased by " + damagePoints + " hitpoints, your total damage is now " + owner.getBaseDamage() + "\n");
        }
        else if (level == 2) {
            damagePoints = level * 10;
            owner.modifyDamage(damagePoints);
            output.append("You have picked up a level 2 Pistol Your damage has increased by " + damagePoints + " hitpoints, your total damage is now " + owner.getBaseDamage() + "\n");
        }
        else if (level == 3) {
            damagePoints = level * 10;
            owner.modifyDamage(damagePoints);
            output.append("You have picked up a level 3 Pistol Your damage has increased by " + damagePoints + " hitpoints, your total damage is now " + owner.getBaseDamage() + "\n");
        }
        else if (level == 4) {
            damagePoints = level * 10;
            owner.modifyDamage(damagePoints);
            output.append("You have picked up a level 4 Pistol Your damage has increased by " + damagePoints + " hitpoints, your total damage is now " + owner.getBaseDamage() + "\n");
        }
        else {
            damagePoints = level * 10;
            owner.modifyDamage(damagePoints);
            output.append("You have picked up a level 5 Pistol Your damage has increased by " + damagePoints + " hitpoints, your total damage is now " + owner.getBaseDamage() + "\n");
        }

    }
    private int calculatePistolLevel() {
        int p = GameWindow.rand.nextInt(6);
        return p;
    }
}

package adventure_game.items;

import adventure_game.Character;
import adventure_game.Game;

public class Knife implements Weapons {

    /**
     * This method modifies the damage in correlation to the knife level
     * 
     * @param owner of type Character
     */
    public void pickUpItem(Character owner){
        int level = calculateKnifeLevel();
        int damagePoints;
        
        if (level == 0) {
            System.out.println("The knife you found is broken. Get Better.");
        }
        else if (level == 1) {
            damagePoints = level * 5;
            owner.modifyDamage(damagePoints);
            System.out.printf("You have picked up a level 1 knife. Your damage has increased by %d hitpoints, your total damage is now %d.",damagePoints,owner.getBaseDamage());
        }
        else if (level == 2) {
            damagePoints = level * 5;
            owner.modifyDamage(damagePoints);
            System.out.printf("You have picked up a level 2 knife. Your damage has increased by %d hitpoints, your total damage is now %d.", damagePoints, owner.getBaseDamage());
        }
        else if (level == 3) {
            damagePoints = level * 5;
            owner.modifyDamage(damagePoints);
            System.out.printf("You have picked up a level 3 knife. Your damage has increased by %d hitpoints, your total damage is now %d.", damagePoints, owner.getBaseDamage());
        }
        else if (level == 4) {
            damagePoints = level * 5;
            owner.modifyDamage(damagePoints);
            System.out.printf("You have picked up a level 4 knife. Your damage has increased by %d hitpoints, your total damage is now %d.", damagePoints, owner.getBaseDamage());
        }
        else {
            damagePoints = level * 5;
            owner.modifyDamage(damagePoints);
            System.out.printf("You have picked up a level 5 knife. Your damage has increased by %d hitpoints, your total damage is now %d.", damagePoints, owner.getBaseDamage());
        }

    }



    private int calculateKnifeLevel() {
        int p = Game.rand.nextInt(5);
        return p;
    }
}

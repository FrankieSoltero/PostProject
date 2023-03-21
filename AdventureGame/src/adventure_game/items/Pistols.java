package adventure_game.items;

import adventure_game.Character;
import adventure_game.Game;

public class Pistols implements Weapons {

    @Override
     /**
     * This method allows an Item to be picked up by a character
     * @param owner of type Character
     */
    public void pickUpItem(Character owner) {
        int level = calculatePistolLevel();
        int damagePoints;
        
        if (level == 0) {
            System.out.println("The pistol you found has no ammo. Get Better.");
        }
        else if (level == 1) {
            damagePoints = level * 10;
            owner.modifyDamage(damagePoints);
            System.out.printf("You have picked up a level 1 Pistol. Your damage has increased by %d hitpoints, your total damage is now %d.",damagePoints,owner.getBaseDamage());
        }
        else if (level == 2) {
            damagePoints = level * 10;
            owner.modifyDamage(damagePoints);
            System.out.printf("You have picked up a level 2 Pistol. Your damage has increased by %d hitpoints, your total damage is now %d.", damagePoints, owner.getBaseDamage());
        }
        else if (level == 3) {
            damagePoints = level * 10;
            owner.modifyDamage(damagePoints);
            System.out.printf("You have picked up a level 3 Pistol. Your damage has increased by %d hitpoints, your total damage is now %d.", damagePoints, owner.getBaseDamage());
        }
        else if (level == 4) {
            damagePoints = level * 10;
            owner.modifyDamage(damagePoints);
            System.out.printf("You have picked up a level 4 Pistol. Your damage has increased by %d hitpoints, your total damage is now %d.", damagePoints, owner.getBaseDamage());
        }
        else {
            damagePoints = level * 10;
            owner.modifyDamage(damagePoints);
            System.out.printf("You have picked up a level 5 Pistol. Your damage has increased by %d hitpoints, your total damage is now %d.", damagePoints, owner.getBaseDamage());
        }

    }
    private int calculatePistolLevel() {
        int p = Game.rand.nextInt(6);
        return p;
    }
}

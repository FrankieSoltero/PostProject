package adventure_game.items;

import adventure_game.Character;
import adventure_game.Game;

public class AssaultRifle implements Weapons {

    @Override
    /**
     * This method allows an Item to be picked up by a character
     * @param owner of type Character
     */
    public void pickUpItem(Character owner) {
        int level = calculateARLevel();
        int damagePoints;
        
        if (level == 0) {
            System.out.println("The Assault Rifle you found has no ammo. Get Better.");
        }
        else if (level == 1) {
            damagePoints = level * 10;
            owner.modifyDamage(damagePoints);
            System.out.printf("You have picked up a level 1 Assualt Rifle. Your damage has increased by %d hitpoints, your total damage is now %d.",damagePoints,owner.getBaseDamage());
        }
        else if (level == 2) {
            damagePoints = level * 10;
            owner.modifyDamage(damagePoints);
            System.out.printf("You have picked up a level 2 Assault Rifle. Your damage has increased by %d hitpoints, your total damage is now %d.", damagePoints, owner.getBaseDamage());
        }
        else if (level == 3) {
            damagePoints = level * 10;
            owner.modifyDamage(damagePoints);
            System.out.printf("You have picked up a level 3 Assault Rifle. Your damage has increased by %d hitpoints, your total damage is now %d.", damagePoints, owner.getBaseDamage());
        }
        else if (level == 4) {
            damagePoints = level * 10;
            owner.modifyDamage(damagePoints);
            System.out.printf("You have picked up a level 4 Assault Rifle. Your damage has increased by %d hitpoints, your total damage is now %d.", damagePoints, owner.getBaseDamage());
        }
        else {
            damagePoints = level * 10;
            owner.modifyDamage(damagePoints);
            System.out.printf("You have picked up a level 5 Assault Rifle. Your damage has increased by %d hitpoints, your total damage is now %d.", damagePoints, owner.getBaseDamage());
        }

    }
    private int calculateARLevel() {
        int p = Game.rand.nextInt(6);
        return p;
    }
}

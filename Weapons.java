package adventure_game.items;

import adventure_game.Character;

/**
 * Interface Items allows for the creation of items such as weapons.
 * 
 * An Item implements the method pickUpItem() which allows for the player
 * to pick up an item and allow for their damage to change due to having an Item.
 * 
 */

 public interface Weapons {
    /**
     * Adds damage to character
     * 
     * For example, a knife will increase a characters damage by a certain amount.
     * 
     * @param owner is the character that will do the action of picking up.
     */
    public void pickUpItem(Character owner);
 }

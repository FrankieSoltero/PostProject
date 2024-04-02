package adventure_game.items;

import javax.swing.JTextArea;

import adventure_game.Character;

/**
 * Interface Consumable allows for the creation of consumable items
 * like Health Potions.
 * 
 * A Consumable implements the method consume() which manifests any
 * of the effects of the consumable item
 */
public interface Consumable {
    /**
     * Manifest any and all effects of this consumable item.
     * 
     * For example, a Health Potion will increase the health of
     * the Character consuming it.
     * 
     * @param owner the Character consuming this item
     */
    public void consume(Character owner, JTextArea output);

}

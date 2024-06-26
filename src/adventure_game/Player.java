package adventure_game;

import javax.swing.JTextArea;

public class Player extends Character{
    /**
     * This is the player constructor
     * @param name of type String
     * @param health of type int
     * @param mana of type int
     * @param baseDamage of type int
     */
    public Player(String name, int health, int level, int baseDamage){
        super(name, health, level, baseDamage);
    }
    /**
     * This method allows the player to take their turn by prompting 
     * with three choices Attack, Defend, Use Item. These are then 
     * what allow the player to interact with itself and or the NPC
     * @param other of type Character
     */
    @Override
    public void takeTurn(Character other, JTextArea output){
        output.append("Turn Taken");
    }
    @Override
    /**
     * This method allows for the player to level up appropriately.
     * Whenever levelingUpXp is called it levels the player after 3 
     * kills. Modfies damage and health based on what level it is.
     */
    public void levelingUp(JTextArea output){
        if (this.level == 1) {
            int damageTotal = (int) (this.baseDamage * (0.1));
            this.modifyDamage(damageTotal);
            int healthTotal = (int) (this.maxHealth * (0.25));
            this.maxHealth += healthTotal;
            this.modifyHealth(this.maxHealth);
            output.append("You have level up. you are now level " + this.level + "\nYour health is " + this.getHealth() + "\nYour damage is " + this.baseDamage + "\n");

        }
        else if (this.level == 2) {
            int damageTotal = (int) (this.baseDamage * (0.1));
            this.modifyDamage(damageTotal);
            int healthTotal = (int) (this.maxHealth * (0.25));
            this.maxHealth += healthTotal;
            this.modifyHealth(this.maxHealth);
            output.append("You have level up. you are now level " + this.level + "\nYour health is " + this.getHealth() + "\nYour damage is " + this.baseDamage + "\n");
            
        }
        else if (this.level == 3) {
            int damageTotal = (int) (this.baseDamage * (0.1));
            this.modifyDamage(damageTotal);
            int healthTotal = (int) (this.maxHealth * (0.25));
            this.maxHealth += healthTotal;
            this.modifyHealth(this.maxHealth);
            output.append("You have level up. you are now level " + this.level + "\nYour health is " + this.getHealth() + "\nYour damage is " + this.baseDamage + "\n");
        }
        else if (this.level == 4) {
            int damageTotal = (int) (this.baseDamage * (0.1));
            this.modifyDamage(damageTotal);
            int healthTotal = (int) (this.maxHealth * (0.25));
            this.maxHealth += healthTotal;
            this.modifyHealth(this.maxHealth);
            output.append("You have level up. you are now level " + this.level + "\nYour health is " + this.getHealth() + "\nYour damage is " + this.baseDamage + "\n");
        }
        else if (this.level == 5) {
            int damageTotal = (int) (this.baseDamage * (0.1));
            this.modifyDamage(damageTotal);
            int healthTotal = (int) (this.maxHealth * (0.25));
            this.maxHealth += healthTotal;
            this.modifyHealth(this.maxHealth);
            output.append("You have level up. you are now level " + this.level + "\nYour health is " + this.getHealth() + "\nYour damage is " + this.baseDamage + "\n");
        }
        else {
            int damageTotal = (int) (this.baseDamage * (0.1));
            this.modifyDamage(damageTotal);
            int healthTotal = (int) (this.maxHealth * (0.3));
            this.maxHealth += healthTotal;
            this.modifyHealth(this.maxHealth);
            output.append("You have level up. you are now level " + this.level + "\nYour health is " + this.getHealth() + "\nYour damage is " + this.baseDamage + "\n");
        }
    }
}
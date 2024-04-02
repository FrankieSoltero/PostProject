package adventure_game;

import javax.swing.JTextArea;

public class NPC extends Character{
    /**
     * This is the Constructor for NPC and requires parameters name,
     * health, mana, baseDamage.
     * @param name type String
     * @param health type Int
     * @param mana type Int
     * @param baseDamage type Int
     */
    public NPC(String name, int health, int level, int baseDamage){
        super(name, health, level, baseDamage);
    }
    /**
     * This allows for the character to take a turn against the NPC
     * @param other of type Character
     */
    @Override
    public void takeTurn(Character other, JTextArea output){
        if(this.isStunned()){
            this.decreaseTurnsStunned();
            System.out.printf("%S is unable to take any actions this turn!", this.getName());
            return;
        }
        this.attack(other, output);
       
    }
    @Override
    /**
     * This method allows for the player to level up appropriately.
     * Whenever levelingUpXp is called it levels the player after 3 
     * kills. Modfies damage and health based on what level it is.
     */
    public void levelingUp(JTextArea output){
        int i;
        for (i = 0; i < this.level; ++i){
            int damageModif = (int) (this.baseDamage * 0.1);
            int healthModif = (int) (this.getHealth() * 0.2);
            this.maxHealth += healthModif;
            this.modifyDamage(damageModif);
            this.modifyHealth(this.maxHealth);
        }
    }
}
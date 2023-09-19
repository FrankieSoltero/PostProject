package adventure_game;
import java.util.ArrayList;

import javax.swing.JTextArea;

import adventure_game.items.Consumable;

abstract public class Character{
    protected int maxHealth;
    private int health;

    private int levelUpXp;
    protected int level;

    protected int baseDamage;
 
    private String name;


    ArrayList<Consumable> items;

    // Character Conditions:
    private int turnsVulnerable; // number of turns Character is vulnerable
    private int turnsInvincible; // number of turns Character takes no damage
    private int turnsStunned; // number of turns Character gets no actions
    // buffer factor for next attack
    // E.g, if 2.0, the next attack will do double damage
    private double tempDamageBuff;

    public Character(String name, int health, int level, int damage){
        this.name = name;
        this.maxHealth = health;
        this.health = health;
        this.levelUpXp = 0;
        this.level = level;
        this.baseDamage = damage;
        this.tempDamageBuff = 1.0; 
        items = new ArrayList<Consumable>();
    }
    /**
     * This method outputs the Character in 
     * String format.
     * @return the output of name, hp, mana, and damage.
     */
    @Override
    public String toString(){
        String output;
        output = "";
        output += "Name " + getName() + "\n";
        output += "hp " + getHealth() + "\n";
        output += "Level " + getLevel() + "\n";
        output += "damage " + getBaseDamage() + "\n";
        return output;
    }

    /**
     * Get the name of this Character
     * @return the name of this Character
     */
    public String getName(){
        return this.name;
    }
    /**
     * Get the health of this Character
     * @return the health of this Character
     */
    public int getHealth(){
        return this.health;
    }
    /**
     * Get the Max Health for this Character
     * @return the Max Health for this Character
     */
    public int getMaxHealth(){
        return this.maxHealth;
    }
    /**
     * Get the Max Level for this Character
     * @return the Max Level for this Character
     */
    public int getlevelUpXp(){
        return this.levelUpXp;
    }
    /**
     * Get the Mana for this Character
     * @return the Mana for this Character
     */
    public int getLevel(){
        return this.level;
    }
    /**
     * Get the Base Damage for this Character
     * @return the Base Damage for this Character
     */
    public int getBaseDamage(){
        return this.baseDamage;
    }
    /**
     * Get the Damage Buff for this Character
     * @return the damage buff as a double
     */
    public double getDamageBuff() {
        return this.tempDamageBuff;
    }
    /**
     * Tests whether the character is Alive or not by asking
     * if their health is above 0
     * @return True or False based on if the Characters health is above 0
     */
    public boolean isAlive(){
        return this.health > 0;
    }
    /**
     * abstract method required to be defined in subclasses
     * @param other is an NPC in this case
     */
    abstract void takeTurn(Character other, JTextArea output);
    /**
     * Attacks the other Character
     * if the other is invincible it makes this character unable to attack for
     * a random number of turns.
     * The double modifier modifies the amount of Damage Done whether its increase or decrease.
     * The temporary damage buff is then applied but is reset back to 1 after the turn is taken.
     * Then if the other is vulnerable it increases the damage by 1.5.
     * The the damage is then deducted from the other Health.
     * Prints out this Name delt damage to other Name.
     * Finally modifies other health to lower it by the damage.
     * @param other of type Character
     */
    public void attack(Character other, JTextArea output){
        if(other.isInvincible()){
            output.append(this.name + " is unable to attack!\n");
            other.decreaseTurnsInvincible();
            return;
        }
        double modifier = GameWindow.rand.nextDouble();
        modifier = (modifier*0.4) + 0.8;
        int damage = (int)(this.baseDamage * modifier);
        // apply temporary damage buff, then reset it back to 1.0
        damage *= this.tempDamageBuff;
        this.tempDamageBuff = 1.0;

        if(other.isVulnerable()){
            damage *= 1.5;
            other.decreaseTurnsVulnerable();
        }
        other.modifyHealth(-damage);
        output.append(this.name + " dealt " + damage + " damage to " + other.getName() + "\n");
        
    }
    /**
     * Chance is set to the next random Double.
     * If Chance is equal to or below 0.75 this Character is set to invincible and
     * their damage buff is set to 2 for the next turn. 
     * Else the character is set to vulnerable for 1 turn.
     * @param other of type Character
     */
    public void defend(Character other, JTextArea output){
        double chance = GameWindow.rand.nextDouble();
        if(chance <=0.75){
            output.append(this.getName() + " blocks " + other.getName() + " attack and is now invincible for 1 turn!\n");
            this.setAsInvincible(1);
            this.setTempDamageBuff(2.0);
        } else {
            output.append(this.getName() + " stumbles. They are vulnerable for 1 turn!\n");
            this.setAsVulnerable(1);
        }
    }

    /**
     * The modifier parameter is added to this Characters Health.
     * Then if this Character health is less than 0 set health equal
     * to 0.
     * If this Characters Health is greated then its Max health, Set its health
     * back to it's max health.
     * @param modifier of type Int
     */
    public void modifyHealth(int modifier) {
        this.health += modifier;
        if(this.health < 0){
            this.health = 0;
        }
        if(this.health > this.getMaxHealth()){
            this.health = this.getMaxHealth();
        }
    }
    /**
     * This method modifies the damage of a character
     * 
     * This allows for weapon items to change the damage a character
     * can do to a monster.
     * 
     * @param modifier of type Int
     */
    public void modifyDamage(int modifier) {
        this.baseDamage += modifier;
    }

    /* CONDITIONS */
    /**
     * Sets turns vulnerable
     * @param numTurns of type int
     */
    public void setAsVulnerable(int numTurns){
        this.turnsVulnerable = numTurns;
    }
    /**
     * returns whether this Character is vulnerable or not
     * based on if the numTurns vulnerable is more than 0
     * @return true or false based on if turnsVulnerable is 
     * greater than 0.
     */
    public boolean isVulnerable(){
        return this.turnsVulnerable > 0;
    }
    /**
     * Decreases turns vulnerable
     */
    public void decreaseTurnsVulnerable(){
        this.turnsVulnerable--;
    }
    /**
     * Sets this character as invincible for num turns
     * @param numTurns of type of Int
     */
    public void setAsInvincible(int numTurns){
        this.turnsInvincible = numTurns;
    }
    /**
     * Asks if this Characters turns invincible is greater than 0.
     * @return true if this characters turns invincible is greater than 0.
     */
    public boolean isInvincible(){
        return this.turnsInvincible > 0;
    }
    /**
     * decreases this characters turns invincible
     */
    public void decreaseTurnsInvincible(){
        this.turnsInvincible--;
    }
    /**
     * Sets this chracter stunned for numTurns.
     * @param numTurns of type Int.
     */
    public void setAsStunned(int numTurns){
        this.turnsStunned = numTurns;
    }
    /**
     * Sets the character to stunned if this characters
     * turns stunned is greater than 0.
     * @return true or false if turns stunned is greater than 0
     */

    public boolean isStunned(){
        return this.turnsStunned > 0;
    }
    /**
     * Decreases turns stunned
     */
    public void decreaseTurnsStunned(){
        this.turnsStunned--;
    }

    /**
     * Set the temporary damage buffer. 
     * 
     * This is a multiplicative factor which will modify the damage 
     * for the next attack made by this Character. After the next 
     * attack, it will get reset back to 1.0
     * 
     * @param buff the multiplicative factor for the next attack's
     * damage.
     */
    public void setTempDamageBuff(double buff){
        this.tempDamageBuff = buff;
    }
    /**
     * stores a Consumable item in items
     * @param item of Consumable
     */

    public void obtain(Consumable item, JTextArea output){
        if (items.size() >= 10){
            output.append("You can only have 10 items at a time\n");
            
        }
        else {
            items.add(item);
            output.append("Bandages: " + items.size() + "\n");
        }
    }
    /**
     * Tests to see if the items inventory is empty if its not empty it returns
     * true
     * @return true or false based on if the items inventory is empty
     */
    public boolean hasItems(){
        return !items.isEmpty();
    }
    public void levelModifier(JTextArea output){
        this.levelUpXp += 1;
        if (this.levelUpXp == 3){
            this.levelUpXp = 0;
            this.level += 1;
            this.levelingUp(output);
        }
        

    }
    public abstract void levelingUp(JTextArea output);

    
}

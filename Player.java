package adventure_game;

import adventure_game.items.bandage;

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
    public void takeTurn(Character other){
        if(this.isStunned()){
            this.decreaseTurnsStunned();
            System.out.printf("%S is stunned and cannot take turn.", this.getName());
            return;
        }
        System.out.println();
        System.out.printf("%s has %d of %d health.\n", this.getName(), this.getHealth(), this.getMaxHealth());
        System.out.printf("%s has %d health.\n", other.getName(), other.getHealth());
        System.out.printf("Do you want to...\n");
        System.out.printf("  1: Attack?\n");
        System.out.printf("  2: Defend?\n");
        if(this.hasItems())
            System.out.printf("  3: Use an item?\n");
        System.out.printf("  4: Create a Bandage?\n");
        System.out.printf("Enter your choice: ");

        int choice = Game.in.nextInt();
        switch(choice){
            case 1:
                this.attack(other);
                break;
            case 2:
                this.defend(other);
                break;
            case 3:
                if(hasItems()){
                    this.useItem(this, other);
                } else {
                    System.out.println("You dig through your bag but find no items. You lose a turn!!");
                }
                break;
            case 4:
                System.out.printf("You are stunned 1 turn while creating a bandage");
                bandage band = new bandage();
                this.obtain(band);
                break;
        }
    }
    @Override
    /**
     * This method allows for the player to level up appropriately.
     * Whenever levelingUpXp is called it levels the player after 3 
     * kills. Modfies damage and health based on what level it is.
     */
    public void levelingUp(){
        if (this.level == 1) {
            int damageTotal = (int) (this.baseDamage * (0.1));
            this.modifyDamage(damageTotal);
            int healthTotal = (int) (this.maxHealth * (0.1));
            this.maxHealth += healthTotal;
            this.modifyHealth(this.maxHealth);
            System.out.printf("You have leveled up. you are now level %s\nYour health is %s\nYour damage is %s\n",this.level,this.getHealth(),this.baseDamage);
        }
        else if (this.level == 2) {
            int damageTotal = (int) (this.baseDamage * (0.1));
            this.modifyDamage(damageTotal);
            int healthTotal = (int) (this.maxHealth * (0.25));
            this.maxHealth += healthTotal;
            this.modifyHealth(this.maxHealth);
            System.out.printf("You have leveled up. you are now level %s\nYour health is %s\nYour damage is %s\n",this.level,this.getHealth(),this.baseDamage);
        }
        else if (this.level == 3) {
            int damageTotal = (int) (this.baseDamage * (0.1));
            this.modifyDamage(damageTotal);
            int healthTotal = (int) (this.maxHealth * (0.25));
            this.maxHealth += healthTotal;
            this.modifyHealth(this.maxHealth);
            System.out.printf("You have leveled up. you are now level %s\nYour health is %s\nYour damage is %s\n",this.level,this.getHealth(),this.baseDamage);
        }
        else if (this.level == 4) {
            int damageTotal = (int) (this.baseDamage * (0.1));
            this.modifyDamage(damageTotal);
            int healthTotal = (int) (this.maxHealth * (0.25));
            this.maxHealth += healthTotal;
            this.modifyHealth(this.maxHealth);
            System.out.printf("You have leveled up. you are now level %s\nYour health is %s\nYour damage is %s\n",this.level,this.getHealth(),this.baseDamage);
        }
        else if (this.level == 5) {
            int damageTotal = (int) (this.baseDamage * (0.1));
            this.modifyDamage(damageTotal);
            int healthTotal = (int) (this.maxHealth * (0.25));
            this.maxHealth += healthTotal;
            this.modifyHealth(this.maxHealth);
            System.out.printf("You have leveled up. you are now level %s\nYour health is %s\nYour damage is %s\n",this.level,this.getHealth(),this.baseDamage);
        }
        else {
            int damageTotal = (int) (this.baseDamage * (0.2));
            this.modifyDamage(damageTotal);
            int healthTotal = (int) (this.maxHealth * (0.4));
            this.maxHealth += healthTotal;
            this.modifyHealth(this.maxHealth);
            System.out.printf("You have leveled up. you are now level %s\nYour health is %s\nYour damage is %s\n",this.level,this.getHealth(),this.baseDamage);
        }
    }
}
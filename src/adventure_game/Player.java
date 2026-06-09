package adventure_game;

import adventure_game.items.bandage;

public class Player extends Character{

    private int weaponBonus = 0;
    private String weaponName = "Fists";

    /**
     * This is the player constructor
     * @param name of type String
     * @param health of type int
     * @param level of type int
     * @param baseDamage of type int
     */
    public Player(String name, int health, int level, int baseDamage){
        super(name, health, level, baseDamage);
    }

    @Override
    public void takeTurn(Character other, MessageLog output){
        output.append("Turn Taken");
    }

    /** Effective damage = innate base damage plus the equipped weapon's bonus. */
    @Override
    public int getEffectiveDamage() {
        return getBaseDamage() + weaponBonus;
    }

    /** Equip the weapon if it beats the current one; otherwise discard it. */
    public void equipWeapon(Weapon w, MessageLog output) {
        if (w.bonus() > weaponBonus) {
            weaponBonus = w.bonus();
            weaponName = w.name();
            output.append("You equip the " + w.name() + " (+" + w.bonus() + ")!");
        } else {
            output.append("You found a " + w.name() + " (+" + w.bonus()
                    + ") but your current weapon is better.");
        }
    }

    public String getWeaponName() { return weaponName; }

    public int getWeaponBonus() { return weaponBonus; }

    /** Restore the equipped weapon fields directly (used only by save/load). */
    void restoreWeapon(int bonus, String name) {
        this.weaponBonus = bonus;
        this.weaponName = name;
    }

    /** Rebuild a Player from persisted save fields. */
    public static Player fromSave(String name, int health, int maxHealth, int level,
            int levelUpXp, int baseDamage, int weaponBonus, String weaponName,
            int bandages, MessageLog sink) {
        Player p = new Player(name, maxHealth, level, baseDamage);
        p.restoreVitals(maxHealth, health, level, levelUpXp, baseDamage);
        p.restoreWeapon(weaponBonus, weaponName);
        for (int i = 0; i < bandages; i++) {
            p.obtain(new bandage(), sink);
        }
        return p;
    }

    /** Number of bandages (consumables) currently carried. */
    public int bandageCount() { return items.size(); }

    @Override
    public void levelingUp(MessageLog output){
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

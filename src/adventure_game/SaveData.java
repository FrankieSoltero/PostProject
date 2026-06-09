package adventure_game;

/**
 * Immutable snapshot of a run for save/load: the player fields, the current room
 * number, and every room's flags. Swing-free DTO; the boundary type between
 * GameState and SaveGame.
 */
public final class SaveData {
    public final String playerName;
    public final int health;
    public final int maxHealth;
    public final int level;
    public final int levelUpXp;
    public final int baseDamage;
    public final int weaponBonus;
    public final String weaponName;
    public final int bandages;
    public final int currentRoom;
    /** [roomNumber][0..3] = hasNPC, hasWeapon, hasItem, hasCure (raw flag ints). */
    public final int[][] roomFlags;

    public SaveData(String playerName, int health, int maxHealth, int level, int levelUpXp,
            int baseDamage, int weaponBonus, String weaponName, int bandages,
            int currentRoom, int[][] roomFlags) {
        this.playerName = playerName;
        this.health = health;
        this.maxHealth = maxHealth;
        this.level = level;
        this.levelUpXp = levelUpXp;
        this.baseDamage = baseDamage;
        this.weaponBonus = weaponBonus;
        this.weaponName = weaponName;
        this.bandages = bandages;
        this.currentRoom = currentRoom;
        this.roomFlags = roomFlags;
    }
}

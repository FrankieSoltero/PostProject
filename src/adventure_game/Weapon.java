package adventure_game;

/** An equippable weapon: a name and a flat damage bonus while equipped. */
public final class Weapon {
    private final String name;
    private final int bonus;

    public Weapon(String name, int bonus) {
        this.name = name;
        this.bonus = bonus;
    }

    public String name() { return name; }

    public int bonus() { return bonus; }

    /** Rolls a random weapon: a random tier, then a bonus within that tier's range. */
    public static Weapon randomWeapon() {
        int tier = GameRandom.rand.nextInt(3);
        switch (tier) {
            case 0:  return new Weapon("Knife", 5 + GameRandom.rand.nextInt(16));         // 5..20
            case 1:  return new Weapon("Pistol", 15 + GameRandom.rand.nextInt(21));       // 15..35
            default: return new Weapon("AssaultRifle", 30 + GameRandom.rand.nextInt(26)); // 30..55
        }
    }
}

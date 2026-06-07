package adventure_game;

import adventure_game.items.bandage;

/** Loot rules: collecting a room's loot and rolling kill drops. Swing-free. */
public final class Loot {

    private Loot() {}

    /** Grant a (safe) room's weapon/bandage to the player and clear its flags. */
    public static void collectRoom(Room room, Player player, MessageLog log) {
        if (room.hasWeapon() == 1) {
            player.equipWeapon(Weapon.randomWeapon(), log);
            room.removeWeapon();
        }
        if (room.hasItem() == 1) {
            player.obtain(new bandage(), log);
            room.removeItem();
        }
    }

    /** On a kill, a chance to drop a bandage and/or a weapon. */
    public static void rollKillDrop(Player player, MessageLog log) {
        if (GameRandom.rand.nextInt(3) == 0) {
            log.append("The corpse yields a bandage.");
            player.obtain(new bandage(), log);
        }
        if (GameRandom.rand.nextInt(4) == 0) {
            log.append("The zombie dropped a weapon.");
            player.equipWeapon(Weapon.randomWeapon(), log);
        }
    }
}

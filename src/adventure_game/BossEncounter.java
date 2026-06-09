package adventure_game;

/**
 * Pure resolution of a defeated boss: award the bounty, clear the room, and decide
 * the next mode. Kept separate from GameState so the win/clear decision is unit-
 * testable without navigating to a boss room.
 */
public final class BossEncounter {

    private BossEncounter() {}

    /** Apply the boss-kill reward and return the next game mode. */
    public static GameState.Mode onDefeat(Room room, Player player, MessageLog log) {
        log.append("You have slain a boss. You grow stronger.");
        player.levelModifier(log);
        player.levelModifier(log);
        player.levelModifier(log);
        room.removeNPC();
        if (room.hasCure()) {
            log.append("You secured the cure. Humanity has a chance.");
            return GameState.Mode.WON;
        }
        log.append("The monstrosity falls. The way ahead is clear.");
        return GameState.Mode.EXPLORE;
    }
}

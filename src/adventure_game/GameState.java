package adventure_game;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Phase-1 slice game state: loads the hospital map, holds the player, the
 * current room, one enemy, and the message log, and resolves a single
 * attack exchange. Swing-free.
 */
public class GameState {
    private Player player;
    private List<Room> rooms;
    private Room currentRoom;
    private NPC enemy;
    private boolean inCombat;
    private final MessageLog log = new MessageLog();

    public static GameState loadHospital() throws FileNotFoundException {
        GameState gs = new GameState();
        gs.rooms = HospitalMap.load(HospitalMap.HOSPITAL_PATH);
        gs.currentRoom = gs.rooms.get(0);
        gs.player = new Player("Mick", 150, 0, 75);
        gs.enemy = new NPC("Walker", 100, 0, 15);
        gs.inCombat = true;
        gs.log.append("A Walker shuffles toward you.");
        return gs;
    }

    /** Resolve one attack: player hits enemy; if it survives, it hits back. */
    public void playerAttack() {
        if (!inCombat || enemy == null) {
            return;
        }
        player.attack(enemy, log);
        if (enemy.isAlive()) {
            enemy.takeTurn(player, log);
        } else {
            log.append(enemy.getName() + " has died.");
            inCombat = false;
        }
    }

    public Player getPlayer() { return player; }
    public Room getCurrentRoom() { return currentRoom; }
    public NPC getEnemy() { return enemy; }
    public boolean isInCombat() { return inCombat; }
    public MessageLog getLog() { return log; }

}

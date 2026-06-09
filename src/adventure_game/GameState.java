package adventure_game;

import java.io.FileNotFoundException;
import java.util.List;

import adventure_game.items.bandage;

/**
 * Game state for the explore-and-fight loop: the hospital rooms, the player,
 * the current room, the current enemy (if any), a mode, and the message log.
 * Swing-free. Gameplay logic is ported from reference/GameWindow.java.txt.
 */
public class GameState {

    public enum Mode { EXPLORE, COMBAT, DEAD, WON }

    private static final int[] REGULAR_ROOMS =
            {1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21};
    private static final int[] BOSS_ROOMS = {10, 19, 22, 24};
    private static final int[] WEAPON_ROOMS = {1, 3, 6, 7, 8, 9, 11, 15, 16, 17, 20, 21};
    private static final int[] ITEM_ROOMS = {1, 3, 4, 7, 11, 17, 20, 21};

    private Player player;
    private List<Room> rooms;
    private Room currentRoom;
    private NPC enemy;
    private Mode mode;
    private final MessageLog log = new MessageLog();

    public static GameState loadHospital() throws FileNotFoundException {
        GameState gs = new GameState();
        gs.rooms = HospitalMap.load(HospitalMap.HOSPITAL_PATH);
        gs.currentRoom = gs.rooms.get(0);
        gs.player = new Player("Mick", 150, 0, 75);
        gs.mode = Mode.EXPLORE;
        gs.populateZombies();
        gs.rooms.get(24).setRoomCure();
        gs.placeRoomLoot();
        gs.log.append("You stand at the Hospital Entrance. The cold bites.");
        return gs;
    }

    private void populateZombies() {
        for (int i : REGULAR_ROOMS) {
            rooms.get(i).setNPC();
        }
        for (int i : BOSS_ROOMS) {
            rooms.get(i).setBossNPC();
        }
    }

    private void placeRoomLoot() {
        for (int i : WEAPON_ROOMS) {
            rooms.get(i).setWeapon();
        }
        for (int i : ITEM_ROOMS) {
            rooms.get(i).setItem();
        }
    }

    // --- navigation ---
    public boolean move(Direction dir) {
        if (mode != Mode.EXPLORE) {
            return false;
        }
        Room next = nextRoom(dir);
        if (next == null) {
            log.append("You cannot go that way.");
            return false;
        }
        currentRoom = next;
        log.append("You enter the " + currentRoom.getRoomName() + ".");
        tryEncounter();
        return true;
    }

    private Room nextRoom(Direction dir) {
        switch (dir) {
            case NORTH: return currentRoom.isRoomNorthNull() ? null : currentRoom.getNorthRoom();
            case SOUTH: return currentRoom.isRoomSouthNull() ? null : currentRoom.getSouthRoom();
            case EAST:  return currentRoom.isRoomEastNull()  ? null : currentRoom.getEastRoom();
            case WEST:  return currentRoom.isRoomWestNull()  ? null : currentRoom.getWestRoom();
            default:    return null;
        }
    }

    public void tryEncounter() {
        int npc = currentRoom.hasNPC();
        if (npc == 1) {
            spawnEnemy();
            mode = Mode.COMBAT;
            log.append("A " + enemy.getName() + " lurches out of the gloom!");
        } else if (npc == 4) {
            enemy = BossFactory.forRoom(currentRoom.getRoomNumber());
            mode = Mode.COMBAT;
            log.append("Something massive blocks your path: " + enemy.getName() + "!");
        } else {
            Loot.collectRoom(currentRoom, player, log);
        }
    }

    private void spawnEnemy() {
        int t = GameRandom.rand.nextInt(3);
        int level;
        if (t == 0) {
            level = GameRandom.rand.nextInt(11);
            enemy = new NPC("Walker", 100, level, 15);
        } else if (t == 1) {
            level = GameRandom.rand.nextInt(11);
            enemy = new NPC("Creeper", 250, level, 18);
        } else {
            level = GameRandom.rand.nextInt(6);
            enemy = new NPC("Sprinter", 500, level, 25);
        }
        enemy.levelingUp(log);
    }

    // --- combat verbs (each resolves one exchange) ---
    public void playerAttack() {
        if (mode != Mode.COMBAT || enemy == null) {
            return;
        }
        player.attack(enemy, log);
        if (!enemy.isAlive()) {
            onEnemyDefeated();
            return;
        }
        enemy.takeTurn(player, log);
        checkPlayerDeath();
    }

    public void playerDefend() {
        if (mode != Mode.COMBAT || enemy == null) {
            return;
        }
        enemy.takeTurn(player, log);
        player.defend(enemy, log);
        checkPlayerDeath();
    }

    public void useBandage() {
        if (mode != Mode.COMBAT || enemy == null) {
            return;
        }
        if (player.hasItems()) {
            player.items.get(0).consume(player, log);
            player.items.remove(0);
            enemy.takeTurn(player, log);
            checkPlayerDeath();
        } else {
            log.append("You have no bandages to use.");
        }
    }

    public void createBandage() {
        if (mode != Mode.COMBAT || enemy == null) {
            return;
        }
        player.obtain(new bandage(), log);
        enemy.takeTurn(player, log);
        checkPlayerDeath();
    }

    private void checkPlayerDeath() {
        if (!player.isAlive()) {
            log.append("You collapse. The hospital claims another survivor.");
            mode = Mode.DEAD;
            enemy = null;
        }
    }

    private void onEnemyDefeated() {
        log.append(enemy.getName() + " has died.");
        if (enemy instanceof Boss) {
            Mode next = BossEncounter.onDefeat(currentRoom, player, log);
            enemy = null;
            mode = next;
            return;
        }
        player.levelModifier(log);
        Loot.rollKillDrop(player, log);
        int roll = GameRandom.rand.nextInt(2) + 1;
        if (roll == 2) {
            spawnEnemy();
            log.append("Another zombie lurches out. Fuck.");
        } else {
            currentRoom.removeNPC();
            enemy = null;
            mode = Mode.EXPLORE;
            log.append("The room is clear... for now.");
            Loot.collectRoom(currentRoom, player, log);
        }
    }

    // --- accessors ---
    public Player getPlayer() { return player; }
    public Room getCurrentRoom() { return currentRoom; }
    /** Read-only access to a room by its number (== list index). */
    public Room getRoom(int roomNumber) { return rooms.get(roomNumber); }
    public NPC getEnemy() { return enemy; }
    public Mode getMode() { return mode; }
    public boolean isInCombat() { return mode == Mode.COMBAT; }
    public MessageLog getLog() { return log; }
}

package adventure_game;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
        gs.rooms = new ArrayList<>();
        readMap("data/levels/Hospital Map/The-Hospital.txt", gs.rooms);
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

    private static void readMap(String file, List<Room> rooms) throws FileNotFoundException {
        File f = new File(file);
        Scanner scnr = new Scanner(f);
        int count = 0;
        int numberLinesToRead = 0;
        while (scnr.hasNextLine()) {
            String line = scnr.nextLine();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("#")) {
                ++count;
                continue;
            }
            if (count == 2) {
                numberLinesToRead = Integer.parseInt(line);
                ++count;
                continue;
            }
            if (count > 2 && count <= 2 + numberLinesToRead) {
                line = line.trim();
                // Limit to 3 so a bio containing ':' (e.g. room 14's
                // "Your exits: ...") is preserved intact in token 2.
                String[] fileInfo = line.split(":", 3);
                int roomNumber = Integer.parseInt(fileInfo[0]);
                String roomName = fileInfo[1];
                String roomBio = fileInfo[2];
                rooms.add(new Room(roomNumber, roomName, roomBio));
            } else if (count > numberLinesToRead + 3) {
                line = line.trim();
                String[] exits = line.split(":");
                int roomTag = Integer.parseInt(exits[0].trim());
                int roomEast = Integer.parseInt(exits[1].trim());
                int roomNorth = Integer.parseInt(exits[2].trim());
                int roomWest = Integer.parseInt(exits[3].trim());
                int roomSouth = Integer.parseInt(exits[4].trim());
                Room cur = rooms.get(roomTag);
                if (roomEast == -1) { cur.setRoomEastNull(); } else { cur.setEastRoom(rooms.get(roomEast)); }
                if (roomNorth == -1) { cur.setRoomNorthNull(); } else { cur.setNorthRoom(rooms.get(roomNorth)); }
                if (roomSouth == -1) { cur.setRoomSouthNull(); } else { cur.setSouthRoom(rooms.get(roomSouth)); }
                if (roomWest == -1) { cur.setRoomWestNull(); } else { cur.setWestRoom(rooms.get(roomWest)); }
            }
            count++;
        }
        scnr.close();
    }
}

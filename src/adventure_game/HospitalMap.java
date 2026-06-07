package adventure_game;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Loads the hospital room graph from the map file. Pure I/O, no game state. */
public final class HospitalMap {

    public static final String HOSPITAL_PATH = "data/levels/Hospital Map/The-Hospital.txt";

    private HospitalMap() {}

    public static List<Room> load(String file) throws FileNotFoundException {
        List<Room> rooms = new ArrayList<>();
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
                String[] fileInfo = line.split(":", 3);
                rooms.add(new Room(Integer.parseInt(fileInfo[0]), fileInfo[1], fileInfo[2]));
            } else if (count > numberLinesToRead + 3) {
                line = line.trim();
                String[] exits = line.split(":");
                int roomTag = Integer.parseInt(exits[0].trim());
                int e = Integer.parseInt(exits[1].trim());
                int n = Integer.parseInt(exits[2].trim());
                int w = Integer.parseInt(exits[3].trim());
                int s = Integer.parseInt(exits[4].trim());
                Room cur = rooms.get(roomTag);
                if (e == -1) { cur.setRoomEastNull(); } else { cur.setEastRoom(rooms.get(e)); }
                if (n == -1) { cur.setRoomNorthNull(); } else { cur.setNorthRoom(rooms.get(n)); }
                if (s == -1) { cur.setRoomSouthNull(); } else { cur.setSouthRoom(rooms.get(s)); }
                if (w == -1) { cur.setRoomWestNull(); } else { cur.setWestRoom(rooms.get(w)); }
            }
            count++;
        }
        scnr.close();
        return rooms;
    }
}

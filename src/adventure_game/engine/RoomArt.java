package adventure_game.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps a room number to its backdrop {@link ArtAsset}. Backdrops load from
 * {@code assets/art/rooms/room<N>.txt}; any room without a file falls back to a
 * shared blank/placeholder asset.
 */
public class RoomArt {
    private final Map<Integer, ArtAsset> byRoom = new HashMap<>();
    private final ArtAsset fallback;

    public RoomArt(ArtAsset fallback) {
        this.fallback = fallback;
    }

    /** Loads room0..room(count-1).txt from dir; missing/unreadable files are skipped. */
    public static RoomArt loadAll(int count, String dir, ArtAsset fallback) {
        RoomArt ra = new RoomArt(fallback);
        for (int i = 0; i < count; i++) {
            Path p = Path.of(dir, "room" + i + ".txt");
            if (Files.exists(p)) {
                try {
                    ra.byRoom.put(i, new ArtAsset(Files.readAllLines(p)));
                } catch (IOException e) {
                    // leave this room to the fallback
                }
            }
        }
        return ra;
    }

    public ArtAsset forRoom(int roomNumber) {
        return byRoom.getOrDefault(roomNumber, fallback);
    }

    public int loadedCount() {
        return byRoom.size();
    }
}

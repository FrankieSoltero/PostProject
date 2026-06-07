package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.engine.ArtAsset;
import adventure_game.engine.RoomArt;

public class RoomArtTest {

    @Test
    void unknownRoomReturnsFallback() {
        ArtAsset fallback = ArtAsset.fromLines("FB");
        RoomArt ra = new RoomArt(fallback);
        assertSame(fallback, ra.forRoom(7));
    }

    @Test
    void loadAllOnMissingDirLoadsNothingAndUsesFallback() {
        ArtAsset fallback = ArtAsset.fromLines("FB");
        RoomArt ra = RoomArt.loadAll(25, "assets/art/__nonexistent_dir__", fallback);
        assertEquals(0, ra.loadedCount());
        assertSame(fallback, ra.forRoom(0));
    }
}

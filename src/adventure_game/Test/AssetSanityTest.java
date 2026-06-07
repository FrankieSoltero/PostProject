package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import adventure_game.engine.ArtAsset;
import adventure_game.engine.RoomArt;

public class AssetSanityTest {

    private static void assertNoBom(Path p) throws Exception {
        byte[] b = Files.readAllBytes(p);
        boolean bom = b.length >= 3
                && (b[0] & 0xFF) == 0xEF && (b[1] & 0xFF) == 0xBB && (b[2] & 0xFF) == 0xBF;
        assertFalse(bom, "BOM found in " + p);
    }

    @Test
    void allArtFilesAreBomFree() throws Exception {
        try (Stream<Path> s = Files.walk(Path.of("assets/art"))) {
            for (Path p : (Iterable<Path>) s.filter(x -> x.toString().endsWith(".txt"))::iterator) {
                assertNoBom(p);
            }
        }
    }

    @Test
    void allTwentyFiveRoomBackdropsLoad() {
        RoomArt ra = RoomArt.loadAll(25, "assets/art/rooms", ArtAsset.fromLines(" "));
        assertEquals(25, ra.loadedCount(), "expected 25 backdrops in assets/art/rooms");
    }

    @Test
    void roomBackdropsFitViewport() throws Exception {
        for (int i = 0; i < 25; i++) {
            ArtAsset a = ArtAsset.fromFile(Path.of("assets/art/rooms", "room" + i + ".txt").toString());
            assertTrue(a.height() <= 13, "room" + i + " too tall: " + a.height());
            assertTrue(a.width() <= 60, "room" + i + " too wide: " + a.width());
        }
    }
}

package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.engine.ArtAsset;

public class ArtAssetTest {

    @Test
    void fromLinesReportsSize() {
        ArtAsset a = ArtAsset.fromLines("abc", "de");
        assertEquals(2, a.height());
        assertEquals(3, a.width()); // widest line
        assertEquals("abc", a.lines().get(0));
    }

    @Test
    void fromFileLoadsAsset() throws Exception {
        ArtAsset a = ArtAsset.fromFile("assets/art/player.txt");
        assertEquals(5, a.height());
        assertEquals(5, a.width());
        // No BOM: first line begins with a real space, not U+FEFF.
        assertEquals(' ', a.lines().get(0).charAt(0));
    }
}

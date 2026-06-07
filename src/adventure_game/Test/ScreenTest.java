package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.engine.ArtAsset;
import adventure_game.engine.Screen;

public class ScreenTest {

    @Test
    void clearFillsWithSpaces() {
        Screen s = new Screen(4, 2);
        s.clear();
        assertEquals("    ", s.toLines()[0]);
        assertEquals("    ", s.toLines()[1]);
    }

    @Test
    void drawTextWritesLeftToRightAndClips() {
        Screen s = new Screen(4, 1);
        s.clear();
        s.drawText(1, 0, "ABCDEF"); // clips at width 4
        assertEquals(" ABC", s.toLines()[0]);
    }

    @Test
    void putIgnoresOutOfBounds() {
        Screen s = new Screen(2, 2);
        s.clear();
        s.put(5, 5, 'X'); // no exception, no effect
        assertEquals("  ", s.toLines()[0]);
    }

    @Test
    void blitTreatsSpacesAsTransparent() {
        Screen s = new Screen(3, 2);
        s.clear();
        s.drawText(0, 0, "###");
        ArtAsset art = ArtAsset.fromLines("A A"); // middle space is transparent
        s.blit(art, 0, 0);
        assertEquals("A#A", s.toLines()[0]);
    }
}

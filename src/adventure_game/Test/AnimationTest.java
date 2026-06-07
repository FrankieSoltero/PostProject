package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.engine.Animation;
import adventure_game.engine.AnimationFrame;
import adventure_game.engine.Animations;
import adventure_game.engine.ArtAsset;

public class AnimationTest {

    @Test
    void frameExposesArtOffsetAndDuration() {
        AnimationFrame f = new AnimationFrame(ArtAsset.fromLines(">"), 12, 0, 60);
        assertEquals(12, f.getDx());
        assertEquals(0, f.getDy());
        assertEquals(60, f.getDurationMs());
        assertEquals(">", f.getArt().lines().get(0));
    }

    @Test
    void playerStrikeAdvancesAndEndsOnImpact() {
        Animation a = Animations.playerStrike();
        assertTrue(a.size() >= 2);
        AnimationFrame first = a.frames().get(0);
        AnimationFrame last = a.frames().get(a.size() - 1);
        assertTrue(last.getDx() > first.getDx());      // travels toward the enemy
        assertEquals("*", last.getArt().lines().get(0)); // impact marker
    }

    @Test
    void playerGuardIsAShortBurst() {
        adventure_game.engine.Animation a = adventure_game.engine.Animations.playerGuard();
        assertTrue(a.size() >= 2 && a.size() <= 6);
        for (adventure_game.engine.AnimationFrame f : a.frames()) {
            assertTrue(f.getDurationMs() >= 50 && f.getDurationMs() <= 120);
        }
    }
}

package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.engine.HudComponents;

public class HudComponentsTest {

    @Test
    void fullBarIsAllFilled() {
        assertEquals("[||||||||||]", HudComponents.healthBar(100, 100, 10));
    }

    @Test
    void emptyBarIsAllDots() {
        assertEquals("[..........]", HudComponents.healthBar(0, 100, 10));
    }

    @Test
    void halfBarIsHalfFilled() {
        assertEquals("[|||||.....]", HudComponents.healthBar(50, 100, 10));
    }

    @Test
    void clampsOverAndUnderflow() {
        assertEquals("[||||||||||]", HudComponents.healthBar(999, 100, 10));
        assertEquals("[..........]", HudComponents.healthBar(-5, 100, 10));
    }
}

package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.GameState;
import adventure_game.engine.ArtAsset;
import adventure_game.engine.Scene;
import adventure_game.engine.Screen;

public class SceneTest {

    private static boolean gridContains(Screen s, String needle) {
        for (String line : s.toLines()) {
            if (line.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    @Test
    void renderDrawsRoomNameHpBarAndLog() throws Exception {
        GameState gs = GameState.loadHospital();
        ArtAsset player = ArtAsset.fromFile("assets/art/player.txt");
        ArtAsset walker = ArtAsset.fromFile("assets/art/walker.txt");
        ArtAsset backdrop = ArtAsset.fromFile("assets/art/room0.txt");
        Scene scene = new Scene(60, 24, player, walker, backdrop);
        Screen screen = new Screen(60, 24);

        scene.render(screen, gs, null);

        assertTrue(gridContains(screen, "Hospital Entrance")); // HUD name
        assertTrue(gridContains(screen, "HP ["));              // HUD bar
        assertTrue(gridContains(screen, "Lv."));               // HUD level
        assertTrue(gridContains(screen, "Walker shuffles"));   // log line
    }
}

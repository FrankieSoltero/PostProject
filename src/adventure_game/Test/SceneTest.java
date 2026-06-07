package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import adventure_game.GameState;
import adventure_game.engine.ArtAsset;
import adventure_game.engine.RoomArt;
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

    private static Scene buildScene() {
        ArtAsset player = ArtAsset.fromLines("  @  ", " /|\\ ", "  |  ", " / \\ ", "     ");
        ArtAsset walker = ArtAsset.fromLines(" ___ ", "[o o]", " \\_/ ", " /|\\ ", " / \\ ");
        Map<String, ArtAsset> enemyArt = new HashMap<>();
        enemyArt.put("Walker", walker);
        RoomArt roomArt = new RoomArt(ArtAsset.fromLines("ROOM-BACKDROP"));
        return new Scene(60, 24, player, enemyArt, walker, roomArt);
    }

    @Test
    void renderShowsHudRoomNameHpAndLog() throws Exception {
        GameState gs = GameState.loadHospital();
        Scene scene = buildScene();
        Screen screen = new Screen(60, 24);

        scene.render(screen, gs, null);

        assertTrue(gridContains(screen, "Hospital Entrance"));
        assertTrue(gridContains(screen, "HP ["));
        assertTrue(gridContains(screen, "Lv."));
        assertTrue(gridContains(screen, "Hospital Entrance."));
    }
}

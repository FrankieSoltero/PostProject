package adventure_game.engine;

import java.util.List;
import java.util.Map;

import adventure_game.GameState;
import adventure_game.Player;
import adventure_game.Room;

/**
 * Composes the whole Screen from GameState each redraw: HUD (top), viewport
 * (middle, where the current room's backdrop, player, enemy, and any active
 * animation frame draw), and log (bottom).
 */
public class Scene {
    private static final int VIEW_TOP = 3;
    private static final int LOG_TOP = 16;
    private static final int PLAYER_X = 6;
    private static final int PLAYER_Y = VIEW_TOP + 6;
    private static final int ENEMY_X = 40;
    private static final int STRIKE_Y = VIEW_TOP + 7;

    private final int width;
    private final int height;
    private final ArtAsset playerArt;
    private final Map<String, ArtAsset> enemyArt;
    private final ArtAsset enemyFallback;
    private final RoomArt roomArt;

    public Scene(int width, int height, ArtAsset playerArt,
                 Map<String, ArtAsset> enemyArt, ArtAsset enemyFallback, RoomArt roomArt) {
        this.width = width;
        this.height = height;
        this.playerArt = playerArt;
        this.enemyArt = enemyArt;
        this.enemyFallback = enemyFallback;
        this.roomArt = roomArt;
    }

    public void render(Screen s, GameState state, AnimationFrame active) {
        s.clear();
        Player p = state.getPlayer();
        Room room = state.getCurrentRoom();

        // --- HUD ---
        s.drawText(1, 0, room.getRoomName());
        String lv = "Lv." + p.getLevel();
        s.drawText(width - 1 - lv.length(), 0, lv);
        String hp = "HP " + HudComponents.healthBar(p.getHealth(), p.getMaxHealth(), 12)
                + " " + p.getHealth() + "/" + p.getMaxHealth();
        s.drawText(1, 1, hp);
        String dmg = "DMG " + p.getBaseDamage();
        s.drawText(width - 1 - dmg.length(), 1, dmg);
        for (int x = 0; x < width; x++) {
            s.put(x, 2, '-');
        }

        // --- VIEWPORT ---
        s.blit(roomArt.forRoom(room.getRoomNumber()), 0, VIEW_TOP);
        s.blit(playerArt, PLAYER_X, PLAYER_Y);
        if (state.isInCombat() && state.getEnemy() != null) {
            ArtAsset ea = enemyArt.getOrDefault(state.getEnemy().getName(), enemyFallback);
            s.blit(ea, ENEMY_X, PLAYER_Y);
        }
        if (active != null) {
            s.blit(active.getArt(), PLAYER_X + active.getDx(), STRIKE_Y + active.getDy());
        }

        // --- LOG ---
        for (int x = 0; x < width; x++) {
            s.put(x, LOG_TOP, '-');
        }
        int logRows = height - LOG_TOP - 1;
        List<String> recent = state.getLog().recent(logRows);
        for (int i = 0; i < recent.size(); i++) {
            s.drawText(1, LOG_TOP + 1 + i, "> " + recent.get(i));
        }
    }
}

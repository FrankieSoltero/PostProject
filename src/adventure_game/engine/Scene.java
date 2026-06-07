package adventure_game.engine;

import java.util.List;
import java.util.Map;

import adventure_game.GameState;
import adventure_game.NPC;
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

    /** A clean arena drawn behind fights so room art does not clutter combat. */
    private static final ArtAsset COMBAT_ARENA = buildCombatArena();

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
        String topRight = "Lv." + p.getLevel() + "  Bandages:" + p.bandageCount();
        s.drawText(width - 1 - topRight.length(), 0, topRight);
        String hp = "HP " + HudComponents.healthBar(p.getHealth(), p.getMaxHealth(), 12)
                + " " + p.getHealth() + "/" + p.getMaxHealth();
        s.drawText(1, 1, hp);
        String botRight = "DMG " + p.getEffectiveDamage() + "  " + p.getWeaponName();
        s.drawText(width - 1 - botRight.length(), 1, botRight);
        for (int x = 0; x < width; x++) {
            s.put(x, 2, '-');
        }

        // --- VIEWPORT ---
        boolean fighting = state.isInCombat() && state.getEnemy() != null;
        ArtAsset backdrop = fighting ? COMBAT_ARENA : roomArt.forRoom(room.getRoomNumber());
        s.blit(backdrop, 0, VIEW_TOP);
        s.blit(playerArt, PLAYER_X, PLAYER_Y);
        if (fighting) {
            NPC enemy = state.getEnemy();
            ArtAsset ea = enemyArt.getOrDefault(enemy.getName(), enemyFallback);
            s.blit(ea, ENEMY_X, PLAYER_Y);
            // Floating enemy health banner above the enemy.
            int bx = ENEMY_X - 6;
            s.drawText(bx, VIEW_TOP + 1, enemy.getName() + " Lv." + enemy.getLevel());
            String ehp = HudComponents.healthBar(enemy.getHealth(), enemy.getMaxHealth(), 12)
                    + " " + enemy.getHealth() + "/" + enemy.getMaxHealth();
            s.drawText(bx, VIEW_TOP + 2, ehp);
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

    private static ArtAsset buildCombatArena() {
        java.util.List<String> lines = new java.util.ArrayList<>();
        for (int i = 0; i < 13; i++) {
            lines.add(i == 10 ? repeat('_', 56) : "");
        }
        return new ArtAsset(lines);
    }

    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}

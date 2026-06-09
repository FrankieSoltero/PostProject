package adventure_game;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import adventure_game.engine.Animation;
import adventure_game.engine.AnimationFrame;
import adventure_game.engine.AnimationPlayer;
import adventure_game.engine.Animations;
import adventure_game.engine.ArtAsset;
import adventure_game.engine.RenderPanel;
import adventure_game.engine.RoomArt;
import adventure_game.engine.Scene;
import adventure_game.engine.Screen;

/** Entry point: unified single-screen window with mode-based controls. */
public class GameApp extends JFrame {
    private static final int COLS = 60;
    private static final int ROWS = 24;

    private final RenderPanel panel;
    private final Scene scene;
    private final Screen screen;
    private final GameState state;
    private final CardLayout cards = new CardLayout();
    private final JPanel south = new JPanel(cards);

    private AnimationPlayer current;
    private AnimationFrame activeFrame;

    public GameApp() throws Exception {
        super("Covid, The Zombie Apocalypse");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        screen = new Screen(COLS, ROWS);
        ArtAsset playerArt = ArtAsset.fromFile("assets/art/player.txt");
        Map<String, ArtAsset> enemyArt = new HashMap<>();
        enemyArt.put("Walker", ArtAsset.fromFile("assets/art/walker.txt"));
        enemyArt.put("Creeper", ArtAsset.fromFile("assets/art/creeper.txt"));
        enemyArt.put("Sprinter", ArtAsset.fromFile("assets/art/sprinter.txt"));
        ArtAsset enemyFallback = enemyArt.get("Walker");
        ArtAsset blank = ArtAsset.fromLines("     ", "     ", "     ", "     ", "     ");
        RoomArt roomArt = RoomArt.loadAll(25, "assets/art/rooms", blank);

        ArtAsset cureArt = ArtAsset.fromLines("(cure)");
        scene = new Scene(COLS, ROWS, playerArt, enemyArt, enemyFallback, roomArt, cureArt);
        state = GameState.loadHospital();
        panel = new RenderPanel(COLS, ROWS);

        south.add(buildExplorePanel(), GameState.Mode.EXPLORE.name());
        south.add(buildCombatPanel(), GameState.Mode.COMBAT.name());
        south.add(buildDeadPanel(), GameState.Mode.DEAD.name());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(south, BorderLayout.SOUTH);

        redraw();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel buildExplorePanel() {
        JPanel p = new JPanel(new GridLayout(2, 3));
        p.add(moveButton("North", Direction.NORTH));
        p.add(moveButton("South", Direction.SOUTH));
        p.add(moveButton("East", Direction.EAST));
        p.add(moveButton("West", Direction.WEST));
        p.add(actionButton("Create Bandage", state::createBandage, false));
        p.add(new JLabel());
        return p;
    }

    private JPanel buildCombatPanel() {
        JPanel p = new JPanel(new GridLayout(1, 4));
        JButton attack = new JButton("Attack");
        attack.addActionListener(e -> {
            if (!isAnimating() && state.getMode() == GameState.Mode.COMBAT) {
                playThen(Animations.playerStrike(), state::playerAttack);
            }
        });
        JButton defend = new JButton("Defend");
        defend.addActionListener(e -> {
            if (!isAnimating() && state.getMode() == GameState.Mode.COMBAT) {
                playThen(Animations.playerGuard(), state::playerDefend);
            }
        });
        p.add(attack);
        p.add(defend);
        p.add(actionButton("Use Bandage", state::useBandage, true));
        p.add(actionButton("Create Bandage", state::createBandage, true));
        return p;
    }

    private JPanel buildDeadPanel() {
        JPanel p = new JPanel(new GridLayout(1, 1));
        p.add(new JLabel("  You are dead. Close the window to quit.  "));
        return p;
    }

    private JButton actionButton(String label, Runnable action, boolean combatOnly) {
        JButton b = new JButton(label);
        b.addActionListener(e -> {
            if (isAnimating()) {
                return;
            }
            if (combatOnly && state.getMode() != GameState.Mode.COMBAT) {
                return;
            }
            action.run();
            redraw();
        });
        return b;
    }

    private JButton moveButton(String label, Direction dir) {
        JButton b = new JButton(label);
        b.addActionListener(e -> {
            if (isAnimating()) {
                return;
            }
            state.move(dir);
            redraw();
        });
        return b;
    }

    private boolean isAnimating() {
        return current != null && current.isPlaying();
    }

    private void playThen(Animation anim, Runnable resolve) {
        current = new AnimationPlayer(
                frame -> { activeFrame = frame; redraw(); },
                () -> { resolve.run(); activeFrame = null; redraw(); });
        current.play(anim);
    }

    private void redraw() {
        scene.render(screen, state, activeFrame);
        panel.setScreen(screen);
        panel.repaint();
        cards.show(south, state.getMode().name());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new GameApp();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}

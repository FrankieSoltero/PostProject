package adventure_game;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import adventure_game.engine.Animations;
import adventure_game.engine.AnimationFrame;
import adventure_game.engine.AnimationPlayer;
import adventure_game.engine.ArtAsset;
import adventure_game.engine.RenderPanel;
import adventure_game.engine.Scene;
import adventure_game.engine.Screen;

/** Entry point: unified single-screen window with the redraw cycle. */
public class GameApp extends JFrame {
    private static final int COLS = 60;
    private static final int ROWS = 24;

    private final RenderPanel panel;
    private final Scene scene;
    private final Screen screen;
    private final GameState state;
    private final AnimationPlayer animationPlayer;
    private AnimationFrame activeFrame;

    public GameApp() throws Exception {
        super("Covid, The Zombie Apocalypse");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        screen = new Screen(COLS, ROWS);
        ArtAsset playerArt = ArtAsset.fromFile("assets/art/player.txt");
        ArtAsset walkerArt = ArtAsset.fromFile("assets/art/walker.txt");
        ArtAsset backdrop = ArtAsset.fromFile("assets/art/room0.txt");
        scene = new Scene(COLS, ROWS, playerArt, walkerArt, backdrop);
        state = GameState.loadHospital();
        panel = new RenderPanel(COLS, ROWS);

        animationPlayer = new AnimationPlayer(
                frame -> { activeFrame = frame; redraw(); },
                () -> { state.playerAttack(); activeFrame = null; redraw(); });

        JPanel buttons = new JPanel(new GridLayout(2, 4));
        buttons.add(directionButton("North"));
        buttons.add(directionButton("South"));
        buttons.add(directionButton("East"));
        buttons.add(directionButton("West"));

        JButton attack = new JButton("Attack");
        attack.addActionListener(e -> {
            if (!animationPlayer.isPlaying() && state.isInCombat()) {
                animationPlayer.play(Animations.playerStrike());
            }
        });
        buttons.add(attack);

        buttons.add(stubButton("Item"));
        buttons.add(stubButton("Save"));
        buttons.add(stubButton("-"));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);

        redraw();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JButton directionButton(String label) {
        JButton b = new JButton(label);
        b.addActionListener(e -> {
            state.getLog().append("Movement is not wired up yet (Phase 1 slice).");
            redraw();
        });
        return b;
    }

    private JButton stubButton(String label) {
        JButton b = new JButton(label);
        b.addActionListener(e -> {
            state.getLog().append(label + " is not wired up yet (Phase 1 slice).");
            redraw();
        });
        return b;
    }

    private void redraw() {
        scene.render(screen, state, activeFrame);
        panel.setScreen(screen);
        panel.repaint();
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

package adventure_game.engine;

import java.util.function.Consumer;

import javax.swing.Timer;

/**
 * Plays an {@link Animation} as a burst on the Swing event thread.
 *
 * For each frame it calls {@code onFrame(frame)} (the caller stores it as
 * the active frame and repaints), waiting the frame's duration. After the
 * last frame it calls {@code onFrame(null)} then {@code onSettle} (the
 * caller resolves game state and does a full redraw).
 */
public class AnimationPlayer {
    private final Consumer<AnimationFrame> onFrame;
    private final Runnable onSettle;
    private Timer timer;
    private Animation anim;
    private int index;

    public AnimationPlayer(Consumer<AnimationFrame> onFrame, Runnable onSettle) {
        this.onFrame = onFrame;
        this.onSettle = onSettle;
    }

    public boolean isPlaying() {
        return timer != null && timer.isRunning();
    }

    public void play(Animation animation) {
        if (isPlaying()) {
            return;
        }
        this.anim = animation;
        this.index = 0;
        if (animation.size() == 0) {
            onSettle.run();
            return;
        }
        onFrame.accept(animation.frames().get(0));
        timer = new Timer(animation.frames().get(0).getDurationMs(), null);
        timer.setRepeats(true);
        timer.addActionListener(e -> {
            index++;
            if (index >= anim.size()) {
                timer.stop();
                onFrame.accept(null);
                onSettle.run();
                return;
            }
            AnimationFrame frame = anim.frames().get(index);
            onFrame.accept(frame);
            timer.setDelay(frame.getDurationMs());
        });
        timer.start();
    }
}

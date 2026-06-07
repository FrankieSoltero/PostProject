package adventure_game.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** An ordered sequence of frames played as one animation burst. */
public class Animation {
    private final List<AnimationFrame> frames;

    public Animation(List<AnimationFrame> frames) {
        this.frames = new ArrayList<>(frames);
    }

    public List<AnimationFrame> frames() {
        return Collections.unmodifiableList(frames);
    }

    public int size() {
        return frames.size();
    }
}

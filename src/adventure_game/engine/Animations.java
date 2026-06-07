package adventure_game.engine;

import java.util.ArrayList;
import java.util.List;

/** Factory for concrete animations used by the game. */
public final class Animations {

    private Animations() {}

    /**
     * The player's attack: a strike marker travels left-to-right from the
     * player toward the enemy, ending on an impact "*". Offsets are columns
     * relative to the player's blit position; Scene applies them.
     */
    public static Animation playerStrike() {
        ArtAsset strike = ArtAsset.fromLines(">");
        ArtAsset impact = ArtAsset.fromLines("*");
        List<AnimationFrame> frames = new ArrayList<>();
        frames.add(new AnimationFrame(strike, 8, 0, 60));
        frames.add(new AnimationFrame(strike, 16, 0, 60));
        frames.add(new AnimationFrame(strike, 24, 0, 60));
        frames.add(new AnimationFrame(strike, 30, 0, 60));
        frames.add(new AnimationFrame(impact, 34, 0, 90));
        return new Animation(frames);
    }

    /**
     * The player's defend: a sizeable shield pulses in front of the player,
     * clearly telegraphing the block before the exchange resolves.
     */
    public static Animation playerGuard() {
        ArtAsset shieldA = ArtAsset.fromLines(" __ ", "(##)", " \\/ ");
        ArtAsset shieldB = ArtAsset.fromLines(".||.", "<##>", "'||'");
        List<AnimationFrame> frames = new ArrayList<>();
        frames.add(new AnimationFrame(shieldA, 7, -1, 80));
        frames.add(new AnimationFrame(shieldB, 7, -1, 80));
        frames.add(new AnimationFrame(shieldA, 7, -1, 80));
        frames.add(new AnimationFrame(shieldB, 7, -1, 80));
        frames.add(new AnimationFrame(shieldA, 7, -1, 100));
        return new Animation(frames);
    }
}

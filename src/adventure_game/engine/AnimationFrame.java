package adventure_game.engine;

/** One frame of an animation: art drawn at an (dx,dy) offset for a duration. */
public class AnimationFrame {
    private final ArtAsset art;
    private final int dx;
    private final int dy;
    private final int durationMs;

    public AnimationFrame(ArtAsset art, int dx, int dy, int durationMs) {
        this.art = art;
        this.dx = dx;
        this.dy = dy;
        this.durationMs = durationMs;
    }

    public ArtAsset getArt() { return art; }
    public int getDx() { return dx; }
    public int getDy() { return dy; }
    public int getDurationMs() { return durationMs; }
}

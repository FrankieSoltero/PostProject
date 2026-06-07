package adventure_game.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A piece of ASCII art: a list of text lines plus its bounding size.
 * Space characters are treated as transparent when blitted (see
 * {@link Screen#blit}).
 */
public class ArtAsset {
    private final List<String> lines;
    private final int width;
    private final int height;

    public ArtAsset(List<String> lines) {
        this.lines = new ArrayList<>(lines);
        int w = 0;
        for (String line : lines) {
            w = Math.max(w, line.length());
        }
        this.width = w;
        this.height = lines.size();
    }

    public static ArtAsset fromLines(String... lines) {
        return new ArtAsset(Arrays.asList(lines));
    }

    public static ArtAsset fromFile(String path) throws IOException {
        return new ArtAsset(Files.readAllLines(Path.of(path)));
    }

    public List<String> lines() {
        return Collections.unmodifiableList(lines);
    }

    public int width() { return width; }

    public int height() { return height; }
}

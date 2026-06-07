package adventure_game.engine;

/**
 * A fixed-size character frame buffer. The renderer recomposes the whole
 * Screen each redraw (the opposite of an append-only log) and a
 * {@link RenderPanel} paints it in a monospaced font.
 */
public class Screen {
    private final int width;
    private final int height;
    private final char[][] cells; // [row][col]

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new char[height][width];
        clear();
    }

    public int width() { return width; }

    public int height() { return height; }

    public void clear() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = ' ';
            }
        }
    }

    public void put(int x, int y, char c) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return;
        }
        cells[y][x] = c;
    }

    public char get(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return ' ';
        }
        return cells[y][x];
    }

    public void drawText(int x, int y, String s) {
        for (int i = 0; i < s.length(); i++) {
            put(x + i, y, s.charAt(i));
        }
    }

    public void drawBox(int x, int y, int w, int h) {
        for (int i = 1; i < w - 1; i++) {
            put(x + i, y, '-');
            put(x + i, y + h - 1, '-');
        }
        for (int j = 1; j < h - 1; j++) {
            put(x, y + j, '|');
            put(x + w - 1, y + j, '|');
        }
        put(x, y, '+');
        put(x + w - 1, y, '+');
        put(x, y + h - 1, '+');
        put(x + w - 1, y + h - 1, '+');
    }

    /** Draws art with space treated as transparent (composites over backdrop). */
    public void blit(ArtAsset art, int x, int y) {
        java.util.List<String> lines = art.lines();
        for (int row = 0; row < lines.size(); row++) {
            String line = lines.get(row);
            for (int col = 0; col < line.length(); col++) {
                char c = line.charAt(col);
                if (c != ' ') {
                    put(x + col, y + row, c);
                }
            }
        }
    }

    public String[] toLines() {
        String[] out = new String[height];
        for (int y = 0; y < height; y++) {
            out[y] = new String(cells[y]);
        }
        return out;
    }
}

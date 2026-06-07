package adventure_game.engine;

/** Reusable on-grid UI pieces rendered as text, drawn via Screen.drawText. */
public final class HudComponents {

    private HudComponents() {}

    /** A bracketed bar of `width` cells: '|' filled, '.' empty. */
    public static String healthBar(int current, int max, int width) {
        if (max <= 0) {
            max = 1;
        }
        int filled = (int) Math.round((double) current / max * width);
        if (filled < 0) {
            filled = 0;
        }
        if (filled > width) {
            filled = width;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < width; i++) {
            sb.append(i < filled ? '|' : '.');
        }
        sb.append(']');
        return sb.toString();
    }
}

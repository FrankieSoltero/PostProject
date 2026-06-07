package adventure_game.engine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JPanel;

/** Paints a Screen in a monospaced font, one row per text line. */
public class RenderPanel extends JPanel {
    private final int cols;
    private final int rows;
    private final int cellW;
    private final int cellH;
    private final int ascent;
    private Screen screen;

    public RenderPanel(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        setBackground(Color.BLACK);
        Font font = new Font("Monospaced", Font.PLAIN, 16);
        setFont(font);
        FontMetrics fm = getFontMetrics(font);
        this.cellW = fm.charWidth('M');
        this.cellH = fm.getHeight();
        this.ascent = fm.getAscent();
        setPreferredSize(new Dimension(cols * cellW + 20, rows * cellH + 20));
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (screen == null) {
            return;
        }
        g.setFont(getFont());
        g.setColor(new Color(120, 230, 120));
        String[] lines = screen.toLines();
        for (int y = 0; y < lines.length; y++) {
            g.drawString(lines[y], 10, 10 + ascent + y * cellH);
        }
    }
}

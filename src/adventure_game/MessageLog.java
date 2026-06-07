package adventure_game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model-side message sink. Replaces direct writes to a Swing
 * {@code JTextArea}. The model appends narration here; the renderer
 * reads it to fill the on-screen log region.
 *
 * {@link #append(String)} accepts multi-line strings (the legacy code
 * appended strings containing "\n") and stores them as individual,
 * trimmed, non-blank lines.
 */
public class MessageLog {
    private final List<String> lines = new ArrayList<>();

    public void append(String text) {
        if (text == null) {
            return;
        }
        for (String part : text.split("\n")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                lines.add(trimmed);
            }
        }
    }

    public List<String> lines() {
        return Collections.unmodifiableList(lines);
    }

    public List<String> recent(int n) {
        int from = Math.max(0, lines.size() - n);
        return new ArrayList<>(lines.subList(from, lines.size()));
    }

    public void clear() {
        lines.clear();
    }

    public int size() {
        return lines.size();
    }
}

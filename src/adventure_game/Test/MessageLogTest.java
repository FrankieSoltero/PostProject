package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import adventure_game.MessageLog;

public class MessageLogTest {

    @Test
    void appendSplitsOnNewlinesAndSkipsBlanks() {
        MessageLog log = new MessageLog();
        log.append("first line\nsecond line\n");
        assertEquals(2, log.size());
        assertEquals("first line", log.lines().get(0));
        assertEquals("second line", log.lines().get(1));
    }

    @Test
    void recentReturnsTailInOrder() {
        MessageLog log = new MessageLog();
        log.append("a");
        log.append("b");
        log.append("c");
        List<String> last2 = log.recent(2);
        assertEquals(2, last2.size());
        assertEquals("b", last2.get(0));
        assertEquals("c", last2.get(1));
    }
}

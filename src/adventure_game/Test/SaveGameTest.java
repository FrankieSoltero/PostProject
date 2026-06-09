package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import adventure_game.GameState;
import adventure_game.SaveGame;
import adventure_game.Weapon;

public class SaveGameTest {

    @Test
    void saveThenLoadFromFileRoundTrips(@TempDir Path dir) throws Exception {
        GameState gs = GameState.loadHospital();
        gs.getPlayer().equipWeapon(new Weapon("Pistol", 30), gs.getLog());
        gs.getRoom(2).removeNPC();

        Path f = SaveGame.slotFile(dir, "run1");
        SaveGame.save(gs, f);
        assertTrue(Files.exists(f));

        GameState loaded = SaveGame.load(f);
        assertEquals("Pistol", loaded.getPlayer().getWeaponName());
        assertEquals(30, loaded.getPlayer().getWeaponBonus());
        assertEquals(-1, loaded.getRoom(2).hasNPC());
        assertEquals(4, loaded.getRoom(10).hasNPC());
        assertTrue(loaded.getRoom(24).hasCure());
    }

    @Test
    void listSlotsReturnsSortedSavedNames(@TempDir Path dir) throws Exception {
        GameState gs = GameState.loadHospital();
        SaveGame.save(gs, SaveGame.slotFile(dir, "beta"));
        SaveGame.save(gs, SaveGame.slotFile(dir, "alpha"));
        assertEquals(List.of("alpha", "beta"), SaveGame.listSlots(dir));
    }

    @Test
    void listSlotsEmptyForMissingDir(@TempDir Path dir) throws Exception {
        assertTrue(SaveGame.listSlots(dir.resolve("does-not-exist")).isEmpty());
    }

    @Test
    void loadMalformedFileThrows(@TempDir Path dir) throws Exception {
        Path f = dir.resolve("bad.txt");
        Files.write(f, List.of("not a real save"));
        assertThrows(Exception.class, () -> SaveGame.load(f));
    }

    @Test
    void saveIsBomFree(@TempDir Path dir) throws Exception {
        GameState gs = GameState.loadHospital();
        Path f = SaveGame.slotFile(dir, "run");
        SaveGame.save(gs, f);
        byte[] b = Files.readAllBytes(f);
        boolean bom = b.length >= 3 && (b[0] & 0xFF) == 0xEF && (b[1] & 0xFF) == 0xBB && (b[2] & 0xFF) == 0xBF;
        assertFalse(bom);
    }
}

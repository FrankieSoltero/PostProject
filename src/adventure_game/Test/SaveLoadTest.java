package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.GameState;
import adventure_game.SaveData;
import adventure_game.Weapon;

public class SaveLoadTest {

    @Test
    void toSaveDataThenFromSaveRoundTripsPlayerAndRooms() throws Exception {
        GameState gs = GameState.loadHospital();
        gs.getPlayer().equipWeapon(new Weapon("AssaultRifle", 50), gs.getLog());
        gs.getRoom(2).removeNPC(); // simulate a cleared room

        SaveData snap = gs.toSaveData();
        GameState restored = GameState.fromSave(snap);
        SaveData snap2 = restored.toSaveData();

        assertEquals(snap.weaponName, snap2.weaponName);
        assertEquals(snap.weaponBonus, snap2.weaponBonus);
        assertEquals(snap.level, snap2.level);
        assertEquals(snap.currentRoom, snap2.currentRoom);
        assertEquals(-1, restored.getRoom(2).hasNPC(), "cleared room stays cleared");
        assertEquals(4, restored.getRoom(10).hasNPC(), "boss room stays a boss");
        assertTrue(restored.getRoom(24).hasCure(), "cure persists in room 24");
        assertEquals(GameState.Mode.EXPLORE, restored.getMode());
    }
}

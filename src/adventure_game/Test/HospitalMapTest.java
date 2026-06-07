package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import adventure_game.HospitalMap;
import adventure_game.Room;

public class HospitalMapTest {

    @Test
    void loadsAllTwentyFiveRooms() throws Exception {
        List<Room> rooms = HospitalMap.load(HospitalMap.HOSPITAL_PATH);
        assertEquals(25, rooms.size());
        assertEquals("Hospital Entrance", rooms.get(0).getRoomName());
        assertEquals("Fauccis Lair", rooms.get(24).getRoomName());
    }

    @Test
    void wiresKnownExits() throws Exception {
        List<Room> rooms = HospitalMap.load(HospitalMap.HOSPITAL_PATH);
        assertTrue(rooms.get(0).isRoomNorthNull());
        assertEquals(2, rooms.get(0).getSouthRoom().getRoomNumber());
        assertEquals(1, rooms.get(0).getEastRoom().getRoomNumber());
        assertTrue(rooms.get(24).isRoomNorthNull());
        assertTrue(rooms.get(24).isRoomSouthNull());
        assertTrue(rooms.get(24).isRoomEastNull());
        assertTrue(rooms.get(24).isRoomWestNull());
    }
}

package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.BeforeEach;

import adventure_game.Room;

public class RoomTest {
    private Room F;
    
    @BeforeEach
    void setup() {
        F = new Room(0, "Supply Room", "This is a Supply Room");
    }

    @Test
    void testGetters() {
        assertTrue(F.getRoomNumber() == 0);
        assertTrue(F.getRoomName() == "Supply Room");
        assertTrue(F.getRoomBio() == "This is a Supply Room");
    }



}

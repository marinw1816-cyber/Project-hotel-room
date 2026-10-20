package hotel.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class RoomTest {
    @Test
    void storesValues() {
        Room room = new Room(101, 2, new BigDecimal("89.90"));
        assertEquals(101, room.getNumber());
        assertEquals(2, room.getCapacity());
        assertEquals(new BigDecimal("89.90"), room.getPricePerNight());
    }

    @Test
    void rejectsInvalidNumberOrCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new Room(0, 2, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new Room(1, 0, BigDecimal.TEN));
    }

    @Test
    void rejectsNegativeOrMissingPrice() {
        assertThrows(IllegalArgumentException.class, () -> new Room(1, 2, new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> new Room(1, 2, null));
    }
}

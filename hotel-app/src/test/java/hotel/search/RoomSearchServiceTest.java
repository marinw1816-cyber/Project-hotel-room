package hotel.search;

import static org.junit.jupiter.api.Assertions.*;

import hotel.model.Reservation;
import hotel.model.ReservationStore;
import hotel.model.Room;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoomSearchServiceTest {
    private final Room small = new Room(101, 2, BigDecimal.TEN);
    private final Room big = new Room(102, 4, BigDecimal.TEN);
    private final LocalDate in = LocalDate.of(2026, 7, 1);
    private final LocalDate out = in.plusDays(3);
    private final ReservationStore store = new ReservationStore();
    private final RoomSearchService service = new RoomSearchService(List.of(small, big), store);

    @Test
    void excludesRoomsThatAreTooSmall() {
        assertEquals(List.of(big), service.findAvailableRooms(in, out, 3));
    }

    @Test
    void excludesOccupiedRoom() {
        store.add(new Reservation(small, "Anna", in.plusDays(1), in.plusDays(2)));
        assertEquals(List.of(big), service.findAvailableRooms(in, out, 2));
    }

    @Test
    void backToBackBookingKeepsRoomAvailable() {
        store.add(new Reservation(small, "Anna", in.minusDays(2), in));
        store.add(new Reservation(big, "Ben", out, out.plusDays(2)));
        assertEquals(List.of(small, big), service.findAvailableRooms(in, out, 2));
    }

    @Test
    void cancelledReservationFreesRoom() {
        Reservation r = new Reservation(small, "Anna", in, out);
        store.add(r);
        assertEquals(List.of(big), service.findAvailableRooms(in, out, 2));
        store.cancel(r);
        assertEquals(List.of(small, big), service.findAvailableRooms(in, out, 2));
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> service.findAvailableRooms(out, in, 2));
        assertThrows(IllegalArgumentException.class, () -> service.findAvailableRooms(in, out, 0));
    }
}

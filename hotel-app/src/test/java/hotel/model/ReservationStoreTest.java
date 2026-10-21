package hotel.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ReservationStoreTest {
    private final Room room = new Room(101, 2, BigDecimal.TEN);
    private final LocalDate in = LocalDate.of(2026, 7, 1);
    private final ReservationStore store = new ReservationStore();

    @Test
    void addAndFind() {
        store.add(new Reservation(room, "Anna", in, in.plusDays(2)));
        assertEquals(1, store.findAll().size());
        assertEquals(1, store.findByGuest("Anna").size());
        assertTrue(store.findByGuest("Ben").isEmpty());
    }

    @Test
    void rejectsOverlappingReservationForSameRoom() {
        store.add(new Reservation(room, "Anna", in, in.plusDays(3)));
        assertThrows(IllegalStateException.class,
                () -> store.add(new Reservation(room, "Ben", in.plusDays(1), in.plusDays(4))));
    }

    @Test
    void allowsOtherRoomOrBackToBackStay() {
        store.add(new Reservation(room, "Anna", in, in.plusDays(3)));
        store.add(new Reservation(new Room(102, 2, BigDecimal.TEN), "Ben", in, in.plusDays(3)));
        store.add(new Reservation(room, "Cara", in.plusDays(3), in.plusDays(5)));
        assertEquals(3, store.findAll().size());
    }

    @Test
    void cancelFreesRoom() {
        Reservation r = new Reservation(room, "Anna", in, in.plusDays(3));
        store.add(r);
        assertTrue(store.cancel(r));
        assertEquals(ReservationStatus.CANCELLED, r.getStatus());
        assertTrue(store.isAvailable(room, in, in.plusDays(3)));
    }
}

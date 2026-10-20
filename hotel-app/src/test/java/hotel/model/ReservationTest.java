package hotel.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ReservationTest {
    private final Room room = new Room(101, 2, BigDecimal.TEN);
    private final LocalDate in = LocalDate.of(2026, 7, 1);

    @Test
    void newReservationIsPendingAndCountsNights() {
        Reservation r = new Reservation(room, "Anna", in, in.plusDays(3));
        assertEquals(ReservationStatus.PENDING, r.getStatus());
        assertEquals(3, r.nights());
    }

    @Test
    void checkOutMustBeAfterCheckIn() {
        assertThrows(IllegalArgumentException.class, () -> new Reservation(room, "Anna", in, in));
        assertThrows(IllegalArgumentException.class, () -> new Reservation(room, "Anna", in, in.minusDays(1)));
    }

    @Test
    void rejectsMissingData() {
        assertThrows(IllegalArgumentException.class, () -> new Reservation(null, "Anna", in, in.plusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> new Reservation(room, " ", in, in.plusDays(1)));
    }

    @Test
    void detectsOverlap() {
        Reservation r = new Reservation(room, "Anna", in, in.plusDays(3));
        assertTrue(r.overlaps(in.plusDays(2), in.plusDays(5)));
        assertFalse(r.overlaps(in.plusDays(3), in.plusDays(5)));
    }
}

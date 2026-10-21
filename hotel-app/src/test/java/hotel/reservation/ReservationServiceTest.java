package hotel.reservation;

import static org.junit.jupiter.api.Assertions.*;

import hotel.model.Reservation;
import hotel.model.ReservationStatus;
import hotel.model.ReservationStore;
import hotel.model.Room;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ReservationServiceTest {
    private final Room room = new Room(101, 2, new BigDecimal("89.90"));
    private final LocalDate in = LocalDate.of(2026, 7, 1);
    private final ReservationStore store = new ReservationStore();
    private final ReservationService service = new ReservationService(store);

    @Test
    void confirmSetsPendingToConfirmed() {
        Reservation r = new Reservation(room, "Anna", in, in.plusDays(2));
        service.confirm(r);
        assertEquals(ReservationStatus.CONFIRMED, r.getStatus());
    }

    @Test
    void confirmRejectsNonPending() {
        Reservation r = new Reservation(room, "Anna", in, in.plusDays(2));
        r.setStatus(ReservationStatus.CONFIRMED);
        assertThrows(IllegalStateException.class, () -> service.confirm(r));
        r.setStatus(ReservationStatus.CANCELLED);
        assertThrows(IllegalStateException.class, () -> service.confirm(r));
    }

    @Test
    void changeDatesReplacesReservationAndKeepsStatus() {
        Reservation old = new Reservation(room, "Anna", in, in.plusDays(3));
        store.add(old);
        service.confirm(old);
        Reservation updated = service.changeDates(old, in.plusDays(1), in.plusDays(5));
        assertEquals(ReservationStatus.CANCELLED, old.getStatus());
        assertEquals(ReservationStatus.CONFIRMED, updated.getStatus());
        assertEquals("Anna", updated.getGuest());
        assertEquals(in.plusDays(1), updated.getCheckIn());
        assertEquals(in.plusDays(5), updated.getCheckOut());
        assertTrue(store.findAll().contains(updated));
    }

    @Test
    void changeDatesRejectsOverlapWithOtherReservation() {
        Reservation old = new Reservation(room, "Anna", in, in.plusDays(2));
        store.add(old);
        store.add(new Reservation(room, "Ben", in.plusDays(3), in.plusDays(5)));
        assertThrows(IllegalStateException.class, () -> service.changeDates(old, in.plusDays(1), in.plusDays(4)));
        assertEquals(ReservationStatus.PENDING, old.getStatus());
        assertEquals(2, store.findAll().size());
    }

    @Test
    void changeDatesIgnoresCancelledAndOtherRooms() {
        Reservation old = new Reservation(room, "Anna", in, in.plusDays(2));
        store.add(old);
        Reservation cancelled = new Reservation(room, "Ben", in.plusDays(3), in.plusDays(5));
        store.add(cancelled);
        store.cancel(cancelled);
        store.add(new Reservation(new Room(102, 2, BigDecimal.TEN), "Cara", in, in.plusDays(6)));
        Reservation updated = service.changeDates(old, in.plusDays(2), in.plusDays(5));
        assertEquals(ReservationStatus.PENDING, updated.getStatus());
    }

    @Test
    void changeDatesRejectsInvalidDatesWithoutCancelling() {
        Reservation old = new Reservation(room, "Anna", in, in.plusDays(2));
        store.add(old);
        assertThrows(IllegalArgumentException.class, () -> service.changeDates(old, in.plusDays(3), in.plusDays(3)));
        assertEquals(ReservationStatus.PENDING, old.getStatus());
    }

    @Test
    void totalPriceMultipliesPriceByNights() {
        Reservation r = new Reservation(room, "Anna", in, in.plusDays(3));
        assertEquals(new BigDecimal("269.70"), service.totalPrice(r));
    }
}

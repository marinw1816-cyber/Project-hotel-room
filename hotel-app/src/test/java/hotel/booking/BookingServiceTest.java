package hotel.booking;

import static org.junit.jupiter.api.Assertions.*;

import hotel.model.Reservation;
import hotel.model.ReservationStatus;
import hotel.model.ReservationStore;
import hotel.model.Room;
import hotel.reservation.ReservationService;
import hotel.search.RoomSearchService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class BookingServiceTest {
    private final Room single = new Room(101, 1, new BigDecimal("49.00"));
    private final Room doubleCheap = new Room(102, 2, new BigDecimal("79.00"));
    private final Room doubleExpensive = new Room(103, 2, new BigDecimal("99.00"));
    private final LocalDate in = LocalDate.of(2026, 7, 1);
    private final LocalDate out = in.plusDays(3);
    private final ReservationStore store = new ReservationStore();
    private final RoomSearchService search =
            new RoomSearchService(List.of(doubleExpensive, single, doubleCheap), store);
    private final ReservationService reservationService = new ReservationService(store);
    private final BookingService booking = new BookingService(search, store, reservationService);

    @Test
    void constructorRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new BookingService(null, store, reservationService));
        assertThrows(IllegalArgumentException.class, () -> new BookingService(search, null, reservationService));
        assertThrows(IllegalArgumentException.class, () -> new BookingService(search, store, null));
    }

    @Test
    void bookTakesCheapestMatchingRoom() {
        Reservation r = booking.book("Anna", in, out, 1);
        assertEquals(101, r.getRoom().getNumber());
        assertEquals(ReservationStatus.PENDING, r.getStatus());
        assertEquals("Anna", r.getGuest());
        assertTrue(store.findAll().contains(r));
    }

    @Test
    void bookSkipsRoomsThatAreTooSmall() {
        Reservation r = booking.book("Anna", in, out, 2);
        assertEquals(102, r.getRoom().getNumber());
    }

    @Test
    void bookThrowsWhenNoRoomAvailable() {
        assertThrows(IllegalStateException.class, () -> booking.book("Anna", in, out, 3));
        assertTrue(store.findAll().isEmpty());
    }

    @Test
    void secondBookingInSamePeriodGetsOtherRoom() {
        Reservation first = booking.book("Anna", in, out, 2);
        Reservation second = booking.book("Ben", in.plusDays(1), out.plusDays(1), 2);
        assertEquals(102, first.getRoom().getNumber());
        assertEquals(103, second.getRoom().getNumber());
        assertThrows(IllegalStateException.class, () -> booking.book("Cara", in, out, 2));
    }

    @Test
    void confirmSetsConfirmed() {
        Reservation r = booking.book("Anna", in, out, 1);
        booking.confirm(r);
        assertEquals(ReservationStatus.CONFIRMED, r.getStatus());
    }

    @Test
    void totalPriceIsPriceTimesNights() {
        Reservation r = booking.book("Anna", in, out, 2);
        assertEquals(new BigDecimal("237.00"), booking.totalPrice(r));
    }
}

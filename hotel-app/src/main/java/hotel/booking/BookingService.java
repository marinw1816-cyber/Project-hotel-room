package hotel.booking;

import hotel.model.Reservation;
import hotel.model.ReservationStore;
import hotel.model.Room;
import hotel.reservation.ReservationService;
import hotel.search.RoomSearchService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;

public class BookingService {
    private final RoomSearchService searchService;
    private final ReservationStore store;
    private final ReservationService reservationService;

    public BookingService(RoomSearchService searchService, ReservationStore store,
                          ReservationService reservationService) {
        if (searchService == null || store == null || reservationService == null) {
            throw new IllegalArgumentException("Services and store must not be null");
        }
        this.searchService = searchService;
        this.store = store;
        this.reservationService = reservationService;
    }

    public Reservation book(String guest, LocalDate checkIn, LocalDate checkOut, int guests) {
        Room cheapest = searchService.findAvailableRooms(checkIn, checkOut, guests).stream()
                .min(Comparator.comparing(Room::getPricePerNight))
                .orElseThrow(() -> new IllegalStateException("No suitable room available"));
        Reservation reservation = new Reservation(cheapest, guest, checkIn, checkOut);
        store.add(reservation);
        return reservation;
    }

    public void confirm(Reservation reservation) {
        reservationService.confirm(reservation);
    }

    public BigDecimal totalPrice(Reservation reservation) {
        return reservationService.totalPrice(reservation);
    }
}

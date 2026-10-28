package hotel.app;

import hotel.booking.BookingService;
import hotel.reservation.ReservationDemo;
import hotel.model.Reservation;
import hotel.model.ReservationStore;
import hotel.model.Room;
import hotel.reservation.ReservationService;
import hotel.search.RoomSearchService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Hotel-App Demo ===");

        List<Room> rooms = List.of(
                new Room(101, 1, new BigDecimal("60.00")),
                new Room(102, 2, new BigDecimal("90.00")),
                new Room(201, 4, new BigDecimal("150.00")));

        ReservationStore store = new ReservationStore();
        RoomSearchService searchService = new RoomSearchService(rooms, store);
        ReservationService reservationService = new ReservationService(store);
        BookingService bookingService = new BookingService(searchService, store, reservationService);

        LocalDate checkIn = LocalDate.of(2026, 12, 1);
        LocalDate checkOut = LocalDate.of(2026, 12, 4);
        int guests = 2;

        System.out.println("Freie Zimmer vom " + checkIn + " bis " + checkOut + " für " + guests + " Personen:");
        for (Room room : searchService.findAvailableRooms(checkIn, checkOut, guests)) {
            System.out.println("  Zimmer " + room.getNumber() + " (" + room.getCapacity() + " Personen, "
                    + room.getPricePerNight() + " EUR/Nacht)");
        }

        Reservation reservation = bookingService.book("Max Mustermann", checkIn, checkOut, guests);
        bookingService.confirm(reservation);
        System.out.println("Buchung: " + reservation.getGuest() + ", Zimmer " + reservation.getRoom().getNumber()
                + ", " + reservation.nights() + " Nächte, Status " + reservation.getStatus()
                + ", Gesamtpreis " + bookingService.totalPrice(reservation) + " EUR");

        System.out.println("Freie Zimmer nach der Buchung:");
        for (Room room : searchService.findAvailableRooms(checkIn, checkOut, guests)) {
            System.out.println("  Zimmer " + room.getNumber());
        }

         ReservationDemo.run();
        // Sean: call UserDemo.run() here
    }
}

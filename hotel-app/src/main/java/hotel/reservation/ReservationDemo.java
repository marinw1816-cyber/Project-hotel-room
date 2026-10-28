package hotel.reservation;

import hotel.model.Reservation;
import hotel.model.ReservationStore;
import hotel.model.Room;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ReservationDemo {
    public static void run() {
        System.out.println("=== Reservation Demo ===");
        Room room = new Room(301, 2, new BigDecimal("80.00"));
        ReservationStore store = new ReservationStore();
        ReservationService service = new ReservationService(store);
        Reservation r = new Reservation(room, "Anna", LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12));
        store.add(r);
        service.confirm(r);
        Reservation changed = service.changeDates(r, LocalDate.of(2026, 12, 11), LocalDate.of(2026, 12, 14));
        System.out.println("Changed: " + changed.getCheckIn() + " to " + changed.getCheckOut() + ", status " + changed.getStatus());
        System.out.println("Total price: " + service.totalPrice(changed) + " EUR");
    }
}

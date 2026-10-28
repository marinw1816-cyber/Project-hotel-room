package hotel.reservation;

import hotel.model.Reservation;
import hotel.model.ReservationStatus;
import hotel.model.ReservationStore;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ReservationService {
    private final ReservationStore store;

    public ReservationService(ReservationStore store) {
        if (store == null) {
            throw new IllegalArgumentException("Store is required");
        }
        this.store = store;
    }

    public void confirm(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Only open reservations can be confirmed");
        }
        reservation.setStatus(ReservationStatus.CONFIRMED);
    }

    public Reservation changeDates(Reservation old, LocalDate in, LocalDate out) {
        if (!store.findAll().contains(old)) {
            throw new IllegalArgumentException("Reservation is not in the store");
        }
        if (old.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("A cancelled reservation cannot be changed");
        }
        // The constructor validates the new data before anything is changed
        Reservation updated = new Reservation(old.getRoom(), old.getGuest(), in, out);
        boolean conflict = store.findAll().stream()
                .filter(r -> r != old)
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .filter(r -> r.getRoom().getNumber() == old.getRoom().getNumber())
                .anyMatch(r -> r.overlaps(in, out));
        if (conflict) {
            throw new IllegalStateException("Room is not available in this period");
        }
        ReservationStatus status = old.getStatus();
        store.cancel(old);
        updated.setStatus(status);
        store.add(updated);
        return updated;
    }

    public BigDecimal totalPrice(Reservation reservation) {
        return reservation.getRoom().getPricePerNight().multiply(BigDecimal.valueOf(reservation.nights()));
    }
}

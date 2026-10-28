package hotel.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationStore {
    private final List<Reservation> reservations = new ArrayList<>();

    public void add(Reservation reservation) {
        if (!isAvailable(reservation.getRoom(), reservation.getCheckIn(), reservation.getCheckOut())) {
            throw new IllegalStateException("Room is not available in this period");
        }
        reservations.add(reservation);
    }

    public List<Reservation> findAll() {
        return List.copyOf(reservations);
    }

    public List<Reservation> findByGuest(String guest) {
        return reservations.stream().filter(r -> r.getGuest().equals(guest)).toList();
    }

    public boolean isAvailable(Room room, LocalDate from, LocalDate to) {
        return reservations.stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .filter(r -> r.getRoom().getNumber() == room.getNumber())
                .noneMatch(r -> r.overlaps(from, to));
    }

    public boolean cancel(Reservation reservation) {
        if (!reservations.contains(reservation)) {
            return false;
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        return true;
    }
}

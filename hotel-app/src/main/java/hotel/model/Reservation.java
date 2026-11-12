package hotel.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reservation {
    private final Room room;
    private final String guest;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private ReservationStatus status = ReservationStatus.PENDING;

    public Reservation(Room room, String guest, LocalDate checkIn, LocalDate checkOut) {
        if (room == null) {
            throw new IllegalArgumentException("Room is required");
        }
        if (guest == null || guest.isBlank()) {
            throw new IllegalArgumentException("Guest is required");
        }
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Date is required");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }
        this.room = room;
        this.guest = guest;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public Room getRoom() {
        return room;
    }

    public String getGuest() {
        return guest;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public long nights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    /**
     * Checks if this reservation overlaps with the period from {@code from} to {@code to}.
     * Back-to-back stays do not overlap: check-out on the 4th and check-in on the 4th is allowed.
     */
    public boolean overlaps(LocalDate from, LocalDate to) {
        return checkIn.isBefore(to) && from.isBefore(checkOut);
    }
}

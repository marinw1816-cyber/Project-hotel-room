package hotel.reservation;

/** Status of a reservation. Stored as text in the database (column "status"). */
public enum ReservationStatus {
    PENDING, CONFIRMED, CANCELLED
}

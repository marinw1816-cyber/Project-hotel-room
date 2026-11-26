package hotel.reservation;

import hotel.room.RoomEntity;
import hotel.room.RoomRepository;
import hotel.user.UserEntity;
import hotel.user.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Search, booking and changes of reservations, stored in the database.
 * Replaces the old in-memory classes RoomSearchService, BookingService and ReservationService.
 *
 * Errors:
 * IllegalArgumentException = invalid input (dates, guests),
 * IllegalStateException = not possible now (room not free, already cancelled),
 * NoSuchElementException = room, user or reservation not found (also when it belongs to another user).
 *
 * The customer methods get the username of the logged-in user (from Authentication).
 * The reservation stores the link to the UserEntity (customer_id = userId).
 */
@Service
@Transactional
public class ReservationService {

    /** Used for a new reservation: there is no own reservation to ignore in the overlap check. */
    private static final long NO_ID = -1L;

    private final ReservationRepository reservations;
    private final RoomRepository rooms;
    private final UserRepository users;

    public ReservationService(ReservationRepository reservations, RoomRepository rooms, UserRepository users) {
        this.reservations = reservations;
        this.rooms = rooms;
        this.users = users;
    }

    /** Free rooms for the period that are big enough, cheapest first. */
    @Transactional(readOnly = true)
    public List<RoomEntity> findFreeRooms(LocalDate checkIn, LocalDate checkOut, int guests) {
        validate(checkIn, checkOut, guests, null);
        return reservations.findFreeRooms(guests, checkIn, checkOut, ReservationStatus.CANCELLED);
    }

    /** Books the room for the user. The new reservation is PENDING until the reception confirms it. */
    public ReservationEntity book(String customerUsername, int roomNumber, LocalDate checkIn, LocalDate checkOut, int guests) {
        if (customerUsername == null || customerUsername.isBlank()) {
            throw new IllegalArgumentException("User is required");
        }
        validate(checkIn, checkOut, guests, null);
        UserEntity customer = findUser(customerUsername);
        RoomEntity room = lockRoom(roomNumber);
        if (room.getCapacity() < guests) {
            throw new IllegalArgumentException("Room " + roomNumber + " is too small for " + guests + " guests");
        }
        checkFree(roomNumber, checkIn, checkOut, NO_ID);
        return reservations.save(new ReservationEntity(room, customer, checkIn, checkOut, guests));
    }

    /** "My reservations" of the user. */
    @Transactional(readOnly = true)
    public List<ReservationEntity> myReservations(String customerUsername) {
        return reservations.findByCustomerUserIdOrderByCheckInAsc(findUser(customerUsername).getUserId());
    }

    /** The user cancels one of his own reservations. */
    public ReservationEntity cancel(long id, String customerUsername) {
        ReservationEntity reservation = findOwn(id, customerUsername);
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation " + id + " is already cancelled");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservation;
    }

    /** The user changes the dates of one of his own reservations. Same room, no double booking. */
    public ReservationEntity changeDates(long id, String customerUsername, LocalDate checkIn, LocalDate checkOut) {
        return changeDates(findOwn(id, customerUsername), checkIn, checkOut);
    }

    // ---- For the reception (all reservations, not only the own ones) ----

    @Transactional(readOnly = true)
    public List<ReservationEntity> allReservations() {
        return reservations.findAllByOrderByCheckInAsc();
    }

    /** The reception changes the dates of any reservation. */
    public ReservationEntity changeDatesByStaff(long id, LocalDate checkIn, LocalDate checkOut) {
        return changeDates(find(id), checkIn, checkOut);
    }

    /** The reception confirms or cancels a reservation (or opens a cancelled one again, if the room is still free). */
    public ReservationEntity changeStatus(long id, ReservationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        ReservationEntity reservation = find(id);
        if (reservation.getStatus() == ReservationStatus.CANCELLED && status != ReservationStatus.CANCELLED) {
            lockRoom(reservation.getRoom().getRoomNumber());
            checkFree(reservation.getRoom().getRoomNumber(), reservation.getCheckIn(), reservation.getCheckOut(),
                    reservation.getReservationId());
        }
        reservation.setStatus(status);
        return reservation;
    }

    // ---- Helpers ----

    private ReservationEntity changeDates(ReservationEntity reservation, LocalDate checkIn, LocalDate checkOut) {
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("A cancelled reservation cannot be changed");
        }
        validate(checkIn, checkOut, reservation.getGuests(), reservation.getCheckIn());
        int roomNumber = reservation.getRoom().getRoomNumber();
        lockRoom(roomNumber);
        checkFree(roomNumber, checkIn, checkOut, reservation.getReservationId());
        reservation.setDates(checkIn, checkOut);
        return reservation;
    }

    /**
     * Loads the room with a write lock (SELECT ... FOR UPDATE) until the end of the transaction.
     * So two bookings of the same room at the same time run one after the other: no double booking.
     */
    private RoomEntity lockRoom(int roomNumber) {
        return rooms.findByIdForUpdate(roomNumber)
                .orElseThrow(() -> new NoSuchElementException("Room " + roomNumber + " not found"));
    }

    private void checkFree(int roomNumber, LocalDate checkIn, LocalDate checkOut, long ignoreId) {
        if (reservations.countOverlapping(roomNumber, checkIn, checkOut, ignoreId, ReservationStatus.CANCELLED) > 0) {
            throw new IllegalStateException("Room " + roomNumber + " is not free in this period");
        }
    }

    private ReservationEntity find(long id) {
        return reservations.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Reservation " + id + " not found"));
    }

    private UserEntity findUser(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User " + username + " not found"));
    }

    /** A reservation of another user is "not found", so nobody can see or change other reservations. */
    private ReservationEntity findOwn(long id, String customerUsername) {
        Long userId = findUser(customerUsername).getUserId();
        return reservations.findById(id)
                .filter(r -> r.getCustomer().getUserId().equals(userId))
                .orElseThrow(() -> new NoSuchElementException("Reservation " + id + " not found"));
    }

    /**
     * Checks dates and guests. oldCheckIn is null for a new booking. When the dates of a stay that has
     * already started are changed (e.g. the reception extends it), the old check-in may stay in the past.
     */
    private static void validate(LocalDate checkIn, LocalDate checkOut, int guests, LocalDate oldCheckIn) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check-out are required");
        }
        if (checkIn.isBefore(LocalDate.now()) && !checkIn.equals(oldCheckIn)) {
            throw new IllegalArgumentException("Check-in cannot be in the past");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }
        if (guests <= 0) {
            throw new IllegalArgumentException("Number of guests must be positive");
        }
    }
}

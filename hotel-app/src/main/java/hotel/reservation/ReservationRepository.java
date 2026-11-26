package hotel.reservation;

import hotel.room.RoomEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Database access for reservations. Spring Data JPA creates the implementation.
 * Two periods overlap when checkIn < other checkOut and other checkIn < checkOut,
 * so back-to-back stays (check-out and check-in on the same day) are allowed.
 */
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    /** "My reservations": all reservations of one customer (by userId), oldest check-in first. */
    List<ReservationEntity> findByCustomerUserIdOrderByCheckInAsc(Long userId);

    /** All reservations, for the reception. */
    List<ReservationEntity> findAllByOrderByCheckInAsc();

    /** True if the room has at least one reservation (then it cannot be deleted). */
    boolean existsByRoomRoomNumber(Integer roomNumber);

    /** Rooms that are big enough and have no active reservation in the period, cheapest first. */
    @Query("""
            select room from RoomEntity room
            where room.capacity >= :guests
              and not exists (
                  select r from ReservationEntity r
                  where r.room = room
                    and r.status <> :cancelled
                    and r.checkIn < :checkOut and :checkIn < r.checkOut)
            order by room.pricePerNight, room.roomNumber
            """)
    List<RoomEntity> findFreeRooms(@Param("guests") int guests,
                                   @Param("checkIn") LocalDate checkIn,
                                   @Param("checkOut") LocalDate checkOut,
                                   @Param("cancelled") ReservationStatus cancelled);

    /** Number of active reservations of the room in the period, without the reservation ignoreId. */
    @Query("""
            select count(r) from ReservationEntity r
            where r.room.roomNumber = :roomNumber
              and r.reservationId <> :ignoreId
              and r.status <> :cancelled
              and r.checkIn < :checkOut and :checkIn < r.checkOut
            """)
    long countOverlapping(@Param("roomNumber") Integer roomNumber,
                          @Param("checkIn") LocalDate checkIn,
                          @Param("checkOut") LocalDate checkOut,
                          @Param("ignoreId") Long ignoreId,
                          @Param("cancelled") ReservationStatus cancelled);
}

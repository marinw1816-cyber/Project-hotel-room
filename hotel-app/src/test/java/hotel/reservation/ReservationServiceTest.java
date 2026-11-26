package hotel.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hotel.app.HotelApplication;
import hotel.room.RoomEntity;
import hotel.room.RoomRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests search, booking and changes with the H2 test database (no MariaDB needed).
 * Every test runs in a transaction that is rolled back at the end, so the tests don't see each other's data.
 */
@SpringBootTest(classes = HotelApplication.class)
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService service;

    @Autowired
    private ReservationRepository reservations;

    @Autowired
    private RoomRepository rooms;

    /** Always in the future, so the "not in the past" rule never fails by accident. */
    private final LocalDate day1 = LocalDate.now().plusDays(10);
    private final LocalDate day4 = day1.plusDays(3);

    @BeforeEach
    void setUp() {
        reservations.deleteAll();
        rooms.deleteAll();
        rooms.save(new RoomEntity(101, 1, new BigDecimal("60.00")));
        rooms.save(new RoomEntity(102, 2, new BigDecimal("90.00")));
        rooms.save(new RoomEntity(201, 4, new BigDecimal("150.00")));
    }

    private static List<Integer> numbers(List<RoomEntity> list) {
        return list.stream().map(RoomEntity::getRoomNumber).toList();
    }

    @Test
    void freeRoomsAreBigEnoughAndCheapestFirst() {
        assertEquals(List.of(102, 201), numbers(service.findFreeRooms(day1, day4, 2)));
        assertEquals(List.of(101, 102, 201), numbers(service.findFreeRooms(day1, day4, 1)));
    }

    @Test
    void bookedRoomIsNotFreeButBackToBackIsAllowed() {
        service.book("anna", 102, day1, day4, 2);

        assertEquals(List.of(201), numbers(service.findFreeRooms(day1.plusDays(1), day4.plusDays(1), 2)));
        // check-out of anna and check-in on the same day: no overlap
        assertEquals(List.of(102, 201), numbers(service.findFreeRooms(day4, day4.plusDays(2), 2)));
    }

    @Test
    void bookSavesAPendingReservationWithTotalPrice() {
        ReservationEntity booked = service.book("anna", 102, day1, day4, 2);

        ReservationEntity stored = reservations.findById(booked.getId()).orElseThrow();
        assertEquals("anna", stored.getCustomerUsername());
        assertEquals(102, stored.getRoom().getRoomNumber().intValue());
        assertEquals(ReservationStatus.PENDING, stored.getStatus());
        assertEquals(3, stored.nights());
        assertEquals(0, new BigDecimal("270.00").compareTo(stored.totalPrice()));
    }

    @Test
    void doubleBookingIsBlocked() {
        service.book("anna", 102, day1, day4, 2);

        assertThrows(IllegalStateException.class, () -> service.book("ben", 102, day1.plusDays(2), day4.plusDays(2), 1));
        assertEquals(1, reservations.count());
    }

    @Test
    void invalidBookingsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.book("anna", 102, day4, day1, 2));
        assertThrows(IllegalArgumentException.class, () -> service.book("anna", 102, day1, day1, 2));
        assertThrows(IllegalArgumentException.class,
                () -> service.book("anna", 102, LocalDate.now().minusDays(1), day4, 2));
        assertThrows(IllegalArgumentException.class, () -> service.book("anna", 101, day1, day4, 2));
        assertThrows(IllegalArgumentException.class, () -> service.book("anna", 102, day1, day4, 0));
        assertThrows(IllegalArgumentException.class, () -> service.book(" ", 102, day1, day4, 2));
        assertThrows(NoSuchElementException.class, () -> service.book("anna", 999, day1, day4, 2));
        assertEquals(0, reservations.count());
    }

    @Test
    void myReservationsShowsOnlyTheOwnOnes() {
        service.book("anna", 102, day1, day4, 2);
        service.book("ben", 201, day1, day4, 3);
        service.book("anna", 101, day4, day4.plusDays(1), 1);

        List<ReservationEntity> mine = service.myReservations("anna");
        assertEquals(2, mine.size());
        assertEquals(List.of(102, 101), mine.stream().map(r -> r.getRoom().getRoomNumber()).toList());
    }

    @Test
    void cancelFreesTheRoom() {
        ReservationEntity booked = service.book("anna", 102, day1, day4, 2);

        service.cancel(booked.getId(), "anna");

        assertEquals(ReservationStatus.CANCELLED, reservations.findById(booked.getId()).orElseThrow().getStatus());
        assertEquals(List.of(102, 201), numbers(service.findFreeRooms(day1, day4, 2)));
        assertThrows(IllegalStateException.class, () -> service.cancel(booked.getId(), "anna"));
    }

    @Test
    void otherUsersCannotCancelOrChangeMyReservation() {
        ReservationEntity booked = service.book("anna", 102, day1, day4, 2);

        assertThrows(NoSuchElementException.class, () -> service.cancel(booked.getId(), "ben"));
        assertThrows(NoSuchElementException.class,
                () -> service.changeDates(booked.getId(), "ben", day4, day4.plusDays(2)));
        assertEquals(ReservationStatus.PENDING, reservations.findById(booked.getId()).orElseThrow().getStatus());
    }

    @Test
    void changeDatesMovesTheReservationOrKeepsTheOldDates() {
        ReservationEntity anna = service.book("anna", 102, day1, day4, 2);
        service.book("ben", 102, day4.plusDays(5), day4.plusDays(8), 1);

        // own period may overlap with the old dates of the same reservation
        service.changeDates(anna.getId(), "anna", day1.plusDays(1), day4.plusDays(1));
        assertEquals(day1.plusDays(1), reservations.findById(anna.getId()).orElseThrow().getCheckIn());

        // overlaps with ben: not possible, old dates stay
        assertThrows(IllegalStateException.class,
                () -> service.changeDates(anna.getId(), "anna", day4.plusDays(4), day4.plusDays(6)));
        ReservationEntity stored = reservations.findById(anna.getId()).orElseThrow();
        assertEquals(day1.plusDays(1), stored.getCheckIn());
        assertEquals(day4.plusDays(1), stored.getCheckOut());
    }

    @Test
    void cancelledReservationCannotBeChanged() {
        ReservationEntity booked = service.book("anna", 102, day1, day4, 2);
        service.cancel(booked.getId(), "anna");

        assertThrows(IllegalStateException.class,
                () -> service.changeDates(booked.getId(), "anna", day4, day4.plusDays(2)));
    }

    @Test
    void receptionSeesAllAndChangesStatus() {
        ReservationEntity anna = service.book("anna", 102, day1, day4, 2);
        service.book("ben", 201, day1, day4, 3);
        assertEquals(2, service.allReservations().size());

        service.changeStatus(anna.getId(), ReservationStatus.CONFIRMED);
        assertEquals(ReservationStatus.CONFIRMED, reservations.findById(anna.getId()).orElseThrow().getStatus());

        // cancelled, then the room is booked by someone else: anna's reservation can't be opened again
        service.changeStatus(anna.getId(), ReservationStatus.CANCELLED);
        service.book("carl", 102, day1, day4, 2);
        assertThrows(IllegalStateException.class, () -> service.changeStatus(anna.getId(), ReservationStatus.CONFIRMED));
    }

    @Test
    void receptionChangesDatesOfAnyReservation() {
        ReservationEntity anna = service.book("anna", 102, day1, day4, 2);

        service.changeDatesByStaff(anna.getId(), day4, day4.plusDays(2));

        ReservationEntity stored = reservations.findById(anna.getId()).orElseThrow();
        assertEquals(day4, stored.getCheckIn());
        assertEquals(day4.plusDays(2), stored.getCheckOut());
    }
}

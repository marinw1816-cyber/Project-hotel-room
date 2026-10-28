package hotel.search;

import hotel.model.ReservationStore;
import hotel.model.Room;

import java.time.LocalDate;
import java.util.List;

public class RoomSearchService {
    private final List<Room> rooms;
    private final ReservationStore store;

    public RoomSearchService(List<Room> rooms, ReservationStore store) {
        if (rooms == null || store == null) {
            throw new IllegalArgumentException("Rooms and store must not be null");
        }
        this.rooms = List.copyOf(rooms);
        this.store = store;
    }

    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, int guests) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Date is required");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }
        if (guests <= 0) {
            throw new IllegalArgumentException("Number of guests must be positive");
        }
        return rooms.stream()
                .filter(r -> r.getCapacity() >= guests)
                .filter(r -> store.isAvailable(r, checkIn, checkOut))
                .toList();
    }
}

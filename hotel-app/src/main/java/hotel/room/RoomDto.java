package hotel.room;

import java.math.BigDecimal;

/** JSON body of the REST API: {"roomNumber": 101, "capacity": 2, "pricePerNight": 80.00} */
public record RoomDto(Integer roomNumber, Integer capacity, BigDecimal pricePerNight) {

    static RoomDto from(RoomEntity room) {
        return new RoomDto(room.getRoomNumber(), room.getCapacity(), room.getPricePerNight());
    }
}

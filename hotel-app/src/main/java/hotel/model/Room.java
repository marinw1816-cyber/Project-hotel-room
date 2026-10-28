package hotel.model;

import java.math.BigDecimal;

public class Room {
    private final int number;
    private final int capacity;
    private final BigDecimal pricePerNight;

    public Room(int number, int capacity, BigDecimal pricePerNight) {
        if (number <= 0) {
            throw new IllegalArgumentException("Room number must be positive");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        if (pricePerNight == null || pricePerNight.signum() < 0) {
            throw new IllegalArgumentException("Price must not be negative");
        }
        this.number = number;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
    }

    public int getNumber() {
        return number;
    }

    public int getCapacity() {
        return capacity;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }
}

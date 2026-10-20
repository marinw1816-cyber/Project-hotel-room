package hotel.model;

import java.math.BigDecimal;

public class Room {
    private final int number;
    private final int capacity;
    private final BigDecimal pricePerNight;

    public Room(int number, int capacity, BigDecimal pricePerNight) {
        if (number <= 0) {
            throw new IllegalArgumentException("Zimmernummer muss positiv sein");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Kapazität muss positiv sein");
        }
        if (pricePerNight == null || pricePerNight.signum() < 0) {
            throw new IllegalArgumentException("Preis darf nicht negativ sein");
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

package hotel.user;

/** The three roles of the hotel application. */
public enum Role {
    ADMIN,        // adds, views, updates and deletes rooms
    CUSTOMER,     // searches rooms and manages own reservations
    RECEPTIONIST  // views and modifies reservations
}

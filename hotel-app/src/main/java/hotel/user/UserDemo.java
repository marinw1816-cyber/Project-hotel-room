package hotel.user;

import java.util.Optional;

public class UserDemo {
    public static void run() {
        System.out.println("=== User Demo ===");
        UserService service = new UserService();

        User anna = service.register("Anna", "secret123", Role.CUSTOMER);
        service.register("Rita", "reception1", Role.RECEPTIONIST);
        System.out.println("Registered: " + anna.getUsername() + " (" + anna.getRole() + ")");
        System.out.println("Password stored only as a hash: "
                + !anna.getPasswordHash().contains("secret123"));

        System.out.println("Login Anna with the right password: "
                + describe(service.login("Anna", "secret123")));
        System.out.println("Login Anna with a wrong password: "
                + describe(service.login("Anna", "wrong-password")));
        System.out.println("Login rita (lower case) with the right password: "
                + describe(service.login("rita", "reception1")));

        try {
            service.register("ANNA", "other-pass1", Role.ADMIN);
        } catch (IllegalStateException e) {
            System.out.println("Second sign-up as ANNA rejected: " + e.getMessage());
        }
    }

    private static String describe(Optional<User> user) {
        return user.map(u -> "success, role " + u.getRole()).orElse("failed");
    }
}

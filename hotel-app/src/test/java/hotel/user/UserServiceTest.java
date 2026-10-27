package hotel.user;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class UserServiceTest {
    @Test
    void registeredUserCanLogIn() {
        UserService service = new UserService();
        service.register("anna", "secret123", Role.CUSTOMER);

        Optional<User> result = service.login("anna", "secret123");

        assertTrue(result.isPresent());
        assertEquals("anna", result.get().getUsername());
    }

    @Test
    void wrongPasswordOrUnknownUserCannotLogIn() {
        UserService service = new UserService();
        service.register("anna", "secret123", Role.CUSTOMER);

        assertTrue(service.login("anna", "wrong-password").isEmpty());
        assertTrue(service.login("nobody", "secret123").isEmpty());
    }

    @Test
    void roleIsKeptAfterRegistration() {
        UserService service = new UserService();
        service.register("admin", "secret123", Role.ADMIN);
        service.register("rita", "secret123", Role.RECEPTIONIST);
        service.register("carl", "secret123", Role.CUSTOMER);

        assertEquals(Role.ADMIN, service.login("admin", "secret123").get().getRole());
        assertEquals(Role.RECEPTIONIST, service.login("rita", "secret123").get().getRole());
        assertEquals(Role.CUSTOMER, service.login("carl", "secret123").get().getRole());
    }

    @Test
    void rejectsDuplicateUsername() {
        UserService service = new UserService();
        service.register("anna", "secret123", Role.CUSTOMER);

        assertThrows(IllegalStateException.class,
                () -> service.register("anna", "other-pass1", Role.ADMIN));
    }

    @Test
    void usernameIsNotCaseSensitive() {
        UserService service = new UserService();
        service.register("Anna", "secret123", Role.CUSTOMER);

        // reported in the review of PR #38: register "Anna", log in as "anna"
        assertTrue(service.login("anna", "secret123").isPresent());
        assertTrue(service.login("ANNA", "secret123").isPresent());
        assertTrue(service.login(" Anna ", "secret123").isPresent());
        assertEquals("anna", service.login("ANNA", "secret123").get().getUsername());
        // the same name in another spelling is a duplicate
        assertThrows(IllegalStateException.class,
                () -> service.register("ANNA", "other-pass1", Role.ADMIN));
    }

    @Test
    void rejectsInvalidInput() {
        UserService service = new UserService();

        assertThrows(IllegalArgumentException.class,
                () -> service.register("", "secret123", Role.CUSTOMER));
        assertThrows(IllegalArgumentException.class,
                () -> service.register("anna", "short", Role.CUSTOMER));
        assertThrows(IllegalArgumentException.class,
                () -> service.register("anna", "secret123", null));
    }

    @Test
    void passwordIsNeverStoredAsPlainText() {
        UserService service = new UserService();
        User user = service.register("anna", "secret123", Role.CUSTOMER);

        assertNotEquals("secret123", user.getPasswordHash());
        assertFalse(user.getPasswordHash().contains("secret123"));
    }
}

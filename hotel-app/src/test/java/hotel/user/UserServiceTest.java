package hotel.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hotel.app.HotelApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Sign-up with the H2 test database. */
@SpringBootTest(classes = HotelApplication.class)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository users;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        users.deleteAll();
    }

    @Test
    void signUpCreatesCustomerAndStoresNoPlainPassword() {
        userService.signUp("anna", "secret123");

        UserEntity stored = users.findByUsername("anna").orElseThrow();
        assertEquals(Role.CUSTOMER, stored.getRole());
        assertFalse(stored.getPasswordHash().contains("secret123"));
        // the hash still belongs to the password
        assertTrue(passwordEncoder.matches("secret123", stored.getPasswordHash()));
    }

    @Test
    void duplicateUsernameIsRejected() {
        userService.signUp("anna", "secret123");

        assertThrows(IllegalStateException.class, () -> userService.signUp("anna", "other-pass1"));
        assertEquals(1, users.count());
    }
}

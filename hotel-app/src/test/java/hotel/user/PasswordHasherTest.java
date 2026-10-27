package hotel.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {
    @Test
    void hashMatchesOnlyTheRightPassword() {
        String hash = PasswordHasher.hash("secret123");

        assertTrue(PasswordHasher.matches("secret123", hash));
        assertFalse(PasswordHasher.matches("secret124", hash));
    }

    @Test
    void samePasswordGivesDifferentHashesBecauseOfSalt() {
        String first = PasswordHasher.hash("secret123");
        String second = PasswordHasher.hash("secret123");

        assertNotEquals(first, second);
        assertTrue(PasswordHasher.matches("secret123", first));
        assertTrue(PasswordHasher.matches("secret123", second));
    }

    @Test
    void brokenOrMissingHashNeverMatches() {
        assertFalse(PasswordHasher.matches("secret123", "no-colon-here"));
        assertFalse(PasswordHasher.matches("secret123", null));
        assertFalse(PasswordHasher.matches(null, PasswordHasher.hash("secret123")));
    }

    @Test
    void damagedHashNeverThrowsAndNeverMatches() {
        // one ":" but no valid Base64 (reported in the review of PR #38)
        assertFalse(PasswordHasher.matches("secret123", "!!:!!"));
        // empty salt
        assertFalse(PasswordHasher.matches("secret123", ":AAAA"));
        // empty parts
        assertFalse(PasswordHasher.matches("secret123", ":"));
    }

    @Test
    void rejectsEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(""));
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(null));
    }
}

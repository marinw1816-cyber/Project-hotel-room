package hotel.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Sign-up and sign-in. Users are kept in memory, like the reservations. */
public class UserService {
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final Map<String, User> users = new HashMap<>();

    /** Sign-up: creates a user with the given role. Only the hash of the password is stored. */
    public User register(String username, String password, Role role) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be empty");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must have at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role is missing");
        }
        String name = username.trim();
        if (users.containsKey(name)) {
            throw new IllegalStateException("Username is already taken");
        }
        User user = new User(name, PasswordHasher.hash(password), role);
        users.put(name, user);
        return user;
    }

    /** Sign-in: returns the user if name and password are right, otherwise empty. */
    public Optional<User> login(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        User user = users.get(username.trim());
        if (user == null || !PasswordHasher.matches(password, user.getPasswordHash())) {
            return Optional.empty();
        }
        return Optional.of(user);
    }
}

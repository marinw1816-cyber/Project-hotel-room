package hotel.user;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Sign-up and the user lookup for the login.
 * Spring Security calls loadUserByUsername() and checks the password itself.
 */
@Service
public class UserService implements UserDetailsService {
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    /** Sign-up from the web form: always a CUSTOMER, nobody can choose a role. */
    public UserEntity signUp(String username, String password) {
        return create(username, password, Role.CUSTOMER);
    }

    /** Start accounts (ADMIN, RECEPTIONIST): only created if the name does not exist yet. */
    public void createIfMissing(String username, String password, Role role) {
        if (!users.existsByUsername(username)) {
            create(username, password, role);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserEntity user = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));
        // roles("ADMIN") becomes the authority "ROLE_ADMIN", which hasRole("ADMIN") checks
        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
    }

    private UserEntity create(String username, String password, Role role) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be empty");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must have at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (users.existsByUsername(username)) {
            throw new IllegalStateException("Username is already taken");
        }
        // BCrypt adds a random salt, so the same password gives a different hash each time
        return users.save(new UserEntity(username, passwordEncoder.encode(password), role));
    }
}

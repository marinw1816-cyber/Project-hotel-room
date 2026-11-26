package hotel.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Runs once at start: creates the ADMIN and RECEPTIONIST account from environment variables.
 * Without values (tests, CI) nothing happens.
 */
@Component
public class StartAccounts implements ApplicationRunner {

    private final UserService userService;

    // ":" with nothing after it: empty text if the variable is not set
    @Value("${APP_ADMIN_USERNAME:}")
    private String adminUsername;
    @Value("${APP_ADMIN_PASSWORD:}")
    private String adminPassword;
    @Value("${APP_RECEPTION_USERNAME:}")
    private String receptionUsername;
    @Value("${APP_RECEPTION_PASSWORD:}")
    private String receptionPassword;

    public StartAccounts(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        createIfSet(adminUsername, adminPassword, Role.ADMIN);
        createIfSet(receptionUsername, receptionPassword, Role.RECEPTIONIST);
    }

    private void createIfSet(String username, String password, Role role) {
        if (!username.isBlank() && !password.isBlank()) {
            userService.createIfMissing(username, password, role);
        }
    }
}

package zechs.zplex.auth;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import zechs.zplex.auth.service.UserService;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class UserSetup {

    private static final Logger logger = Logger.getLogger(UserSetup.class.getName());
    private final UserService userService;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    public UserSetup(UserService userService) {
        this.userService = userService;
    }

    @PostConstruct
    public void setup() {
        try {
            logger.log(Level.INFO, "Running user setup...");
            userService.createAdminUser(adminPassword);
            logger.log(Level.INFO, "User setup completed successfully.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed during user setup: " + e.getMessage(), e);
        }
    }
}

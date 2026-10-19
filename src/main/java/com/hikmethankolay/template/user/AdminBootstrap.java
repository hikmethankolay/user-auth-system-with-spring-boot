package com.hikmethankolay.template.user;

import com.hikmethankolay.template.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Creates the administrator configured in {@code app.admin.*} on startup, if username, email and
 * password are all set and no admin exists yet. This solves the "who creates the first admin"
 * problem without manual SQL.
 *
 * <p>If the configured username or email already belongs to a regular user, registration fails
 * with a conflict and the application does not start.</p>
 */
@Component
class AdminBootstrap implements ApplicationRunner {

    /** Logger for the bootstrap result. */
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    /** Creates the admin account. */
    private final UserService userService;

    /** Configured admin credentials. */
    private final AppProperties.Admin admin;

    /**
     * Creates the runner.
     *
     * @param userService creates the admin account
     * @param properties  application properties holding the admin credentials
     */
    AdminBootstrap(UserService userService, AppProperties properties) {
        this.userService = userService;
        this.admin = properties.admin();
    }

    /**
     * Creates the admin when it is configured and no admin exists.
     *
     * @param args application arguments (unused)
     */
    @Override
    public void run(ApplicationArguments args) {
        if (!admin.isConfigured() || userService.adminExists()) {
            return;
        }
        userService.register(admin.username(), admin.email(), admin.password(), Role.ADMIN);
        log.info("Created initial admin user '{}'", admin.username());
    }
}

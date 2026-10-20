package com.hikmethankolay.template.user;

import com.hikmethankolay.template.common.ApiException;
import com.hikmethankolay.template.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * Creates the owner account configured in {@code app.admin.*} on startup, if username, email and
 * password are all set and no owner exists yet. This solves the "who creates the first admin"
 * problem without manual SQL. The owner has every administrator right and is the only one who can
 * change roles and manage administrators (see {@link Role#OWNER}).
 *
 * <p>If the configured username or email already belongs to a regular user, registration fails
 * with a conflict and the application does not start.</p>
 */
@Component
class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserService userService;

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
        if (!admin.isConfigured() || userService.ownerExists()) {
            return;
        }
        // The configured admin address is trusted, so no verification email is sent.
        try {
            userService.register(admin.username(), admin.email(), admin.password(), Role.OWNER, true);
            log.info("Created the owner account '{}'", admin.username());
        } catch (ApiException | DataIntegrityViolationException ex) {
            // Several instances starting at once all try; only a conflict with another account is an error.
            if (!userService.ownerExists()) {
                throw ex;
            }
            log.info("Another instance created the owner account");
        }
    }
}

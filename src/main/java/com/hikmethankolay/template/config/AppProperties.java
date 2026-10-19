package com.hikmethankolay.template.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * Type-safe view of the {@code app.*} properties defined in {@code application.yml}.
 *
 * <p>The properties are validated at startup, so a misconfigured environment
 * (for example a missing {@code JWT_SECRET}) fails fast with a clear message.</p>
 *
 * @param jwt    token signing and lifetime settings
 * @param cookie settings of the HttpOnly authentication cookie
 * @param cors   cross-origin settings for browser clients
 * @param login     brute-force protection settings
 * @param rateLimit per-IP request rate limit
 * @param admin     optional initial administrator account
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @Valid @NotNull Jwt jwt,
        @Valid @NotNull Cookie cookie,
        @Valid @NotNull Cors cors,
        @Valid @NotNull Login login,
        @Valid @NotNull RateLimit rateLimit,
        @Valid @NotNull Admin admin) {

    /**
     * JWT settings.
     *
     * @param secret         HMAC-SHA256 signing secret, at least 32 characters
     * @param issuer         value of the {@code iss} claim; tokens from other issuers are rejected
     * @param accessTokenTtl lifetime of a regular token
     * @param rememberMeTtl  lifetime of a token issued with "remember me"
     */
    public record Jwt(
            @NotBlank(message = "must be set (JWT_SECRET)")
            @Size(min = 32, message = "must be at least 32 characters (JWT_SECRET)")
            String secret,
            @NotBlank String issuer,
            @NotNull Duration accessTokenTtl,
            @NotNull Duration rememberMeTtl) {
    }

    /**
     * Authentication cookie settings.
     *
     * @param name     cookie name
     * @param secure   whether the cookie is sent over HTTPS only; enable in production
     * @param sameSite {@code SameSite} attribute; {@code Strict} protects against CSRF
     */
    public record Cookie(@NotBlank String name, boolean secure, @NotBlank String sameSite) {
    }

    /**
     * CORS settings.
     *
     * @param allowedOrigins origins allowed to call the API with credentials; never use {@code *}
     */
    public record Cors(List<String> allowedOrigins) {
    }

    /**
     * Brute-force protection settings.
     *
     * @param maxAttempts  failed logins after which an IP or account is blocked
     * @param lockDuration how long a block lasts after the last failure
     */
    public record Login(@Positive int maxAttempts, @NotNull Duration lockDuration) {
    }

    /**
     * Per-client-IP rate limit for all API requests (Actuator is exempt).
     *
     * @param enabled           whether requests are limited
     * @param requestsPerMinute requests allowed per IP in each one-minute window
     */
    public record RateLimit(boolean enabled, @Positive int requestsPerMinute) {
    }

    /**
     * Initial administrator, created on startup when all three fields are set and no admin exists yet.
     *
     * @param username admin username
     * @param email    admin email address
     * @param password admin password in plain text; change it after the first login
     */
    public record Admin(String username, String email, String password) {

        /**
         * Tells whether all admin fields are set.
         *
         * @return {@code true} if username, email and password are all non-blank
         */
        public boolean isConfigured() {
            return StringUtils.hasText(username) && StringUtils.hasText(email) && StringUtils.hasText(password);
        }
    }
}

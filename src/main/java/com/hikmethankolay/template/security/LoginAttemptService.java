package com.hikmethankolay.template.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hikmethankolay.template.config.AppProperties;
import java.time.Duration;
import org.springframework.stereotype.Service;

/**
 * Brute-force protection for the login endpoint.
 *
 * <p>Failed logins are counted per key (client IP and account identifier). A key is blocked once
 * it reaches {@code app.login.max-attempts} and unblocked after {@code app.login.lock-duration}
 * without new failures.</p>
 *
 * <p>State is kept in memory, so limits apply per application instance. Use a shared store
 * such as Redis when running several instances.</p>
 */
@Service
public class LoginAttemptService {

    /** Failures after which a key is blocked. */
    private final int maxAttempts;

    /** How long a block lasts after the last failure. */
    private final Duration lockDuration;

    /** Failure counters by key; entries expire {@link #lockDuration} after the last failure. */
    private final Cache<String, Integer> failures;

    /**
     * Creates the service.
     *
     * @param properties application properties holding the limits
     */
    public LoginAttemptService(AppProperties properties) {
        this.maxAttempts = properties.login().maxAttempts();
        this.lockDuration = properties.login().lockDuration();
        this.failures = Caffeine.newBuilder()
                .expireAfterWrite(lockDuration)
                .maximumSize(100_000)
                .build();
    }

    /**
     * Tells whether a key is currently blocked.
     *
     * @param key an IP or account key
     * @return {@code true} if the key reached the maximum number of failures
     */
    public boolean isBlocked(String key) {
        Integer count = failures.getIfPresent(key);
        return count != null && count >= maxAttempts;
    }

    /**
     * Records one failed login for each key.
     *
     * @param keys the IP and account keys of the attempt
     */
    public void recordFailure(String... keys) {
        for (String key : keys) {
            failures.asMap().merge(key, 1, Integer::sum);
        }
    }

    /**
     * Clears the failures of a key, e.g. after a successful login.
     *
     * @param key the key to reset
     */
    public void reset(String key) {
        failures.invalidate(key);
    }

    /**
     * Returns the full lock duration. It is sent as {@code Retry-After}, so it is an upper bound
     * on the remaining wait (the lock may expire sooner if the last failure was a while ago).
     *
     * @return the configured lock duration
     */
    public Duration lockDuration() {
        return lockDuration;
    }
}

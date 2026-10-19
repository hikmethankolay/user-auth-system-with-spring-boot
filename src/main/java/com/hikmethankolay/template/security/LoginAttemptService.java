package com.hikmethankolay.template.security;

import com.hikmethankolay.template.config.AppProperties;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

/**
 * Brute-force protection for the login endpoint.
 *
 * <p>Failed logins are counted per key (client IP and account identifier) in Redis, so the limits
 * hold across all application instances. A key is blocked once it reaches
 * {@code app.login.max-attempts} and unblocked after {@code app.login.lock-duration} without new
 * failures.</p>
 *
 * <p>If Redis is unavailable, logins are allowed (fail open) and a warning is logged, so an outage
 * does not lock every user out.</p>
 */
@Service
public class LoginAttemptService {

    /** Logger for Redis failures. */
    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    /** Atomically increments a counter and (re)sets its expiry, returning the new count. */
    private static final RedisScript<Long> RECORD_FAILURE = RedisScript.of(
            "local count = redis.call('INCR', KEYS[1]) redis.call('PEXPIRE', KEYS[1], ARGV[1]) return count",
            Long.class);

    /** Redis access with string keys and values. */
    private final StringRedisTemplate redis;

    /** Key prefix, namespaced by application name. */
    private final String keyPrefix;

    /** Failures after which a key is blocked. */
    private final int maxAttempts;

    /** How long a block lasts after the last failure. */
    private final Duration lockDuration;

    /**
     * Creates the service.
     *
     * @param redis           Redis access
     * @param properties      application properties holding the limits
     * @param applicationName value of {@code spring.application.name}, used to namespace keys
     */
    public LoginAttemptService(
            StringRedisTemplate redis, AppProperties properties, @Value("${spring.application.name}") String applicationName) {
        this.redis = redis;
        this.keyPrefix = applicationName + ":login-failures:";
        this.maxAttempts = properties.login().maxAttempts();
        this.lockDuration = properties.login().lockDuration();
    }

    /**
     * Tells whether a key is currently blocked.
     *
     * @param key an IP or account key
     * @return {@code true} if the key reached the maximum number of failures
     */
    public boolean isBlocked(String key) {
        try {
            String count = redis.opsForValue().get(keyPrefix + key);
            return count != null && Long.parseLong(count) >= maxAttempts;
        } catch (DataAccessException ex) {
            log.warn("Could not read login failures for {}: {}", key, ex.getMessage());
            return false;
        }
    }

    /**
     * Records one failed login for each key.
     *
     * @param keys the IP and account keys of the attempt
     */
    public void recordFailure(String... keys) {
        for (String key : keys) {
            try {
                redis.execute(RECORD_FAILURE, List.of(keyPrefix + key), Long.toString(lockDuration.toMillis()));
            } catch (DataAccessException ex) {
                log.warn("Could not record login failure for {}: {}", key, ex.getMessage());
            }
        }
    }

    /**
     * Clears the failures of a key, e.g. after a successful login.
     *
     * @param key the key to reset
     */
    public void reset(String key) {
        try {
            redis.delete(keyPrefix + key);
        } catch (DataAccessException ex) {
            log.warn("Could not reset login failures for {}: {}", key, ex.getMessage());
        }
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

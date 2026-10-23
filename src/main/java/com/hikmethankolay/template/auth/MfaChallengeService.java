package com.hikmethankolay.template.auth;

import com.hikmethankolay.template.common.ApiException;
import com.hikmethankolay.template.common.ProblemType;
import com.hikmethankolay.template.common.SecureTokens;
import com.hikmethankolay.template.config.AppProperties;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Remembers, between the password and the second factor, who is logging in. The client gets a random token;
 * Redis keeps only its hash, for {@code app.mfa.challenge-ttl}.
 *
 * <p>Like the failed-login counters, this fails closed: without Redis, users with two-factor authentication
 * cannot log in (503), rather than skipping the second factor.</p>
 */
@Service
public class MfaChallengeService {

    private static final Logger log = LoggerFactory.getLogger(MfaChallengeService.class);

    private final StringRedisTemplate redis;

    private final String keyPrefix;

    private final Duration ttl;

    /**
     * Creates the service.
     *
     * @param redis           stores the challenges
     * @param properties      application properties holding the challenge lifetime
     * @param applicationName prefix of the Redis keys
     */
    public MfaChallengeService(
            StringRedisTemplate redis,
            AppProperties properties,
            @Value("${spring.application.name}") String applicationName) {
        this.redis = redis;
        this.keyPrefix = applicationName + ":mfa-challenge:";
        this.ttl = properties.mfa().challengeTtl();
    }

    /**
     * Starts a challenge.
     *
     * @param challenge who is logging in
     * @return the token the client sends back with the code
     * @throws ApiException 503 if Redis is unavailable
     */
    public String create(Challenge challenge) {
        String token = SecureTokens.generate();
        String value = challenge.userId() + ":" + challenge.sessionVersion() + ":" + challenge.rememberMe();
        try {
            redis.opsForValue().set(key(token), value, ttl);
        } catch (DataAccessException ex) {
            throw unavailable(ex);
        }
        return token;
    }

    /**
     * Looks a challenge up without using it up, so a mistyped code can be retried.
     *
     * @param token the token from the login response
     * @return the challenge, or empty if the token is unknown, used or expired
     * @throws ApiException 503 if Redis is unavailable
     */
    public Optional<Challenge> find(String token) {
        String value;
        try {
            value = redis.opsForValue().get(key(token));
        } catch (DataAccessException ex) {
            throw unavailable(ex);
        }
        if (value == null) {
            return Optional.empty();
        }
        String[] parts = value.split(":");
        return Optional.of(
                new Challenge(Long.parseLong(parts[0]), Integer.parseInt(parts[1]), Boolean.parseBoolean(parts[2])));
    }

    /**
     * Uses a challenge up. Only one caller can: of two requests that both passed the code check, the second
     * gets {@code false}.
     *
     * @param token the token from the login response
     * @return {@code true} if this call removed the challenge
     * @throws ApiException 503 if Redis is unavailable
     */
    public boolean consume(String token) {
        try {
            return Boolean.TRUE.equals(redis.delete(key(token)));
        } catch (DataAccessException ex) {
            throw unavailable(ex);
        }
    }

    /**
     * Returns how long a challenge lives.
     *
     * @return the lifetime
     */
    public Duration ttl() {
        return ttl;
    }

    private String key(String token) {
        return keyPrefix + SecureTokens.hash(token);
    }

    private static ApiException unavailable(DataAccessException ex) {
        log.warn("Could not access the MFA challenges, refusing the login: {}", ex.getMessage());
        return new ApiException(
                HttpStatus.SERVICE_UNAVAILABLE,
                ProblemType.LOGIN_UNAVAILABLE,
                "Login is temporarily unavailable. Please try again later.");
    }

    /**
     * A login waiting for its second factor.
     *
     * @param userId         the user
     * @param sessionVersion the user's session version when the password was checked
     * @param rememberMe     whether the session should be remembered
     */
    public record Challenge(long userId, int sessionVersion, boolean rememberMe) {}
}

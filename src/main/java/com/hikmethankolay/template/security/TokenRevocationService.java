package com.hikmethankolay.template.security;

import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Denylist of revoked JWTs, kept in Redis so revocation works across all instances.
 *
 * <p>Each revoked token id ({@code jti}) is stored only until the token would have expired anyway,
 * so the list never grows beyond the tokens that are still alive. The JWT decoder consults it on
 * every request (see {@link SecurityConfig}).</p>
 *
 * <p>If Redis is unavailable, revocation and the check fail open (tokens stay valid) and a warning
 * is logged: an outage briefly disables revocation rather than locking every user out.</p>
 */
@Service
public class TokenRevocationService {

    /** Logger for Redis failures. */
    private static final Logger log = LoggerFactory.getLogger(TokenRevocationService.class);

    /** Redis access with string keys and values. */
    private final StringRedisTemplate redis;

    /** Key prefix, namespaced by application name. */
    private final String keyPrefix;

    /**
     * Creates the service.
     *
     * @param redis           Redis access
     * @param applicationName value of {@code spring.application.name}, used to namespace keys
     */
    public TokenRevocationService(StringRedisTemplate redis, @Value("${spring.application.name}") String applicationName) {
        this.redis = redis;
        this.keyPrefix = applicationName + ":revoked-token:";
    }

    /**
     * Revokes a token until its expiry. Tokens without an id or already expired are ignored.
     *
     * @param jwt the validated token to revoke
     */
    public void revoke(Jwt jwt) {
        String tokenId = jwt.getId();
        Instant expiresAt = jwt.getExpiresAt();
        if (tokenId == null || expiresAt == null) {
            return;
        }
        Duration remaining = Duration.between(Instant.now(), expiresAt);
        if (remaining.isNegative() || remaining.isZero()) {
            return;
        }
        try {
            redis.opsForValue().set(keyPrefix + tokenId, "1", remaining);
        } catch (DataAccessException ex) {
            log.warn("Could not revoke token {}: {}", tokenId, ex.getMessage());
        }
    }

    /**
     * Tells whether a token id has been revoked.
     *
     * @param tokenId the token's {@code jti} claim, may be {@code null}
     * @return {@code true} if the token is on the denylist
     */
    public boolean isRevoked(String tokenId) {
        if (tokenId == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redis.hasKey(keyPrefix + tokenId));
        } catch (DataAccessException ex) {
            log.warn("Could not check token revocation for {}: {}", tokenId, ex.getMessage());
            return false;
        }
    }
}

package com.hikmethankolay.template.security;

import com.hikmethankolay.template.config.AppProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
 * <p>Two kinds of entries exist, both kept only until the affected tokens would have expired
 * anyway, so the list never grows beyond the tokens that are still alive:</p>
 * <ul>
 *   <li>a single token id ({@code jti}), revoked on logout;</li>
 *   <li>a per-user cut-off time: every token of that user issued before it is rejected. Set when
 *       all sessions of a user end, e.g. after a password change.</li>
 * </ul>
 *
 * <p>The JWT decoder consults both on every request, in one Redis round trip (see {@link SecurityConfig}).</p>
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

    /** Key prefix of revoked token ids, namespaced by application name. */
    private final String keyPrefix;

    /** Key prefix of per-user cut-off times, namespaced by application name. */
    private final String userKeyPrefix;

    /** Lifetime of access tokens: how long a per-user cut-off must be kept. */
    private final Duration accessTokenTtl;

    /**
     * Creates the service.
     *
     * @param redis           Redis access
     * @param applicationName value of {@code spring.application.name}, used to namespace keys
     * @param properties      application properties holding the access token lifetime
     */
    public TokenRevocationService(
            StringRedisTemplate redis, @Value("${spring.application.name}") String applicationName, AppProperties properties) {
        this.redis = redis;
        this.keyPrefix = applicationName + ":revoked-token:";
        this.userKeyPrefix = applicationName + ":tokens-revoked-before:";
        this.accessTokenTtl = properties.jwt().accessTokenTtl();
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
     * Revokes every access token of a user issued so far. Tokens issued afterwards stay valid
     * (see {@link #issuedNotBefore(long)}).
     *
     * <p>Token issue times have whole-second precision, so the cut-off is the start of the next
     * second: tokens issued in the current second are revoked too.</p>
     *
     * @param userId the user whose tokens are revoked
     */
    public void revokeAllIssuedBefore(long userId) {
        long cutOff = Instant.now().getEpochSecond() + 1;
        try {
            // Kept one access token lifetime: by then every token issued before the cut-off has expired.
            redis.opsForValue().set(userKeyPrefix + userId, Long.toString(cutOff), accessTokenTtl.plusSeconds(1));
        } catch (DataAccessException ex) {
            log.warn("Could not revoke the tokens of user {}: {}", userId, ex.getMessage());
        }
    }

    /**
     * Returns the earliest issue time a new token of the user may have, so it is not caught by a
     * cut-off set in the current second.
     *
     * @param userId the user a token is issued for
     * @return the active cut-off of the user, or empty if there is none (or Redis is unavailable)
     */
    public Optional<Instant> issuedNotBefore(long userId) {
        try {
            String cutOff = redis.opsForValue().get(userKeyPrefix + userId);
            return Optional.ofNullable(cutOff).map(value -> Instant.ofEpochSecond(Long.parseLong(value)));
        } catch (DataAccessException ex) {
            log.warn("Could not read the token cut-off of user {}: {}", userId, ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Tells whether a token has been revoked, individually or by a cut-off of its user.
     *
     * @param jwt the token, already verified otherwise
     * @return {@code true} if the token must be rejected
     */
    public boolean isRevoked(Jwt jwt) {
        String tokenId = jwt.getId();
        String userId = jwt.getSubject();
        if (tokenId == null || userId == null) {
            return false;
        }
        try {
            List<String> entries = redis.opsForValue().multiGet(List.of(keyPrefix + tokenId, userKeyPrefix + userId));
            if (entries == null) {
                return false;
            }
            if (entries.get(0) != null) {
                return true;
            }
            Instant issuedAt = jwt.getIssuedAt();
            return entries.get(1) != null && issuedAt != null && issuedAt.getEpochSecond() < Long.parseLong(entries.get(1));
        } catch (DataAccessException ex) {
            log.warn("Could not check token revocation for {}: {}", tokenId, ex.getMessage());
            return false;
        }
    }
}

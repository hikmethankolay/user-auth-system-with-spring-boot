package com.hikmethankolay.template.auth;

import com.hikmethankolay.template.audit.AuditEvent;
import com.hikmethankolay.template.audit.AuditLog;
import com.hikmethankolay.template.common.SecureTokens;
import com.hikmethankolay.template.config.AppProperties;
import com.hikmethankolay.template.user.User;
import com.hikmethankolay.template.user.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issues, rotates and revokes refresh tokens.
 *
 * <p>A refresh token is an opaque 256-bit random value; only its SHA-256 hash is stored. Each one
 * can be exchanged exactly once: the exchange marks it used and issues a successor in the same
 * family. Presenting a used token again means it was copied, so the whole family (the session) is
 * revoked and the event is audited; whoever holds the newest token must log in again.</p>
 *
 * <p>Expiry is sliding: each exchange starts a new {@code app.jwt.refresh-token-ttl} (or
 * {@code app.jwt.remember-me-ttl}) period, so a session ends after that much inactivity.</p>
 *
 * <p>Each token also carries the user's session version. Ending a user's sessions bumps it, so a
 * token issued under the old version, even by a login or exchange that raced the change, is rejected.</p>
 */
@Service
public class RefreshTokenService {

    /** Logger for token cleanup. */
    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    /** Error for unknown, used, revoked or expired tokens; deliberately the same for all. */
    static final String INVALID_REFRESH_TOKEN = "The refresh token is missing, invalid or expired.";

    /** Token persistence. */
    private final RefreshTokenRepository tokens;

    /** Token lifetimes. */
    private final AppProperties.Jwt properties;

    /** Records token reuse. */
    private final AuditLog audit;

    /** Reads the users' current session version. */
    private final UserRepository users;

    /**
     * Creates the service.
     *
     * @param tokens     token persistence
     * @param properties application properties holding the lifetimes
     * @param audit      records token reuse
     * @param users      reads the users' session version
     */
    public RefreshTokenService(RefreshTokenRepository tokens, AppProperties properties, AuditLog audit, UserRepository users) {
        this.tokens = tokens;
        this.properties = properties.jwt();
        this.audit = audit;
        this.users = users;
    }

    /**
     * Starts a new session.
     *
     * @param userId         owner of the session
     * @param sessionVersion the user's session version when the password was checked
     * @param rememberMe     whether to use the longer remember-me lifetime
     * @return the new refresh token
     * @throws BadCredentialsException if the user's sessions ended since the password was checked (mapped to 401)
     */
    @Transactional
    public IssuedRefreshToken issue(long userId, int sessionVersion, boolean rememberMe) {
        if (!isCurrent(userId, sessionVersion)) {
            throw new BadCredentialsException(INVALID_REFRESH_TOKEN);
        }
        return issue(userId, UUID.randomUUID(), sessionVersion, rememberMe);
    }

    /**
     * Exchanges a refresh token for a new one in the same session.
     *
     * <p>Failures do not roll back, so revoking a family on reuse is committed even though the
     * request is rejected.</p>
     *
     * @param rawToken the token presented by the client
     * @return the user the session belongs to and the successor token
     * @throws BadCredentialsException if the token is unknown, expired, already used or from ended
     *                                 sessions (mapped to 401)
     */
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public Rotation rotate(String rawToken) {
        RefreshToken current = tokens.findByTokenHash(SecureTokens.hash(rawToken))
                .orElseThrow(() -> new BadCredentialsException(INVALID_REFRESH_TOKEN));
        Instant now = Instant.now();
        if (current.isUsed()) {
            tokens.deleteAllByFamilyId(current.getFamilyId());
            audit.record(AuditEvent.REFRESH_TOKEN_REUSED, current.getUserId(), "sessionId", current.getFamilyId());
            throw new BadCredentialsException(INVALID_REFRESH_TOKEN);
        }
        if (current.isExpired(now)) {
            throw new BadCredentialsException(INVALID_REFRESH_TOKEN);
        }
        if (!isCurrent(current.getUserId(), current.getSessionVersion())) {
            tokens.deleteAllByFamilyId(current.getFamilyId());
            throw new BadCredentialsException(INVALID_REFRESH_TOKEN);
        }
        current.markUsed(now);
        return new Rotation(current.getUserId(),
                issue(current.getUserId(), current.getFamilyId(), current.getSessionVersion(), current.isRememberMe()));
    }

    /**
     * Ends the session a refresh token belongs to. Unknown tokens are ignored.
     *
     * @param rawToken the token presented by the client
     */
    @Transactional
    public void revokeSession(String rawToken) {
        tokens.findByTokenHash(SecureTokens.hash(rawToken))
                .ifPresent(token -> tokens.deleteAllByFamilyId(token.getFamilyId()));
    }

    /**
     * Ends every session of a user.
     *
     * @param userId owner of the sessions
     * @return the number of deleted tokens
     */
    @Transactional
    public int revokeAll(long userId) {
        return tokens.deleteAllByUserId(userId);
    }

    /**
     * Lists the active sessions of a user.
     *
     * @param userId owner of the sessions
     * @return one entry per session, newest first
     */
    @Transactional(readOnly = true)
    public List<Session> activeSessions(long userId) {
        return tokens.findActiveByUserId(userId, Instant.now()).stream()
                .map(token -> new Session(token.getFamilyId(), token.isRememberMe(), token.getCreatedAt(), token.getExpiresAt()))
                .toList();
    }

    /** Deletes expired tokens once an hour. Safe to run on several instances at once. */
    @Scheduled(cron = "0 30 * * * *")
    @Transactional
    public void deleteExpiredTokens() {
        int deleted = tokens.deleteAllExpiredBefore(Instant.now());
        if (deleted > 0) {
            log.info("Deleted {} expired refresh tokens", deleted);
        }
    }

    /**
     * Tells whether sessions issued under a session version are still valid.
     *
     * @param userId         owner of the sessions
     * @param sessionVersion the version they were issued under
     * @return {@code false} if the user is gone or their sessions ended since
     */
    private boolean isCurrent(long userId, int sessionVersion) {
        return users.findById(userId).map(User::getSessionVersion).filter(version -> version == sessionVersion).isPresent();
    }

    /**
     * Creates and stores a token.
     *
     * @param userId         owner of the token
     * @param familyId       session the token belongs to
     * @param sessionVersion the user's session version when the session started
     * @param rememberMe     whether the session was started with "remember me"
     * @return the raw token with its lifetime
     */
    private IssuedRefreshToken issue(long userId, UUID familyId, int sessionVersion, boolean rememberMe) {
        Duration ttl = rememberMe ? properties.rememberMeTtl() : properties.refreshTokenTtl();
        String token = SecureTokens.generate();
        tokens.save(new RefreshToken(userId, familyId, sessionVersion, SecureTokens.hash(token), rememberMe, Instant.now().plus(ttl)));
        return new IssuedRefreshToken(token, ttl, rememberMe);
    }

    /**
     * A freshly issued refresh token.
     *
     * @param value      the opaque token to give to the client
     * @param ttl        how long it is valid
     * @param rememberMe whether the session was started with "remember me"
     */
    public record IssuedRefreshToken(String value, Duration ttl, boolean rememberMe) {
    }

    /**
     * Result of a successful exchange.
     *
     * @param userId    owner of the session
     * @param successor the token that replaces the exchanged one
     */
    public record Rotation(long userId, IssuedRefreshToken successor) {
    }

    /**
     * An active session, as listed in the personal data export.
     *
     * @param id         the session (family) id
     * @param rememberMe whether it was started with "remember me"
     * @param lastUsedAt when its current refresh token was issued (the login or the last refresh)
     * @param expiresAt  when it ends unless refreshed
     */
    public record Session(UUID id, boolean rememberMe, Instant lastUsedAt, Instant expiresAt) {
    }
}

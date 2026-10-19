package com.hikmethankolay.template.auth;

import com.hikmethankolay.template.audit.AuditEvent;
import com.hikmethankolay.template.audit.AuditLog;
import com.hikmethankolay.template.security.TokenRevocationService;
import com.hikmethankolay.template.user.SessionsInvalidated;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Ends every session of a user on {@link SessionsInvalidated}, so a stolen token stops working as
 * soon as the password changes.
 */
@Component
public class SessionInvalidationListener {

    /** Deletes refresh tokens. */
    private final RefreshTokenService refreshTokens;

    /** Rejects access tokens issued before the change. */
    private final TokenRevocationService revocations;

    /** Records the revocation. */
    private final AuditLog audit;

    /**
     * Creates the listener.
     *
     * @param refreshTokens deletes refresh tokens
     * @param revocations   rejects older access tokens
     * @param audit         records the revocation
     */
    public SessionInvalidationListener(
            RefreshTokenService refreshTokens, TokenRevocationService revocations, AuditLog audit) {
        this.refreshTokens = refreshTokens;
        this.revocations = revocations;
        this.audit = audit;
    }

    /**
     * Deletes the user's refresh tokens inside the transaction that changed the user, so both
     * changes commit or roll back together.
     *
     * @param event identifies the user
     */
    @EventListener
    public void revokeRefreshTokens(SessionsInvalidated event) {
        int revoked = refreshTokens.revokeAll(event.userId());
        audit.record(AuditEvent.SESSIONS_REVOKED, event.userId(), "refreshTokens", revoked);
    }

    /**
     * Rejects the user's existing access tokens once the change has committed.
     *
     * @param event identifies the user
     */
    @TransactionalEventListener
    public void revokeAccessTokens(SessionsInvalidated event) {
        revocations.revokeAllIssuedBefore(event.userId());
    }
}

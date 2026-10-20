package com.hikmethankolay.template.auth;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hikmethankolay.template.audit.AuditEvent;
import com.hikmethankolay.template.audit.AuditLog;
import com.hikmethankolay.template.security.TokenRevocationService;
import com.hikmethankolay.template.user.SessionsInvalidated;
import org.junit.jupiter.api.Test;

/** Unit tests of {@link SessionInvalidationListener}. */
class SessionInvalidationListenerTest {

    private final RefreshTokenService refreshTokens = mock(RefreshTokenService.class);
    private final TokenRevocationService revocations = mock(TokenRevocationService.class);
    private final AuditLog audit = mock(AuditLog.class);
    private final SessionInvalidationListener listener =
            new SessionInvalidationListener(refreshTokens, revocations, audit);

    /** Ending the refresh tokens records how many sessions were ended. */
    @Test
    void endedSessionsAreAuditedWithTheirCount() {
        when(refreshTokens.revokeAll(7L)).thenReturn(2);

        listener.revokeRefreshTokens(new SessionsInvalidated(7L));

        verify(audit).record(AuditEvent.SESSIONS_REVOKED, 7L, "refreshTokens", 2);
    }

    /** The user's existing access tokens are revoked. */
    @Test
    void accessTokensAreCutOff() {
        listener.revokeAccessTokens(new SessionsInvalidated(7L));

        verify(revocations).revokeAllIssuedBefore(7L);
    }
}

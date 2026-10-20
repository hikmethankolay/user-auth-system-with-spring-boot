package com.hikmethankolay.template.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hikmethankolay.template.TestProperties;
import com.hikmethankolay.template.audit.AuditEvent;
import com.hikmethankolay.template.audit.AuditLog;
import com.hikmethankolay.template.common.SecureTokens;
import com.hikmethankolay.template.user.User;
import com.hikmethankolay.template.user.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.authentication.BadCredentialsException;

/** Unit tests of {@link RefreshTokenService} with a mocked repository: rotation, reuse detection, session listing and cleanup. */
@ExtendWith(OutputCaptureExtension.class)
class RefreshTokenServiceTest {

    /** Raw value of the stored refresh token. */
    private static final String RAW = "raw-refresh-token";
    /** Session (family) id of the stored token. */
    private static final UUID FAMILY = UUID.randomUUID();

    /** Refresh token persistence. */
    private final RefreshTokenRepository tokens = mock(RefreshTokenRepository.class);
    /** Reads the users' session versions. */
    private final UserRepository users = mock(UserRepository.class);
    /** Records audit events. */
    private final AuditLog audit = mock(AuditLog.class);
    /** The service under test. */
    private final RefreshTokenService service = new RefreshTokenService(tokens, TestProperties.create(), audit, users);

    /** A rotated remember-me token passes its session version, family and long lifetime on to its successor. */
    @Test
    void rotationKeepsTheSessionVersionAndRememberMe() {
        RefreshToken current = stored(true, 3);
        /**
         * Makes user 1 exist with the given session version.
         *
         * @param version the user's current session version
         */
        userWithSessionVersion(3);

        RefreshTokenService.Rotation rotation = service.rotate(RAW);

        assertThat(rotation.userId()).isEqualTo(1L);
        assertThat(rotation.successor().rememberMe()).isTrue();
        assertThat(rotation.successor().ttl()).isEqualTo(Duration.ofDays(30));
        assertThat(current.isUsed()).isTrue();
        RefreshToken successor = savedToken();
        assertThat(successor.getSessionVersion()).isEqualTo(3);
        assertThat(successor.isRememberMe()).isTrue();
        assertThat(successor.getFamilyId()).isEqualTo(FAMILY);
    }

    /** A rotated normal token keeps the short refresh lifetime. */
    @Test
    void rotationOfANormalSessionKeepsTheShortLifetime() {
        /**
         * Stores a refresh token for {@link #RAW} owned by user 1.
         *
         * @param rememberMe     whether the session uses "remember me"
         * @param sessionVersion the session version the token was issued under
         * @return the stored token
         */
        stored(false, 0);
        userWithSessionVersion(0);

        RefreshTokenService.Rotation rotation = service.rotate(RAW);

        assertThat(rotation.successor().rememberMe()).isFalse();
        assertThat(rotation.successor().ttl()).isEqualTo(Duration.ofDays(1));
        assertThat(savedToken().isRememberMe()).isFalse();
    }

    /** Presenting a used token deletes its whole session and records the reuse. */
    @Test
    void reusedTokenEndsTheSessionAndIsAudited() {
        stored(false, 0).markUsed(Instant.now());

        assertThatThrownBy(() -> service.rotate(RAW)).isInstanceOf(BadCredentialsException.class);

        verify(tokens).deleteAllByFamilyId(FAMILY);
        verify(audit).record(AuditEvent.REFRESH_TOKEN_REUSED, 1L, "sessionId", FAMILY);
    }

    /** Ending every session reports how many tokens were deleted. */
    @Test
    void revokeAllReturnsTheNumberOfEndedTokens() {
        when(tokens.deleteAllByUserId(1L)).thenReturn(4);

        assertThat(service.revokeAll(1L)).isEqualTo(4);
    }

    /** A listed session shows its remember-me flag and when its current token was issued. */
    @Test
    void activeSessionsShowWhenTheirTokenWasIssued() {
        RefreshToken token =
                new RefreshToken(1L, FAMILY, 0, "hash", true, Instant.now().plusSeconds(60));
        when(tokens.findActiveByUserId(any(Long.class), any(Instant.class))).thenReturn(List.of(token));

        assertThat(service.activeSessions(1L)).singleElement().satisfies(session -> {
            assertThat(session.rememberMe()).isTrue();
            assertThat(session.lastUsedAt()).isEqualTo(token.getCreatedAt());
        });
    }

    /**
     * The hourly cleanup logs only when it deleted something.
     *
     * @param output captured console output
     */
    @Test
    void cleanupLogsOnlyWhenTokensWereDeleted(CapturedOutput output) {
        when(tokens.deleteAllExpiredBefore(any(Instant.class))).thenReturn(0);
        service.deleteExpiredTokens();
        assertThat(output).doesNotContain("expired refresh tokens");

        when(tokens.deleteAllExpiredBefore(any(Instant.class))).thenReturn(1);
        service.deleteExpiredTokens();
        assertThat(output).contains("Deleted 1 expired refresh tokens");
    }

    private RefreshToken stored(boolean rememberMe, int sessionVersion) {
        RefreshToken token = new RefreshToken(
                1L,
                FAMILY,
                sessionVersion,
                SecureTokens.hash(RAW),
                rememberMe,
                Instant.now().plusSeconds(60));
        when(tokens.findByTokenHash(SecureTokens.hash(RAW))).thenReturn(Optional.of(token));
        return token;
    }

    private void userWithSessionVersion(int version) {
        User user = mock(User.class);
        when(user.getSessionVersion()).thenReturn(version);
        when(users.findById(1L)).thenReturn(Optional.of(user));
    }

    /**
     * Returns the token passed to the repository's {@code save}.
     *
     * @return the saved token
     */
    private RefreshToken savedToken() {
        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(tokens).save(saved.capture());
        return saved.getValue();
    }
}

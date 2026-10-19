package com.hikmethankolay.template.auth;

import com.hikmethankolay.template.user.PersonalDataProvider;
import org.springframework.stereotype.Component;

/** Adds the user's active sessions (refresh token families) to the personal data export. */
@Component
public class SessionDataProvider implements PersonalDataProvider {

    /** Lists the sessions. */
    private final RefreshTokenService refreshTokens;

    /**
     * Creates the provider.
     *
     * @param refreshTokens lists the sessions
     */
    public SessionDataProvider(RefreshTokenService refreshTokens) {
        this.refreshTokens = refreshTokens;
    }

    /**
     * Returns the section name.
     *
     * @return {@code sessions}
     */
    @Override
    public String section() {
        return "sessions";
    }

    /**
     * Lists the user's active sessions.
     *
     * @param userId the user
     * @return one {@link RefreshTokenService.Session} per session, newest first
     */
    @Override
    public Object export(long userId) {
        return refreshTokens.activeSessions(userId);
    }
}

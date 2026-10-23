package com.hikmethankolay.template.auth;

import java.time.Duration;

/** Outcome of a correct first factor: either the session's tokens, or a request for the second factor. */
public sealed interface LoginResult permits AuthTokens, LoginResult.MfaRequired {

    /**
     * The user has two-factor authentication: send the token back with a code to
     * {@code POST /api/v1/auth/login/mfa}.
     *
     * @param mfaToken identifies this login attempt
     * @param ttl      how long the code can be sent
     */
    record MfaRequired(String mfaToken, Duration ttl) implements LoginResult {}
}

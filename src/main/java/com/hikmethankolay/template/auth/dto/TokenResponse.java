package com.hikmethankolay.template.auth.dto;

import java.time.Duration;

/**
 * Token response, shaped like an OAuth 2.0 access token response (RFC 6749, section 5.1).
 *
 * @param accessToken the signed JWT
 * @param tokenType   always {@code Bearer}
 * @param expiresIn   token lifetime in seconds
 */
public record TokenResponse(String accessToken, String tokenType, long expiresIn) {

    /**
     * Creates a bearer token response.
     *
     * @param accessToken the signed JWT
     * @param ttl         token lifetime
     * @return the response body
     */
    public static TokenResponse bearer(String accessToken, Duration ttl) {
        return new TokenResponse(accessToken, "Bearer", ttl.toSeconds());
    }
}

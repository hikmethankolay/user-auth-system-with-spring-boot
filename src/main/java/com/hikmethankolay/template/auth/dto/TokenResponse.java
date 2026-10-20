package com.hikmethankolay.template.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Duration;

/**
 * Token response, shaped like an OAuth 2.0 access token response (RFC 6749, section 5.1).
 *
 * @param accessToken  the signed JWT
 * @param tokenType    always {@code Bearer}
 * @param expiresIn    access token lifetime in seconds
 * @param refreshToken single-use token for {@code POST /api/v1/auth/refresh}
 */
@Schema(requiredProperties = {"accessToken", "tokenType", "expiresIn", "refreshToken"})
public record TokenResponse(String accessToken, @Schema(allowableValues = "Bearer") String tokenType, long expiresIn, String refreshToken) {

    /**
     * Creates a bearer token response.
     *
     * @param accessToken  the signed JWT
     * @param ttl          access token lifetime
     * @param refreshToken the refresh token
     * @return the response body
     */
    public static TokenResponse bearer(String accessToken, Duration ttl, String refreshToken) {
        return new TokenResponse(accessToken, "Bearer", ttl.toSeconds(), refreshToken);
    }
}

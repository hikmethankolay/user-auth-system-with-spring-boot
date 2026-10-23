package com.hikmethankolay.template.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Login response of a user with two-factor authentication: no tokens yet, send a code first.
 *
 * @param mfaToken  send it with the code to {@code POST /api/v1/auth/login/mfa}
 * @param expiresIn seconds left to send the code
 */
@Schema(requiredProperties = {"mfaToken", "expiresIn"})
public record MfaChallengeResponse(String mfaToken, long expiresIn) {}

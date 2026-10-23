package com.hikmethankolay.template.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code POST /api/v1/auth/login/mfa}.
 *
 * @param mfaToken the token from the login response
 * @param code     the code shown by the authenticator app, or an unused recovery code
 */
public record MfaLoginRequest(
        @NotBlank @Size(max = 64) String mfaToken,
        @NotBlank @Size(max = 32) String code) {}

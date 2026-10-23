package com.hikmethankolay.template.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code POST /api/v1/auth/oauth2/{provider}}: what the provider sent to the frontend's redirect page.
 * The frontend must have checked {@code state} before calling.
 *
 * @param code         the authorization code from the redirect
 * @param codeVerifier the PKCE code verifier, if the authorization request had a {@code code_challenge}
 */
public record SocialLoginRequest(
        @NotBlank @Size(max = 2048) String code,
        @Size(min = 43, max = 128) String codeVerifier) {}

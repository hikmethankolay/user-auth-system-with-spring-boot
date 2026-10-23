package com.hikmethankolay.template.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A configured login provider.
 *
 * @param provider         id to use in {@code POST /api/v1/auth/oauth2/{provider}}
 * @param authorizationUri where to send the user, after adding {@code state} and, recommended,
 *                         {@code code_challenge} with {@code code_challenge_method=S256}
 */
@Schema(requiredProperties = {"provider", "authorizationUri"})
public record SocialProviderResponse(
        String provider, @Schema(format = "uri") String authorizationUri) {}

package com.hikmethankolay.template.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Body of {@code POST /api/v1/auth/login}.
 *
 * @param identifier username or email address
 * @param password   plain password
 * @param rememberMe optional; {@code true} issues a long-lived token and a persistent cookie
 */
public record LoginRequest(@NotBlank String identifier, @NotBlank String password, Boolean rememberMe) {

    /**
     * Tells whether "remember me" was requested.
     *
     * @return {@code true} only if {@code rememberMe} is explicitly {@code true}
     */
    public boolean isRememberMe() {
        return Boolean.TRUE.equals(rememberMe);
    }
}

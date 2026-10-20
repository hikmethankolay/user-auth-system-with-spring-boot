package com.hikmethankolay.template.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code POST /api/v1/auth/verify-email}.
 *
 * @param token the token from the email link
 */
public record TokenRequest(
        @NotBlank @Size(min = 1, max = 100) String token) {}

package com.hikmethankolay.template.auth.dto;

import com.hikmethankolay.template.user.validation.ValidPassword;
import com.hikmethankolay.template.user.validation.ValidUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code POST /api/v1/auth/register}.
 *
 * @param username unique username; see {@link ValidUsername}
 * @param email    unique email address
 * @param password password; see {@link ValidPassword}
 */
public record RegisterRequest(
        @NotBlank @ValidUsername String username,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @ValidPassword String password) {
}

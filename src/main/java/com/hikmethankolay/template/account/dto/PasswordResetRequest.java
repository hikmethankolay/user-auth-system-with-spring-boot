package com.hikmethankolay.template.account.dto;

import com.hikmethankolay.template.user.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code POST /api/v1/auth/password-reset/confirm}.
 *
 * @param token       the token from the reset email
 * @param newPassword the new password; see {@link ValidPassword}
 */
public record PasswordResetRequest(
        @NotBlank @Size(min = 1, max = 100) String token,
        @NotBlank @ValidPassword String newPassword) {
}

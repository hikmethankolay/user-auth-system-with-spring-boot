package com.hikmethankolay.template.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code POST /api/v1/auth/verify-email/resend} and {@code POST /api/v1/auth/password-reset}.
 *
 * @param email the account's email address
 */
public record EmailRequest(@NotBlank @Email @Size(max = 254) String email) {
}

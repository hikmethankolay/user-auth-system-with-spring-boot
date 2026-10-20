package com.hikmethankolay.template.account.dto;

import com.hikmethankolay.template.user.validation.ValidEmail;
import jakarta.validation.constraints.NotBlank;

/**
 * Body of {@code POST /api/v1/auth/verify-email/resend} and {@code POST /api/v1/auth/password-reset}.
 *
 * @param email the account's email address
 */
public record EmailRequest(@NotBlank @ValidEmail String email) {
}

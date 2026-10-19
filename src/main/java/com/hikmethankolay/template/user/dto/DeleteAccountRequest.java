package com.hikmethankolay.template.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Body of {@code DELETE /api/v1/users/me}.
 *
 * @param password the current password, so a stolen token alone cannot delete the account
 */
public record DeleteAccountRequest(@NotBlank String password) {
}

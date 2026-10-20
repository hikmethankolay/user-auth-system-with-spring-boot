package com.hikmethankolay.template.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code DELETE /api/v1/users/me}.
 *
 * @param password the current password, so a stolen token alone cannot delete the account
 */
public record DeleteAccountRequest(@NotBlank @Size(max = 72) String password) {
}

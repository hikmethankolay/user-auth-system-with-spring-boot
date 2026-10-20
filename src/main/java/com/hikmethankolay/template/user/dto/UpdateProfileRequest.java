package com.hikmethankolay.template.user.dto;

import com.hikmethankolay.template.user.validation.ValidEmail;
import com.hikmethankolay.template.user.validation.ValidUsername;

/**
 * Body of {@code PATCH /api/v1/users/me}. Omitted ({@code null}) fields are left unchanged.
 *
 * @param username new username
 * @param email    new email address
 */
public record UpdateProfileRequest(
        @ValidUsername String username,
        @ValidEmail String email) {
}

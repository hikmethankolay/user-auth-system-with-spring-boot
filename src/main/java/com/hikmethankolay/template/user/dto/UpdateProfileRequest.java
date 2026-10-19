package com.hikmethankolay.template.user.dto;

import com.hikmethankolay.template.user.validation.ValidUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code PATCH /api/v1/users/me}. Omitted ({@code null}) fields are left unchanged.
 *
 * @param username new username
 * @param email    new email address
 */
public record UpdateProfileRequest(
        @ValidUsername String username,
        @Email @Size(min = 3, max = 254) String email) {
}

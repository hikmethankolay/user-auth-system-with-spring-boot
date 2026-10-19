package com.hikmethankolay.template.user.dto;

import com.hikmethankolay.template.user.Role;
import com.hikmethankolay.template.user.validation.ValidPassword;
import com.hikmethankolay.template.user.validation.ValidUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code PATCH /api/v1/users/{id}} (administrators only). Omitted ({@code null}) fields are left unchanged.
 *
 * @param username new username
 * @param email    new email address
 * @param password new password; the current one is not required
 * @param role     new role
 */
public record AdminUpdateUserRequest(
        @ValidUsername String username,
        @Email @Size(min = 3, max = 254) String email,
        @ValidPassword String password,
        Role role) {
}

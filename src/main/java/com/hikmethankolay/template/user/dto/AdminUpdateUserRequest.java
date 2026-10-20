package com.hikmethankolay.template.user.dto;

import com.hikmethankolay.template.user.Role;
import com.hikmethankolay.template.user.validation.ValidEmail;
import com.hikmethankolay.template.user.validation.ValidPassword;
import com.hikmethankolay.template.user.validation.ValidUsername;

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
        @ValidEmail String email,
        @ValidPassword String password,
        Role role) {
}

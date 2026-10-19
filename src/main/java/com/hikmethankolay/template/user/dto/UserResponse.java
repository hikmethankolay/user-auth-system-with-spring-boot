package com.hikmethankolay.template.user.dto;

import com.hikmethankolay.template.user.Role;
import com.hikmethankolay.template.user.User;
import java.time.Instant;

/**
 * Public representation of a user. Never contains the password hash.
 *
 * @param id        user id
 * @param username  username
 * @param email     lower-case email address
 * @param role          role of the user
 * @param emailVerified whether the email address has been confirmed
 * @param createdAt     creation time (ISO-8601, UTC)
 * @param updatedAt     time of the last update (ISO-8601, UTC)
 */
public record UserResponse(
        long id, String username, String email, Role role, boolean emailVerified, Instant createdAt, Instant updatedAt) {

    /**
     * Maps an entity to its public representation.
     *
     * @param user the entity
     * @return the response body
     */
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(),
                user.isEmailVerified(), user.getCreatedAt(), user.getUpdatedAt());
    }
}

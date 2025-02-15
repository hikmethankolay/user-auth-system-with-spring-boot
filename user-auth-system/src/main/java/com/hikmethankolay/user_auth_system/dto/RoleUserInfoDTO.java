/**
 * @file RoleUserInfoDTO.java
 * @brief Data Transfer Object for role-user information.
 *
 * This DTO represents a user associated with a role, including user credentials.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.dto
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.dto;

import com.hikmethankolay.user_auth_system.entity.User;

/**
 * @class RoleUserInfoDTO
 * @brief DTO for user details in a role.
 *
 * This record holds basic user information associated with a role.
 */
public record RoleUserInfoDTO(
        Long id,
        String username,
        String email
) {
    /**
     * @brief Constructs a RoleUserInfoDTO from a User entity.
     * @param user The user entity.
     */
    public RoleUserInfoDTO(User user) {
        this(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}

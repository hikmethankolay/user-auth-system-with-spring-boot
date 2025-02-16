/**
 * @file ERole.java
 * @brief Enum representing user roles.
 *
 * This enum defines different roles for users, such as user, moderator, and admin.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.enums
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.enums;

/**
 * Enum representing different user roles.
 *
 * This enum defines roles such as:
 * - ROLE_USER: Standard user role.
 * - ROLE_MODERATOR: Moderator role with additional privileges.
 * - ROLE_ADMIN: Administrator role with full access.
 */
public enum ERole {
    /** Standard user role. */
    ROLE_USER,

    /** Moderator role with additional privileges. */
    ROLE_MODERATOR,

    /** Administrator role with full access. */
    ROLE_ADMIN
}

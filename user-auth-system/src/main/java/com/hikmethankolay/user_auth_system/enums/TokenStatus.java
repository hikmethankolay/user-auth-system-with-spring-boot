/**
 * @file TokenStatus.java
 * @brief Enum representing authentication token statuses.
 *
 * This enum defines different statuses for authentication tokens, such as valid, expired, and invalid.
 *
 * @author Hikmethan Kolay
 * @date 2025-03-29
 */

/**
 * @package com.hikmethankolay.user_auth_system.enums
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.enums;

/**
 * Enum representing different authentication token statuses.
 *
 * This enum defines statuses such as:
 * - VALID: Token is valid and can be used for authentication.
 * - EXPIRED: Token has exceeded its lifetime and needs to be refreshed.
 * - INVALID: Token is invalid due to tampering or other issues.
 */
public enum TokenStatus {
    /** Token is valid and can be used for authentication. */
    VALID,

    /** Token has exceeded its lifetime and needs to be refreshed. */
    EXPIRED,

    /** Token is invalid due to tampering or other issues. */
    INVALID
}
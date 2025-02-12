/**
 * @file EApiStatus.java
 * @brief Enum representing API response statuses.
 *
 * This enum defines different statuses for API responses, such as success, failure, and unauthorized access.
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
 * @enum EApiStatus
 * @brief Enumeration of API response statuses.
 *
 * This enum defines possible API response statuses.
 */
public enum EApiStatus {
    /** Successful API response. */
    SUCCESS,

    /** Failed API response. */
    FAILURE,

    /** Unauthorized API response. */
    UNAUTHORIZED
}
/**
 * @file UserInfo.java
 * @brief Interface for user information.
 *
 * This interface defines methods for retrieving all user details.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.dto
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.dto;

import com.hikmethankolay.user_auth_system.enums.ERole;
import java.util.Set;

/**
 * @interface UserInfo
 * @brief Interface for all user details.
 *
 * This interface provides methods to access all user information.
 */
public interface UserInfo {
    /**
     * @brief Retrieves the ID of the user.
     * @return The ID as a Long.
     */
    Long getId();

    /**
     * @brief Retrieves the username of the user.
     * @return The username as a string.
     */
    String getUsername();

    /**
     * @brief Retrieves the email of the user.
     * @return The email as a string.
     */
    String getEmail();

    /**
     * @brief Retrieves the password of the user.
     * @return The password as a string.
     */
    String getPassword();

    /**
     * @brief Retrieves the roles assigned to the user.
     * @return A set of ERole enums.
     */
    Set<ERole> getRoles();
}
/**
 * @file LoginRequestDTO.java
 * @brief Data Transfer Object for login requests.
 *
 * This DTO encapsulates user login credentials, including an identifier
 * (username or email) and password.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.dto
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.dto;

/**
 * @class LoginRequestDTO
 * @brief DTO for login request data.
 *
 * This record holds the necessary login credentials.
 */
public record LoginRequestDTO(String identifier, String password) {
}
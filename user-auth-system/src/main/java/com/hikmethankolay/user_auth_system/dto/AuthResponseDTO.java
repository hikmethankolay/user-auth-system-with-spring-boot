/**
 * @file AuthResponseDTO.java
 * @brief Data Transfer Object for authentication responses.
 *
 * This DTO encapsulates authentication tokens, specifying the token value and type.
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
 * @class AuthResponseDTO
 * @brief DTO for authentication responses.
 *
 * This record holds an authentication token and its type.
 */
public record AuthResponseDTO(String token, String tokenType) {

    /**
     * @brief Constructor with default token type "Bearer".
     * @param token The authentication token.
     */
    public AuthResponseDTO(String token) {
        this(token, "Bearer");
    }
}
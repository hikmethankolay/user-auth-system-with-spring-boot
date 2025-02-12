/**
 * @file ApiResponseDTO.java
 * @brief Data Transfer Object for API responses.
 *
 * This generic DTO is used to standardize API responses by including a status,
 * data payload, and message.
 *
 * @tparam T The type of the data payload.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.dto
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.dto;

import com.hikmethankolay.user_auth_system.enums.EApiStatus;

/**
 * @record ApiResponseDTO
 * @brief Generic API response wrapper.
 *
 * @tparam T The type of the data payload.
 */
public record ApiResponseDTO<T>(EApiStatus status, T data, String message) {
}
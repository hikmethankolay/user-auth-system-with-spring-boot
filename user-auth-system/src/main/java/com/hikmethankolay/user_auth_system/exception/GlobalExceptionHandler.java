/**
 * @file GlobalExceptionHandler.java
 * @brief Global exception handler for centralized error management.
 *
 * This class provides application-wide exception handling to standardize error responses
 * and improve error handling consistency across the application.
 *
 * @author Hikmethan Kolay
 * @date 2025-03-29
 */

/**
 * @package com.hikmethankolay.user_auth_system.exception
 * @brief Contains exception handling components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.exception;

import com.hikmethankolay.user_auth_system.dto.ApiResponseDTO;
import com.hikmethankolay.user_auth_system.enums.EApiStatus;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * @class GlobalExceptionHandler
 * @brief Controller advice for handling exceptions across the application.
 *
 * This class provides centralized exception handling for converting exceptions
 * into standardized API responses with appropriate HTTP status codes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @brief Handles validation constraint violations.
     * @param ex The constraint violation exception.
     * @return Response entity containing validation error details.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDTO<String>> handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();

        return ResponseEntity.badRequest()
                .body(new ApiResponseDTO<>(EApiStatus.FAILURE, null, String.join(", ", errors)));
    }

    /**
     * @brief Handles all unhandled exceptions as a fallback.
     * @param ex The exception to handle.
     * @return Response entity containing the exception message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<String>> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(EApiStatus.FAILURE, null, ex.getMessage()));
    }
}
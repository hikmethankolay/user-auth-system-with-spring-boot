/**
 * @file GlobalExceptionHandlerTest.java
 * @brief Tests for the GlobalExceptionHandler class.
 *
 * Contains unit tests for exception handling mechanisms.
 *
 * @author Test Suite Generator
 * @date 2025-03-29
 */
package com.hikmethankolay.user_auth_system.exception;

import com.hikmethankolay.user_auth_system.dto.ApiResponseDTO;
import com.hikmethankolay.user_auth_system.enums.EApiStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @class GlobalExceptionHandlerTest
 * @brief Test class for GlobalExceptionHandler.
 *
 * This class contains unit tests for centralized exception handling.
 */
@SpringBootTest
public class GlobalExceptionHandlerTest {

    /**
     * GlobalExceptionHandler instance to be tested.
     */
    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    /**
     * Mock ConstraintViolation for testing validation errors.
     */
    @MockitoBean
    private ConstraintViolation<Object> violation;

    /**
     * Mock Path for constraint violation property path.
     */
    @MockitoBean
    private Path path;

    /**
     * @brief Setup method that runs before each test.
     *
     * Initializes common mocks.
     */
    @BeforeEach
    public void setup() {
        // Configure the mock to return a non-null path when getPropertyPath() is called
        when(violation.getPropertyPath()).thenReturn(path);

        // If you need more path operations, configure them too
        when(path.toString()).thenReturn("username");

        // Add additional mock behavior as needed
        when(violation.getMessage()).thenReturn("must be between 8 and 32 characters");
    }

    /**
     * @brief Test handling of constraint violation exceptions.
     *
     * Verifies that handleConstraintViolationException correctly formats validation errors.
     */
    @Test
    public void testHandleConstraintViolationException() {
        // Arrange
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);
        ConstraintViolationException ex = new ConstraintViolationException("Validation error", violations);

        // Act
        ResponseEntity<ApiResponseDTO<String>> response = globalExceptionHandler.handleConstraintViolationException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(EApiStatus.FAILURE, response.getBody().status());
        assertNull(response.getBody().data());
        assertEquals("username: must be between 8 and 32 characters", response.getBody().message());
    }

    /**
     * @brief Test handling of multiple constraint violations.
     *
     * Verifies that handleConstraintViolationException correctly formats multiple validation errors.
     */
    @Test
    public void testHandleMultipleConstraintViolations() {
        // Arrange
        Set<ConstraintViolation<?>> violations = new HashSet<>();

        // Create first violation
        ConstraintViolation<Object> violation1 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        when(violation1.getPropertyPath()).thenReturn(path1);
        when(path1.toString()).thenReturn("username");
        when(violation1.getMessage()).thenReturn("must be between 8 and 32 characters");
        violations.add(violation1);

        // Create second violation
        ConstraintViolation<Object> violation2 = mock(ConstraintViolation.class);
        Path path2 = mock(Path.class);
        when(violation2.getPropertyPath()).thenReturn(path2);
        when(path2.toString()).thenReturn("email");
        when(violation2.getMessage()).thenReturn("must be a valid email address");
        violations.add(violation2);

        ConstraintViolationException ex = new ConstraintViolationException("Validation error", violations);

        // Act
        ResponseEntity<ApiResponseDTO<String>> response = globalExceptionHandler.handleConstraintViolationException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(EApiStatus.FAILURE, response.getBody().status());
        assertNull(response.getBody().data());

        // The order of the errors in the message might not be predictable
        String message = response.getBody().message();
        assertTrue(message.contains("username: must be between 8 and 32 characters"));
        assertTrue(message.contains("email: must be a valid email address"));
    }

    /**
     * @brief Test handling of general exceptions.
     *
     * Verifies that handleGeneralException correctly handles unexpected exceptions.
     */
    @Test
    public void testHandleGeneralException() {
        // Arrange
        Exception ex = new RuntimeException("Something went wrong");

        // Act
        ResponseEntity<ApiResponseDTO<String>> response = globalExceptionHandler.handleGeneralException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(EApiStatus.FAILURE, response.getBody().status());
        assertNull(response.getBody().data());
        assertEquals("Something went wrong", response.getBody().message());
    }

    /**
     * @brief Test handling of nested exceptions.
     *
     * Verifies that handleGeneralException correctly handles exceptions with cause.
     */
    @Test
    public void testHandleNestedExceptions() {
        // Arrange
        Exception cause = new IllegalArgumentException("Invalid argument");
        Exception ex = new RuntimeException("Operation failed", cause);

        // Act
        ResponseEntity<ApiResponseDTO<String>> response = globalExceptionHandler.handleGeneralException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(EApiStatus.FAILURE, response.getBody().status());
        assertNull(response.getBody().data());
        assertEquals("Operation failed", response.getBody().message());
    }
}
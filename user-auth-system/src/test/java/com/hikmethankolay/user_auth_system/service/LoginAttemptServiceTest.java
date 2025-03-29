/**
 * @file LoginAttemptServiceTest.java
 * @brief Tests for the LoginAttemptService class.
 *
 * Contains unit tests for login attempt tracking and blocking operations.
 *
 * @author Test Suite Generator
 * @date 2025-03-29
 */
package com.hikmethankolay.user_auth_system.service;

import com.hikmethankolay.user_auth_system.security.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @class LoginAttemptServiceTest
 * @brief Test class for LoginAttemptService.
 *
 * This class contains unit tests for login attempt tracking and blocking operations.
 */
public class LoginAttemptServiceTest {

    /**
     * LoginAttemptService instance to be tested.
     */
    private LoginAttemptService loginAttemptService;

    /**
     * @brief Setup method that runs before each test.
     *
     * Initializes the login attempt service.
     */
    @BeforeEach
    public void setUp() {
        loginAttemptService = new LoginAttemptService();
    }

    /**
     * @brief Test successful login resets attempt counter.
     *
     * Verifies that loginSucceeded properly resets the counter.
     */
    @Test
    public void testLoginSucceeded() {
        // Arrange
        String key = "test@example.com";
        loginAttemptService.loginFailed(key); // One failed attempt
        
        // Act
        loginAttemptService.loginSucceeded(key);
        
        // Assert
        assertFalse(loginAttemptService.isBlocked(key));
    }

    /**
     * @brief Test failed login increments attempt counter.
     *
     * Verifies that loginFailed properly increments the counter.
     */
    @Test
    public void testLoginFailed() {
        // Arrange
        String key = "test@example.com";
        
        // Act - simulate 3 failed attempts
        loginAttemptService.loginFailed(key);
        loginAttemptService.loginFailed(key);
        loginAttemptService.loginFailed(key);
        
        // Assert - should not be blocked yet (10 is the threshold)
        assertFalse(loginAttemptService.isBlocked(key));
    }

    /**
     * @brief Test blocking after maximum attempts.
     *
     * Verifies that isBlocked correctly returns true after maximum attempts.
     */
    @Test
    public void testIsBlockedAfterMaxAttempts() {
        // Arrange
        String key = "test@example.com";
        
        // Act - simulate 10 failed attempts (the maximum allowed)
        for (int i = 0; i < 10; i++) {
            loginAttemptService.loginFailed(key);
        }
        
        // Assert
        assertTrue(loginAttemptService.isBlocked(key));
    }

    /**
     * @brief Test initial state is not blocked.
     *
     * Verifies that isBlocked correctly returns false initially.
     */
    @Test
    public void testIsNotBlockedInitially() {
        // Assert
        assertFalse(loginAttemptService.isBlocked("new@example.com"));
    }

    /**
     * @brief Test different keys are tracked separately.
     *
     * Verifies that attempts are tracked separately for different keys.
     */
    @Test
    public void testDifferentKeysTrackedSeparately() {
        // Arrange
        String key1 = "user1@example.com";
        String key2 = "user2@example.com";
        
        // Act - block key1 but not key2
        for (int i = 0; i < 10; i++) {
            loginAttemptService.loginFailed(key1);
        }
        loginAttemptService.loginFailed(key2);
        
        // Assert
        assertTrue(loginAttemptService.isBlocked(key1));
        assertFalse(loginAttemptService.isBlocked(key2));
    }

    /**
     * @brief Test login succeeded after being blocked.
     *
     * Verifies that loginSucceeded properly resets the counter even after being blocked.
     */
    @Test
    public void testLoginSucceededAfterBlocked() {
        // Arrange
        String key = "test@example.com";
        
        // Block the key
        for (int i = 0; i < 10; i++) {
            loginAttemptService.loginFailed(key);
        }
        assertTrue(loginAttemptService.isBlocked(key));
        
        // Act - succeed login
        loginAttemptService.loginSucceeded(key);
        
        // Assert - should no longer be blocked
        assertFalse(loginAttemptService.isBlocked(key));
    }
}

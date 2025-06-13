/**
 * @file AuthConfigTest.java
 * @brief Tests for the AuthConfig class.
 *
 * Contains unit tests for authentication configuration including
 * password encoder and authentication manager beans.
 *
 * @author Test Suite Generator
 * @date 2025-03-29
 */
package com.hikmethankolay.user_auth_system.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @class AuthConfigTest
 * @brief Test class for AuthConfig.
 *
 * This class contains unit tests for authentication configuration.
 */
@SpringBootTest
public class AuthConfigTest {

    /**
     * AuthConfig instance to be tested.
     */
    @Autowired
    private AuthConfig authConfig;

    /**
     * Mock AuthenticationConfiguration for testing authentication manager.
     */
    @MockitoBean
    private AuthenticationConfiguration authenticationConfiguration;

    /**
     * Mock AuthenticationManager for testing.
     */
    @MockitoBean
    private AuthenticationManager authenticationManager;

    /**
     * @brief Test password encoder bean.
     *
     * Verifies that passwordEncoder correctly returns a BCryptPasswordEncoder.
     */
    @Test
    public void testPasswordEncoder() {
        // Act
        PasswordEncoder encoder = authConfig.passwordEncoder();
        
        // Assert
        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);
        
        // Test encoding
        String password = "testPassword";
        String encoded = encoder.encode(password);
        
        // Verify encoding produces different results each time
        String encoded2 = encoder.encode(password);
        assertNotEquals(encoded, encoded2);
        
        // Verify matches works correctly
        assertTrue(encoder.matches(password, encoded));
        assertFalse(encoder.matches("wrongPassword", encoded));
    }
} 
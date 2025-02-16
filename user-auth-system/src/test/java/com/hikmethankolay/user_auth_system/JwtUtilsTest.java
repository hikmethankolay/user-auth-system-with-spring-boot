/**
 * @file JwtUtilsTest.java
 * @brief Unit tests for JwtUtils class.
 *
 * This file contains JUnit tests for verifying the functionality of the JwtUtils class.
 * The tests ensure that JWT token generation, validation, and parsing work as expected.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-16
 */

/**
 * @package com.hikmethankolay.user_auth_system
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system;

import com.hikmethankolay.user_auth_system.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @class JwtUtilsTest
 * @brief Unit tests for JwtUtils class.
 *
 * This test class verifies JWT token generation, validation, and extraction of claims.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "api.security.token.secret=test-secret",
        "api.security.token.expiration=3600000"
})
class JwtUtilsTest {

    /**
     * Instance of JwtUtils to be tested.
     */
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Test user ID used for token generation.
     */
    private String testUserId = "12345";

    /**
     * Test username used for token generation.
     */
    private String testUsername = "testuser";

    /**
     * JWT token generated for tests.
     */
    private String token;

    /**
     * @brief Sets up test preconditions.
     *
     * Generates a JWT token before each test.
     */
    @BeforeEach
    void setUp() {
        token = jwtUtils.generateJwtToken(testUserId, testUsername);
    }

    /**
     * @brief Tests JWT token generation.
     *
     * Ensures that the generated token is not null.
     */
    @Test
    void testGenerateJwtToken() {
        assertNotNull(token);
    }

    /**
     * @brief Tests validation of a valid JWT token.
     *
     * Ensures that the generated token is recognized as valid.
     */
    @Test
    void testValidateJwtToken_ValidToken() {
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    /**
     * @brief Tests validation of an invalid JWT token.
     *
     * Ensures that an altered token is recognized as invalid.
     */
    @Test
    void testValidateJwtToken_InvalidToken() {
        assertFalse(jwtUtils.validateJwtToken(token + "invalid"));
    }

    /**
     * @brief Tests extraction of user ID from a JWT token.
     *
     * Ensures that the extracted user ID matches the expected value.
     */
    @Test
    void testGetUserIdFromJwtToken() {
        assertEquals(Long.valueOf(testUserId), jwtUtils.getUserIdFromJwtToken(token));
    }

    /**
     * @brief Tests extraction of username from a JWT token.
     *
     * Ensures that the extracted username matches the expected value.
     */
    @Test
    void testGetUserNameFromJwtToken() {
        assertEquals(testUsername, jwtUtils.getUserNameFromJwtToken(token));
    }
}

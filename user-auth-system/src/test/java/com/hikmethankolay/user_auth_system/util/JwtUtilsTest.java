/**
 * @file JwtUtilsTest.java
 * @brief Tests for the JwtUtils class.
 *
 * Contains unit tests for JWT token generation, validation, and parsing operations.
 *
 * @author Test Suite Generator
 * @date 2025-03-29
 */
package com.hikmethankolay.user_auth_system.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.hikmethankolay.user_auth_system.enums.TokenStatus;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @class JwtUtilsTest
 * @brief Test class for JwtUtils.
 *
 * This class contains unit tests for JWT token operations.
 */
public class JwtUtilsTest extends BaseUtilTest {

    /**
     * JwtUtils instance to be tested, with mocked dependencies injected.
     */
    @InjectMocks
    @Spy
    private JwtUtils jwtUtils;

    /**
     * Secret key for JWT operations.
     */
    private final String jwtSecret = "testSecretKeyThatIsLongEnoughForHMAC256Signature";

    /**
     * Standard token expiration time (15 minutes in milliseconds).
     */
    private final long jwtExpirationMs = 900000L;

    /**
     * Extended token expiration time for Remember Me (30 days in milliseconds).
     */
    private final long rememberMeExpirationMs = 2592000000L;

    /**
     * @brief Setup method that runs before each test.
     *
     * Initializes token settings.
     */
    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", jwtExpirationMs);
        ReflectionTestUtils.setField(jwtUtils, "rememberMeExpirationMs", rememberMeExpirationMs);
    }

    /**
     * @brief Test JWT token generation with standard expiration.
     *
     * Verifies that generateJwtToken correctly creates tokens with standard expiration.
     */
    @Test
    public void testGenerateJwtToken() {
        // Act
        String token = jwtUtils.generateJwtToken("1", "testuser", false);
        
        // Assert
        assertNotNull(token);
        
        // Verify token contains expected claims
        String userId = JWT.decode(token).getSubject();
        String username = JWT.decode(token).getClaim("username").asString();
        Boolean rememberMe = JWT.decode(token).getClaim("rememberMe").asBoolean();
        
        assertEquals("1", userId);
        assertEquals("testuser", username);
        assertFalse(rememberMe);
    }

    /**
     * @brief Test JWT token generation with Remember Me.
     *
     * Verifies that generateJwtToken correctly creates tokens with extended expiration.
     */
    @Test
    public void testGenerateJwtTokenWithRememberMe() {
        // Act
        String token = jwtUtils.generateJwtToken("1", "testuser", true);
        
        // Assert
        assertNotNull(token);
        
        // Verify token contains expected claims
        String userId = JWT.decode(token).getSubject();
        String username = JWT.decode(token).getClaim("username").asString();
        Boolean rememberMe = JWT.decode(token).getClaim("rememberMe").asBoolean();
        
        assertEquals("1", userId);
        assertEquals("testuser", username);
        assertTrue(rememberMe);
        
        // Verify expiration time is extended
        Date expirationDate = JWT.decode(token).getExpiresAt();
        long expectedExpiration = System.currentTimeMillis() + rememberMeExpirationMs;
        
        // Allow a small tolerance for execution time
        assertTrue(Math.abs(expirationDate.getTime() - expectedExpiration) < 1000);
    }

    /**
     * @brief Test validation of valid JWT token.
     *
     * Verifies that validateJwtToken correctly identifies valid tokens.
     */
    @Test
    public void testValidateJwtTokenValid() {
        // Arrange
        String token = JWT.create()
                .withSubject("1")
                .withClaim("username", "testuser")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 10000))
                .sign(Algorithm.HMAC256(jwtSecret));
        
        // Act
        TokenStatus status = jwtUtils.validateJwtToken(token);
        
        // Assert
        assertEquals(TokenStatus.VALID, status);
    }

    /**
     * @brief Test validation of expired JWT token.
     *
     * Verifies that validateJwtToken correctly identifies expired tokens.
     */
    @Test
    public void testValidateJwtTokenExpired() {
        // Arrange
        String token = JWT.create()
                .withSubject("1")
                .withClaim("username", "testuser")
                .withIssuedAt(new Date(System.currentTimeMillis() - 20000))
                .withExpiresAt(new Date(System.currentTimeMillis() - 10000))
                .sign(Algorithm.HMAC256(jwtSecret));
        
        // Act
        TokenStatus status = jwtUtils.validateJwtToken(token);
        
        // Assert
        assertEquals(TokenStatus.EXPIRED, status);
    }

    /**
     * @brief Test validation of invalid JWT token.
     *
     * Verifies that validateJwtToken correctly identifies invalid tokens.
     */
    @Test
    public void testValidateJwtTokenInvalid() {
        // Arrange - token signed with different secret
        String token = JWT.create()
                .withSubject("1")
                .withClaim("username", "testuser")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 10000))
                .sign(Algorithm.HMAC256("differentSecret"));
        
        // Act
        TokenStatus status = jwtUtils.validateJwtToken(token);
        
        // Assert
        assertEquals(TokenStatus.INVALID, status);
    }

    /**
     * @brief Test extraction of user ID from JWT token.
     *
     * Verifies that getUserIdFromJwtToken correctly extracts the user ID.
     */
    @Test
    public void testGetUserIdFromJwtToken() {
        // Arrange
        String token = JWT.create()
                .withSubject("123")
                .withClaim("username", "testuser")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 10000))
                .sign(Algorithm.HMAC256(jwtSecret));
        
        // Act
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        
        // Assert
        assertEquals(123L, userId);
    }

    /**
     * @brief Test extraction of username from JWT token.
     *
     * Verifies that getUserNameFromJwtToken correctly extracts the username.
     */
    @Test
    public void testGetUserNameFromJwtToken() {
        // Arrange
        String token = JWT.create()
                .withSubject("1")
                .withClaim("username", "testuser")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 10000))
                .sign(Algorithm.HMAC256(jwtSecret));
        
        // Act
        String username = jwtUtils.getUserNameFromJwtToken(token);
        
        // Assert
        assertEquals("testuser", username);
    }

    /**
     * @brief Test checking Remember Me flag in token.
     *
     * Verifies that wasRememberMe correctly identifies Remember Me tokens.
     */
    @Test
    public void testWasRememberMe() {
        // Arrange
        String token = JWT.create()
                .withSubject("1")
                .withClaim("username", "testuser")
                .withClaim("rememberMe", true)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 10000))
                .sign(Algorithm.HMAC256(jwtSecret));
        
        // Act
        boolean result = jwtUtils.wasRememberMe(token);
        
        // Assert
        assertTrue(result);
    }

    /**
     * @brief Test checking Remember Me flag in token that doesn't have it.
     *
     * Verifies that wasRememberMe handles tokens without Remember Me flag.
     */
    @Test
    public void testWasRememberMeWithoutFlag() {
        // Arrange
        String token = JWT.create()
                .withSubject("1")
                .withClaim("username", "testuser")
                // No rememberMe claim
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 10000))
                .sign(Algorithm.HMAC256(jwtSecret));
        
        // Act
        boolean result = jwtUtils.wasRememberMe(token);
        
        // Assert
        assertFalse(result);
    }

    /**
     * @brief Test checking Remember Me flag in invalid token.
     *
     * Verifies that wasRememberMe handles invalid tokens gracefully.
     */
    @Test
    public void testWasRememberMeWithInvalidToken() {
        // Arrange
        String invalidToken = "invalid.jwt.token";
        
        // Act
        boolean result = jwtUtils.wasRememberMe(invalidToken);
        
        // Assert
        assertFalse(result);
    }

    /**
     * @brief Test extracting token from Authorization header.
     *
     * Verifies that extractTokenFromRequest correctly extracts token from header.
     */
    @Test
    public void testExtractTokenFromRequestHeader() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        // Act
        String extractedToken = jwtUtils.extractTokenFromRequest(request);
        
        // Assert
        assertEquals(token, extractedToken);
        verify(request).getHeader("Authorization");
    }

    /**
     * @brief Test extracting token from cookie.
     *
     * Verifies that extractTokenFromRequest correctly extracts token from cookie.
     */
    @Test
    public void testExtractTokenFromRequestCookie() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "valid.jwt.token";
        
        when(request.getHeader("Authorization")).thenReturn(null);
        
        Cookie authCookie = new Cookie("auth_token", token);
        Cookie[] cookies = new Cookie[] { authCookie };
        when(request.getCookies()).thenReturn(cookies);
        
        // Act
        String extractedToken = jwtUtils.extractTokenFromRequest(request);
        
        // Assert
        assertEquals(token, extractedToken);
        verify(request).getHeader("Authorization");
        verify(request).getCookies();
    }

    /**
     * @brief Test extracting token when none is present.
     *
     * Verifies that extractTokenFromRequest handles requests without tokens.
     */
    @Test
    public void testExtractTokenFromRequestNonePresent() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);
        
        // Act
        String extractedToken = jwtUtils.extractTokenFromRequest(request);
        
        // Assert
        assertNull(extractedToken);
        verify(request).getHeader("Authorization");
        verify(request).getCookies();
    }
}

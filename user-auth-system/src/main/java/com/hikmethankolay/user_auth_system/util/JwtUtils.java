/**
 * @file JwtUtils.java
 * @brief Utility class for handling JSON Web Tokens (JWTs).
 *
 * Provides methods for generating, validating, and parsing JWT tokens.
 * This class uses the Auth0 JWT library and is configured via Spring's
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.util
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.hikmethankolay.user_auth_system.enums.TokenStatus;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Utility class for generating, validating, and parsing JWT tokens.
 */
@Component
public class JwtUtils {


    /** JWT Secret Key */
    @Value("${api.security.token.secret}")
    private String jwtSecret;

    /** Standard JWT Token expiration time */
    @Value("${api.security.token.expiration}")
    private Long jwtExpirationMs;

    /** Extended expiration time for Remember Me */
    @Value("${api.security.token.remember-me-expiration}") // 30 days in milliseconds
    private Long rememberMeExpirationMs;

    /**
     * Generates a JWT token for a given user with optional Remember Me.
     *
     * @param userId The user ID to set as the token subject
     * @param username The username to include as a claim
     * @param rememberMe Whether to use extended expiration time
     * @return A signed JWT token string
     */
    public String generateJwtToken(String userId, String username, boolean rememberMe) {
        Long expirationTime = rememberMe ? rememberMeExpirationMs : jwtExpirationMs;

        return JWT.create()
                .withSubject(userId)
                .withClaim("username", username)
                .withClaim("rememberMe", rememberMe)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    /**
     * Validates the JWT token and returns its status.
     *
     * @param token The JWT token string to validate
     * @return The token status (VALID, EXPIRED, or INVALID)
     */
    public TokenStatus validateJwtToken(String token) {
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token);
            Date expirationDate = jwt.getExpiresAt();
            if (expirationDate.before(new Date())) {
                return TokenStatus.EXPIRED;
            }
            return TokenStatus.VALID;
        } catch (Exception e) {
            return TokenStatus.INVALID;
        }
    }

    /**
     * Extracts the user ID (subject) from a JWT token.
     *
     * @param token The JWT token string
     * @return The user ID as a Long
     */
    public Long getUserIdFromJwtToken(String token) {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token);
        return Long.valueOf(jwt.getSubject());
    }

    /**
     * Extracts the username from a JWT token.
     *
     * @param token The JWT token string
     * @return The username claim as a String
     */
    public String getUserNameFromJwtToken(String token) {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token);
        return jwt.getClaim("username").asString();
    }

    /**
     * Checks if the token was created with Remember Me.
     *
     * @param token The JWT token string
     * @return True if the token was created with Remember Me, false otherwise
     */
    public boolean wasRememberMe(String token) {
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token);
            return jwt.getClaim("rememberMe").asBoolean();
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * Extracts the JWT token from the request (header or cookie).
     *
     * @param request The HTTP request
     * @return The JWT token or null if not found
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        // First try to get from Authorization header
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // Then try to get from cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("auth_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}

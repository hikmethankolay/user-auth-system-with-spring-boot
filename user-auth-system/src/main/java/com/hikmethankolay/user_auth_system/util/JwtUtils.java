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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Utility class for generating, validating, and parsing JWT tokens.
 */
@Component
public class JwtUtils {

    /** JWT Token */
    @Value("${api.security.token.secret}")
    private String jwtSecret;

    /** JWT Token expiration time */
    @Value("${api.security.token.expiration}")
    private int jwtExpirationMs;

    /**
     * Generates a JWT token for a given user.
     *
     * @param userId   the user ID to set as the token subject
     * @param username the username to include as a claim
     * @return a signed JWT token string
     */
    public String generateJwtToken(String userId, String username) {
        return JWT.create()
                .withSubject(userId)
                .withClaim("username", username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    /**
     * Validates the provided JWT token.
     *
     * @param token the JWT token string to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateJwtToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts the user ID (subject) from a JWT token.
     *
     * @param token the JWT token string
     * @return the user ID as a Long
     */
    public Long getUserIdFromJwtToken(String token) {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token);
        return Long.valueOf(jwt.getSubject());
    }

    /**
     * Extracts the username from a JWT token.
     *
     * @param token the JWT token string
     * @return the username claim as a String
     */
    public String getUserNameFromJwtToken(String token) {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token);
        return jwt.getClaim("username").asString();
    }
}

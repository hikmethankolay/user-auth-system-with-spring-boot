package com.hikmethankolay.user_auth_system.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${api.security.token.secret}")
    private String jwtSecret;
    private final int jwtExpirationMs = 86400000; // Token valid for 1 day

    // Generate JWT Token
    public String generateJwtToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    // Validate JWT Token
    public boolean validateJwtToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Extract Username from JWT Token
    public String getUserNameFromJwtToken(String token) {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token);
        return jwt.getSubject();
    }

}

package com.hikmethankolay.user_auth_system.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;


@Component
public class JwtUtils {

    @Value("${api.security.token.secret}")
    private String jwtSecret;

    @Value("${api.security.token.expiration}")
    private int jwtExpirationMs;

    public String generateJwtToken(String userId, String username) {
        return JWT.create()
                .withSubject(userId)
                .withClaim("username", username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    public boolean validateJwtToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserIdFromJwtToken(String token) {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token);
        return Long.valueOf(jwt.getSubject());
    }

    public String getUserNameFromJwtToken(String token) {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token);
        return jwt.getClaim("username").asString();
    }

}

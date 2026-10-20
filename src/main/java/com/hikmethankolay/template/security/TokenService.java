package com.hikmethankolay.template.security;

import com.hikmethankolay.template.config.AppProperties;
import com.hikmethankolay.template.user.Role;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Issues signed JWTs and reads the application's custom claims.
 *
 * <p>Token validation is not done here but by Spring Security's resource server
 * (see {@link SecurityConfig#jwtDecoder(AppProperties, TokenRevocationService)}).</p>
 */
@Service
public class TokenService {

    /** Claim holding the user's roles, mapped to {@code ROLE_*} authorities. */
    public static final String ROLES_CLAIM = "roles";

    /** Claim holding the username, for clients that want to display it without an extra request. */
    public static final String USERNAME_CLAIM = "username";

    /** Signs the tokens. */
    private final JwtEncoder jwtEncoder;

    /** Tells whether a revocation cut-off is active for the user. */
    private final TokenRevocationService revocations;

    /** Issuer and lifetime settings. */
    private final AppProperties.Jwt properties;

    /**
     * Creates the service.
     *
     * @param jwtEncoder  signs the tokens
     * @param revocations tells whether a revocation cut-off is active for the user
     * @param properties  application properties holding issuer and lifetimes
     */
    public TokenService(JwtEncoder jwtEncoder, TokenRevocationService revocations, AppProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.revocations = revocations;
        this.properties = properties.jwt();
    }

    /**
     * Issues a short-lived access token for a user. Each token gets a unique id ({@code jti}) so
     * it can be revoked.
     *
     * <p>If the user's tokens were revoked in the current second, the issue time is moved to the
     * cut-off (less than a second ahead), so the new token is not revoked with the old ones.</p>
     *
     * @param userId   database id, stored as the {@code sub} claim
     * @param username username, stored as the {@code username} claim
     * @param role     role, stored in the {@code roles} claim
     * @return the signed token with its lifetime
     */
    public IssuedToken issue(long userId, String username, Role role) {
        Instant now = Instant.now();
        Duration ttl = properties.accessTokenTtl();
        Instant issuedAt =
                revocations.issuedNotBefore(userId).filter(now::isBefore).orElse(now);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(properties.issuer())
                .subject(Long.toString(userId))
                .issuedAt(issuedAt)
                .expiresAt(now.plus(ttl))
                .claim(USERNAME_CLAIM, username)
                .claim(ROLES_CLAIM, List.of(role.name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String value =
                jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(value, ttl);
    }

    /**
     * Reads the user id from a validated token.
     *
     * @param jwt the authenticated principal
     * @return the user id stored in the {@code sub} claim
     */
    public static long userId(Jwt jwt) {
        return Long.parseLong(jwt.getSubject());
    }

    /**
     * A freshly issued token.
     *
     * @param value the compact, signed JWT
     * @param ttl   how long the token is valid
     */
    public record IssuedToken(String value, Duration ttl) {}
}

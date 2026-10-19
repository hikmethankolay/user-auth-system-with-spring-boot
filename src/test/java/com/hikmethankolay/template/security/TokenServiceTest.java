package com.hikmethankolay.template.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hikmethankolay.template.TestProperties;
import com.hikmethankolay.template.config.AppProperties;
import com.hikmethankolay.template.user.Role;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

class TokenServiceTest {

    private final SecurityConfig securityConfig = new SecurityConfig();
    private final AppProperties properties = TestProperties.create();
    private final TokenRevocationService revocations = mock(TokenRevocationService.class);
    private final TokenService tokenService = new TokenService(securityConfig.jwtEncoder(properties), revocations, properties);
    private final JwtDecoder decoder = securityConfig.jwtDecoder(properties, revocations);

    @Test
    void issuedTokenCarriesUserClaims() {
        TokenService.IssuedToken token = tokenService.issue(42L, "alice", Role.ADMIN);

        Jwt jwt = decoder.decode(token.value());
        assertThat(jwt.getId()).isNotBlank();
        assertThat(TokenService.userId(jwt)).isEqualTo(42L);
        assertThat(jwt.getClaimAsString(TokenService.USERNAME_CLAIM)).isEqualTo("alice");
        assertThat(jwt.getClaimAsStringList(TokenService.ROLES_CLAIM)).isEqualTo(List.of("ADMIN"));
        assertThat(jwt.getClaimAsString("iss")).isEqualTo(TestProperties.ISSUER);
        assertThat(token.ttl()).isEqualTo(Duration.ofMinutes(15));
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        AppProperties other = TestProperties.withJwt(new AppProperties.Jwt(
                "another-secret-that-is-also-32-chars-long", TestProperties.ISSUER, Duration.ofMinutes(15), Duration.ofDays(1), Duration.ofDays(30)));
        String foreignToken = new TokenService(securityConfig.jwtEncoder(other), revocations, other)
                .issue(1L, "mallory", Role.ADMIN).value();

        assertThatThrownBy(() -> decoder.decode(foreignToken)).isInstanceOf(JwtException.class);
    }

    @Test
    void revokedTokenIsRejected() {
        String value = tokenService.issue(1L, "bob", Role.USER).value();
        String tokenId = decoder.decode(value).getId();
        when(revocations.isRevoked(argThat((Jwt jwt) -> tokenId.equals(jwt.getId())))).thenReturn(true);

        assertThatThrownBy(() -> decoder.decode(value))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("revoked");
    }

    @Test
    void tokenIssuedRightAfterARevocationCutOffStartsAtTheCutOff() {
        Instant cutOff = Instant.now().plusSeconds(1).truncatedTo(ChronoUnit.SECONDS);
        when(revocations.issuedNotBefore(7L)).thenReturn(Optional.of(cutOff));

        Jwt jwt = decoder.decode(tokenService.issue(7L, "carol", Role.USER).value());

        assertThat(jwt.getIssuedAt()).isEqualTo(cutOff);
    }

    @Test
    void everyTokenGetsItsOwnId() {
        String first = decoder.decode(tokenService.issue(1L, "bob", Role.USER).value()).getId();
        String second = decoder.decode(tokenService.issue(1L, "bob", Role.USER).value()).getId();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void tokenFromAnotherIssuerIsRejected() {
        AppProperties other = TestProperties.withJwt(new AppProperties.Jwt(
                TestProperties.JWT_SECRET, "someone-else", Duration.ofMinutes(15), Duration.ofDays(1), Duration.ofDays(30)));
        String foreignToken = new TokenService(securityConfig.jwtEncoder(other), revocations, other)
                .issue(1L, "mallory", Role.ADMIN).value();

        assertThatThrownBy(() -> decoder.decode(foreignToken)).isInstanceOf(JwtException.class);
    }
}

package com.hikmethankolay.template.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hikmethankolay.template.TestProperties;
import com.hikmethankolay.template.config.AppProperties;
import com.hikmethankolay.template.user.Role;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

class TokenServiceTest {

    private final SecurityConfig securityConfig = new SecurityConfig();
    private final AppProperties properties = TestProperties.create();
    private final TokenRevocationService revocations = mock(TokenRevocationService.class);
    private final TokenService tokenService = new TokenService(securityConfig.jwtEncoder(properties), properties);
    private final JwtDecoder decoder = securityConfig.jwtDecoder(properties, revocations);

    @Test
    void issuedTokenCarriesUserClaims() {
        TokenService.IssuedToken token = tokenService.issue(42L, "alice", Role.ADMIN, false);

        Jwt jwt = decoder.decode(token.value());
        assertThat(jwt.getId()).isNotBlank();
        assertThat(TokenService.userId(jwt)).isEqualTo(42L);
        assertThat(jwt.getClaimAsString(TokenService.USERNAME_CLAIM)).isEqualTo("alice");
        assertThat(jwt.getClaimAsStringList(TokenService.ROLES_CLAIM)).isEqualTo(List.of("ADMIN"));
        assertThat(jwt.getClaimAsString("iss")).isEqualTo(TestProperties.ISSUER);
        assertThat(TokenService.isRememberMe(jwt)).isFalse();
        assertThat(token.ttl()).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    void rememberMeUsesLongerTtl() {
        TokenService.IssuedToken token = tokenService.issue(1L, "bob", Role.USER, true);

        Jwt jwt = decoder.decode(token.value());
        assertThat(TokenService.isRememberMe(jwt)).isTrue();
        assertThat(token.ttl()).isEqualTo(Duration.ofDays(30));
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofDays(30));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        AppProperties other = TestProperties.withJwt(new AppProperties.Jwt(
                "another-secret-that-is-also-32-chars-long", TestProperties.ISSUER, Duration.ofMinutes(15), Duration.ofDays(30)));
        String foreignToken = new TokenService(securityConfig.jwtEncoder(other), other)
                .issue(1L, "mallory", Role.ADMIN, false).value();

        assertThatThrownBy(() -> decoder.decode(foreignToken)).isInstanceOf(JwtException.class);
    }

    @Test
    void revokedTokenIsRejected() {
        String value = tokenService.issue(1L, "bob", Role.USER, false).value();
        String tokenId = decoder.decode(value).getId();
        when(revocations.isRevoked(tokenId)).thenReturn(true);

        assertThatThrownBy(() -> decoder.decode(value))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("revoked");
    }

    @Test
    void everyTokenGetsItsOwnId() {
        String first = decoder.decode(tokenService.issue(1L, "bob", Role.USER, false).value()).getId();
        String second = decoder.decode(tokenService.issue(1L, "bob", Role.USER, false).value()).getId();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void tokenFromAnotherIssuerIsRejected() {
        AppProperties other = TestProperties.withJwt(new AppProperties.Jwt(
                TestProperties.JWT_SECRET, "someone-else", Duration.ofMinutes(15), Duration.ofDays(30)));
        String foreignToken = new TokenService(securityConfig.jwtEncoder(other), other)
                .issue(1L, "mallory", Role.ADMIN, false).value();

        assertThatThrownBy(() -> decoder.decode(foreignToken)).isInstanceOf(JwtException.class);
    }
}

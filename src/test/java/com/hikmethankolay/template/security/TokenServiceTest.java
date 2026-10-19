package com.hikmethankolay.template.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    private final TokenService tokenService = new TokenService(securityConfig.jwtEncoder(properties), properties);
    private final JwtDecoder decoder = securityConfig.jwtDecoder(properties);

    @Test
    void issuedTokenCarriesUserClaims() {
        TokenService.IssuedToken token = tokenService.issue(42L, "alice", Role.ADMIN, false);

        Jwt jwt = decoder.decode(token.value());
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
        AppProperties other = new AppProperties(
                new AppProperties.Jwt("another-secret-that-is-also-32-chars-long", TestProperties.ISSUER,
                        Duration.ofMinutes(15), Duration.ofDays(30)),
                properties.cookie(), properties.cors(), properties.login(), properties.admin());
        String foreignToken = new TokenService(securityConfig.jwtEncoder(other), other)
                .issue(1L, "mallory", Role.ADMIN, false).value();

        assertThatThrownBy(() -> decoder.decode(foreignToken)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenFromAnotherIssuerIsRejected() {
        AppProperties other = new AppProperties(
                new AppProperties.Jwt(TestProperties.JWT_SECRET, "someone-else", Duration.ofMinutes(15), Duration.ofDays(30)),
                properties.cookie(), properties.cors(), properties.login(), properties.admin());
        String foreignToken = new TokenService(securityConfig.jwtEncoder(other), other)
                .issue(1L, "mallory", Role.ADMIN, false).value();

        assertThatThrownBy(() -> decoder.decode(foreignToken)).isInstanceOf(JwtException.class);
    }
}

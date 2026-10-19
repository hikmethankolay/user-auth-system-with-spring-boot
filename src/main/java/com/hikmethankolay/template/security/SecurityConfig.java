package com.hikmethankolay.template.security;

import com.hikmethankolay.template.config.AppProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Stateless JWT security configuration.
 *
 * <ul>
 *   <li>Public endpoints get their own filter chain without token processing, so a stale
 *       cookie can never block login or registration.</li>
 *   <li>Every other request needs a valid JWT, sent as a Bearer header or as the auth cookie.</li>
 *   <li>Role checks are declared on controller methods with {@code @PreAuthorize}.</li>
 * </ul>
 *
 * <p>CSRF protection is disabled because the API is stateless and the auth cookie defaults to
 * {@code SameSite=Strict}, so browsers never attach it to cross-site requests. Revisit this if
 * {@code app.cookie.same-site} is relaxed to {@code Lax} or {@code None}.</p>
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class SecurityConfig {

    /** Paths reachable without authentication. Add new public endpoints here. */
    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/logout",
            "/api/v1/auth/verify-email", "/api/v1/auth/verify-email/resend",
            "/api/v1/auth/password-reset", "/api/v1/auth/password-reset/confirm",
            "/actuator/health", "/actuator/health/**", "/actuator/info", "/actuator/prometheus",
            "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**",
            "/error"
    };

    /**
     * Filter chain for {@code PUBLIC_PATHS}: everything is permitted and no token is read.
     *
     * @param http           the builder provided by Spring Security
     * @param problemHandler renders security errors as problem details
     * @return the public filter chain, evaluated first
     * @throws Exception if the chain cannot be built
     */
    @Bean
    @Order(1)
    SecurityFilterChain publicFilterChain(HttpSecurity http, SecurityProblemHandler problemHandler) throws Exception {
        return applyCommonSettings(http.securityMatcher(PUBLIC_PATHS), problemHandler)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    /**
     * Filter chain for all other requests: a valid JWT is required (deny by default).
     *
     * @param http                the builder provided by Spring Security
     * @param problemHandler      renders security errors as problem details
     * @param bearerTokenResolver reads the token from the header or the auth cookie
     * @return the authenticated filter chain
     * @throws Exception if the chain cannot be built
     */
    @Bean
    @Order(2)
    SecurityFilterChain apiFilterChain(
            HttpSecurity http, SecurityProblemHandler problemHandler, BearerTokenResolver bearerTokenResolver) throws Exception {
        return applyCommonSettings(http, problemHandler)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .bearerTokenResolver(bearerTokenResolver)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(problemHandler)
                        .accessDeniedHandler(problemHandler))
                .build();
    }

    /**
     * Settings shared by both chains: stateless, no CSRF, no form/basic login, CORS enabled.
     *
     * @param http           the builder to configure
     * @param problemHandler renders security errors as problem details
     * @return the same builder, for chaining
     */
    private static HttpSecurity applyCommonSettings(HttpSecurity http, SecurityProblemHandler problemHandler) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(problemHandler)
                        .accessDeniedHandler(problemHandler));
    }

    /**
     * Maps the {@code roles} claim to Spring Security authorities ({@code ADMIN} becomes {@code ROLE_ADMIN}).
     *
     * @return the converter used by the resource server
     */
    private static JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(TokenService.ROLES_CLAIM);
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    /**
     * Password hashing. The delegating encoder stores the algorithm id with each hash
     * ({@code {bcrypt}...}), so the algorithm can be upgraded later without breaking old hashes.
     *
     * @return the password encoder
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Username/password authentication used by the login endpoint.
     *
     * @param userDetailsService loads users by username or email
     * @param passwordEncoder    verifies password hashes
     * @return the authentication manager
     */
    @Bean
    AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    /**
     * Signs tokens with HMAC-SHA256.
     *
     * @param properties application properties holding the secret
     * @return the JWT encoder used by {@link TokenService}
     */
    @Bean
    JwtEncoder jwtEncoder(AppProperties properties) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(signingKey(properties)));
    }

    /**
     * Reads the token from the {@code Authorization} header or the auth cookie.
     *
     * @param properties application properties holding the cookie name
     * @return the resolver used by the resource server and by logout
     */
    @Bean
    BearerTokenResolver bearerTokenResolver(AppProperties properties) {
        return new CookieBearerTokenResolver(properties.cookie().name());
    }

    /**
     * Verifies signature, expiry, issuer and revocation of incoming tokens.
     *
     * @param properties  application properties holding the secret and issuer
     * @param revocations denylist of revoked token ids
     * @return the JWT decoder used by the resource server
     */
    @Bean
    JwtDecoder jwtDecoder(AppProperties properties, TokenRevocationService revocations) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(signingKey(properties))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        OAuth2TokenValidator<Jwt> notRevoked = jwt -> revocations.isRevoked(jwt.getId())
                ? OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "The token has been revoked.", null))
                : OAuth2TokenValidatorResult.success();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.jwt().issuer()), notRevoked));
        return decoder;
    }

    /**
     * Builds the HMAC key from the configured secret.
     *
     * @param properties application properties holding the secret
     * @return the signing key
     */
    private static SecretKey signingKey(AppProperties properties) {
        return new SecretKeySpec(properties.jwt().secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /**
     * CORS policy for browser clients, restricted to {@code app.cors.allowed-origins}.
     *
     * @param properties application properties holding the allowed origins
     * @return the CORS configuration picked up by {@code http.cors()}
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource(AppProperties properties) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(properties.cors().allowedOrigins());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setExposedHeaders(List.of("Location", "Retry-After", "X-Trace-Id"));
        cors.setAllowCredentials(true);
        cors.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}

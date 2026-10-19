package com.hikmethankolay.template.auth;

import com.hikmethankolay.template.auth.dto.LoginRequest;
import com.hikmethankolay.template.auth.dto.RegisterRequest;
import com.hikmethankolay.template.auth.dto.TokenResponse;
import com.hikmethankolay.template.config.AppProperties;
import com.hikmethankolay.template.security.TokenService;
import com.hikmethankolay.template.security.TokenService.IssuedToken;
import com.hikmethankolay.template.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Duration;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Authentication endpoints under {@code /api/v1/auth}.
 *
 * <p>Tokens are returned in the body (for API clients, as in an OAuth 2.0 token response) and as an
 * HttpOnly cookie (for browsers). Remember-me tokens get a persistent cookie, others a session cookie.</p>
 *
 * <table>
 *   <caption>Endpoints</caption>
 *   <tr><th>Method</th><th>Path</th><th>Access</th><th>Success</th></tr>
 *   <tr><td>POST</td><td>/register</td><td>public</td><td>201 + Location</td></tr>
 *   <tr><td>POST</td><td>/login</td><td>public</td><td>200</td></tr>
 *   <tr><td>POST</td><td>/refresh</td><td>authenticated</td><td>200</td></tr>
 *   <tr><td>POST</td><td>/logout</td><td>public</td><td>204</td></tr>
 * </table>
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthController {

    /** Authentication business logic. */
    private final AuthService authService;

    /** Authentication cookie settings. */
    private final AppProperties.Cookie cookieProperties;

    /**
     * Creates the controller.
     *
     * @param authService authentication business logic
     * @param properties  application properties holding the cookie settings
     */
    public AuthController(AuthService authService, AppProperties properties) {
        this.authService = authService;
        this.cookieProperties = properties.cookie();
    }

    /**
     * Registers a new user.
     *
     * @param request registration data
     * @return {@code 201 Created} with the user and a {@code Location} header pointing to it
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/users/{id}")
                .buildAndExpand(user.id())
                .toUri();
        return ResponseEntity.created(location).body(user);
    }

    /**
     * Logs in with username or email and password.
     *
     * @param request     credentials and remember-me flag
     * @param httpRequest the servlet request, used for the client IP
     * @return {@code 200 OK} with the token in the body and in a cookie
     */
    @PostMapping("/login")
    @Operation(summary = "Log in and obtain a token")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        // getRemoteAddr() honours X-Forwarded-For only when server.forward-headers-strategy is framework or native.
        return tokenResponse(authService.login(request, httpRequest.getRemoteAddr()));
    }

    /**
     * Exchanges a valid token for a new one with a fresh expiry.
     *
     * @param jwt the caller's current token
     * @return {@code 200 OK} with the new token in the body and in a cookie
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh the current token")
    public ResponseEntity<TokenResponse> refresh(@AuthenticationPrincipal Jwt jwt) {
        return tokenResponse(authService.refresh(TokenService.userId(jwt), TokenService.isRememberMe(jwt)));
    }

    /**
     * Logs out a browser client by expiring the auth cookie. API clients simply discard their token.
     * The JWT itself is not revoked and stays valid until it expires.
     *
     * @return {@code 204 No Content} with an expired cookie
     */
    @PostMapping("/logout")
    @Operation(summary = "Log out (clear the auth cookie)")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, authCookie("", Duration.ZERO).toString())
                .build();
    }

    /**
     * Builds a token response; {@code Cache-Control: no-store} as required for token responses by RFC 6749.
     *
     * @param token the issued token
     * @return {@code 200 OK} with body and cookie
     */
    private ResponseEntity<TokenResponse> tokenResponse(IssuedToken token) {
        Duration cookieMaxAge = token.rememberMe() ? token.ttl() : Duration.ofSeconds(-1);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, authCookie(token.value(), cookieMaxAge).toString())
                .body(TokenResponse.bearer(token.value(), token.ttl()));
    }

    /**
     * Builds the HttpOnly authentication cookie.
     *
     * @param value  the token, or an empty string to clear the cookie
     * @param maxAge cookie lifetime; negative for a session cookie, zero to delete it
     * @return the cookie
     */
    private ResponseCookie authCookie(String value, Duration maxAge) {
        return ResponseCookie.from(cookieProperties.name(), value)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}

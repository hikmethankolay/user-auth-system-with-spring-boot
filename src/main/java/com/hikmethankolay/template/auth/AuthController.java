package com.hikmethankolay.template.auth;

import com.hikmethankolay.template.auth.dto.LoginRequest;
import com.hikmethankolay.template.auth.dto.RefreshRequest;
import com.hikmethankolay.template.auth.dto.RegisterRequest;
import com.hikmethankolay.template.auth.dto.TokenResponse;
import com.hikmethankolay.template.common.ApiDocs;
import com.hikmethankolay.template.config.AppProperties;
import com.hikmethankolay.template.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Duration;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.WebUtils;

/**
 * Authentication endpoints under {@code /api/v1/auth}.
 *
 * <p>Login and refresh return a short-lived access token and a single-use refresh token, both in the
 * body (for API clients, as in an OAuth 2.0 token response) and as HttpOnly cookies (for browsers).
 * The refresh cookie is only sent to {@code /api/v1/auth}. Remember-me sessions get persistent
 * cookies, others session cookies.</p>
 *
 * <table>
 *   <caption>Endpoints</caption>
 *   <tr><th>Method</th><th>Path</th><th>Access</th><th>Success</th></tr>
 *   <tr><td>POST</td><td>/register</td><td>public</td><td>201 + Location</td></tr>
 *   <tr><td>POST</td><td>/login</td><td>public</td><td>200</td></tr>
 *   <tr><td>POST</td><td>/refresh</td><td>refresh token</td><td>200</td></tr>
 *   <tr><td>POST</td><td>/logout</td><td>public</td><td>204</td></tr>
 * </table>
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthController {

    /** Authentication business logic. */
    private final AuthService authService;

    /** Description of the {@code Set-Cookie} headers on token responses. */
    private static final String SET_COOKIES = "The access token cookie (path /) and the refresh token cookie (path /api/v1/auth)";

    /** Authentication cookie settings. */
    private final AppProperties.Cookie cookieProperties;

    /** Reads the token presented at logout. */
    private final BearerTokenResolver bearerTokenResolver;

    /**
     * Creates the controller.
     *
     * @param authService         authentication business logic
     * @param properties          application properties holding the cookie settings
     * @param bearerTokenResolver reads the token from the header or the auth cookie
     */
    public AuthController(AuthService authService, AppProperties properties, BearerTokenResolver bearerTokenResolver) {
        this.authService = authService;
        this.cookieProperties = properties.cookie();
        this.bearerTokenResolver = bearerTokenResolver;
    }

    /**
     * Registers a new user.
     *
     * @param request registration data
     * @return {@code 201 Created} with the user and a {@code Location} header pointing to it
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "registerUser", summary = "Register a new user",
            description = "Creates an account with the USER role and sends a verification email.")
    @ApiResponse(responseCode = "201", description = "User created",
            headers = @Header(name = HttpHeaders.LOCATION, description = "URL of the new user",
                    schema = @Schema(type = "string", format = "uri")),
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "409", description = "Username or email already taken",
            content = @Content(mediaType = ApiDocs.PROBLEM_JSON, schema = @Schema(ref = ApiDocs.PROBLEM_SCHEMA)))
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
     * @return {@code 200 OK} with the tokens in the body and in cookies
     */
    @PostMapping("/login")
    @Operation(operationId = "login", summary = "Log in and obtain tokens",
            description = "Accepts a username or an email address. Repeated failures lock the IP and the account (429).")
    @ApiResponse(responseCode = "200", description = "Logged in; the tokens are also set as HttpOnly cookies",
            headers = @Header(name = HttpHeaders.SET_COOKIE, description = SET_COOKIES, schema = @Schema(type = "string")),
            content = @Content(schema = @Schema(implementation = TokenResponse.class)))
    @ApiResponse(responseCode = "401", description = "Wrong username, email or password",
            content = @Content(mediaType = ApiDocs.PROBLEM_JSON, schema = @Schema(ref = ApiDocs.PROBLEM_SCHEMA)))
    @ApiResponse(responseCode = "403", description = "Email address not verified (when verification is required)",
            content = @Content(mediaType = ApiDocs.PROBLEM_JSON, schema = @Schema(ref = ApiDocs.PROBLEM_SCHEMA)))
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        // getRemoteAddr() honours X-Forwarded-For only when server.forward-headers-strategy is framework or native.
        return tokenResponse(authService.login(request, httpRequest.getRemoteAddr()));
    }

    /**
     * Exchanges a refresh token for new tokens. The presented refresh token can no longer be used;
     * presenting it again ends the session.
     *
     * @param request     optional body carrying the refresh token; without it the refresh cookie is used
     * @param httpRequest the servlet request carrying the refresh cookie
     * @return {@code 200 OK} with the new tokens in the body and in cookies
     */
    @PostMapping("/refresh")
    @Operation(operationId = "refreshTokens", summary = "Exchange a refresh token for new tokens",
            description = "Takes the refresh token from the body or the refresh token cookie. Each refresh token works "
                    + "once; presenting a used one ends the whole session.")
    @ApiResponse(responseCode = "200", description = "New tokens; also set as HttpOnly cookies",
            headers = @Header(name = HttpHeaders.SET_COOKIE, description = SET_COOKIES, schema = @Schema(type = "string")),
            content = @Content(schema = @Schema(implementation = TokenResponse.class)))
    @ApiResponse(responseCode = "401", description = "Refresh token missing, invalid, expired or already used",
            content = @Content(mediaType = ApiDocs.PROBLEM_JSON, schema = @Schema(ref = ApiDocs.PROBLEM_SCHEMA)))
    public ResponseEntity<TokenResponse> refresh(
            @RequestBody(required = false) RefreshRequest request, HttpServletRequest httpRequest) {
        return tokenResponse(authService.refresh(refreshToken(request, httpRequest)));
    }

    /**
     * Logs out: revokes the access token (header or cookie) and the refresh token's session (body or
     * cookie), and expires both cookies. Calling it without tokens, or with invalid ones, still succeeds.
     *
     * @param request     optional body carrying the refresh token
     * @param httpRequest the servlet request carrying the tokens
     * @return {@code 204 No Content} with expired cookies
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "logout", summary = "Log out (revoke the tokens and clear the auth cookies)")
    @ApiResponse(responseCode = "204", description = "Logged out, also when no or invalid tokens were sent",
            headers = @Header(name = HttpHeaders.SET_COOKIE, description = "Expires both auth cookies",
                    schema = @Schema(type = "string")))
    public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshRequest request, HttpServletRequest httpRequest) {
        String accessToken;
        try {
            accessToken = bearerTokenResolver.resolve(httpRequest);
        } catch (AuthenticationException ex) {
            accessToken = null; // Malformed Authorization header: nothing to revoke.
        }
        authService.logout(accessToken, refreshToken(request, httpRequest));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessCookie("", Duration.ZERO).toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie("", Duration.ZERO).toString())
                .build();
    }

    /**
     * Reads the refresh token from the body, falling back to the refresh cookie.
     *
     * @param request     the optional body
     * @param httpRequest the servlet request
     * @return the refresh token, or {@code null} if neither carries one
     */
    private String refreshToken(RefreshRequest request, HttpServletRequest httpRequest) {
        if (request != null && request.refreshToken() != null) {
            return request.refreshToken();
        }
        Cookie cookie = WebUtils.getCookie(httpRequest, cookieProperties.refreshName());
        return cookie != null ? cookie.getValue() : null;
    }

    /**
     * Builds a token response; {@code Cache-Control: no-store} as required for token responses by RFC 6749.
     *
     * @param tokens the issued tokens
     * @return {@code 200 OK} with body and cookies
     */
    private ResponseEntity<TokenResponse> tokenResponse(AuthTokens tokens) {
        boolean persistent = tokens.refreshToken().rememberMe();
        Duration session = Duration.ofSeconds(-1);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, accessCookie(
                        tokens.accessToken().value(), persistent ? tokens.accessToken().ttl() : session).toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie(
                        tokens.refreshToken().value(), persistent ? tokens.refreshToken().ttl() : session).toString())
                .body(TokenResponse.bearer(
                        tokens.accessToken().value(), tokens.accessToken().ttl(), tokens.refreshToken().value()));
    }

    /**
     * Builds the HttpOnly access token cookie, sent with every request.
     *
     * @param value  the token, or an empty string to clear the cookie
     * @param maxAge cookie lifetime; negative for a session cookie, zero to delete it
     * @return the cookie
     */
    private ResponseCookie accessCookie(String value, Duration maxAge) {
        return cookie(cookieProperties.name(), value, "/", maxAge);
    }

    /**
     * Builds the HttpOnly refresh token cookie, sent only to the authentication endpoints.
     *
     * @param value  the token, or an empty string to clear the cookie
     * @param maxAge cookie lifetime; negative for a session cookie, zero to delete it
     * @return the cookie
     */
    private ResponseCookie refreshCookie(String value, Duration maxAge) {
        return cookie(cookieProperties.refreshName(), value, "/api/v1/auth", maxAge);
    }

    /**
     * Builds an HttpOnly cookie with the configured {@code Secure} and {@code SameSite} attributes.
     *
     * @param name   cookie name
     * @param value  cookie value
     * @param path   paths the browser sends the cookie to
     * @param maxAge cookie lifetime; negative for a session cookie, zero to delete it
     * @return the cookie
     */
    private ResponseCookie cookie(String name, String value, String path, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path(path)
                .maxAge(maxAge)
                .build();
    }
}

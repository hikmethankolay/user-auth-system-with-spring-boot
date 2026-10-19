package com.hikmethankolay.template.auth;

import com.hikmethankolay.template.audit.AuditEvent;
import com.hikmethankolay.template.audit.AuditLog;
import com.hikmethankolay.template.auth.dto.LoginRequest;
import com.hikmethankolay.template.auth.dto.RegisterRequest;
import com.hikmethankolay.template.common.ApiException;
import com.hikmethankolay.template.config.AppProperties;
import com.hikmethankolay.template.security.AppUserDetails;
import com.hikmethankolay.template.security.LoginAttemptService;
import com.hikmethankolay.template.security.TokenRevocationService;
import com.hikmethankolay.template.security.TokenService;
import com.hikmethankolay.template.user.Role;
import com.hikmethankolay.template.user.UserRepository;
import com.hikmethankolay.template.user.UserService;
import com.hikmethankolay.template.user.dto.UserResponse;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

/**
 * Registration, login, token refresh and logout.
 *
 * <p>Login returns a short-lived access token (JWT) and a refresh token; refresh exchanges the
 * refresh token for a new pair (see {@link RefreshTokenService} for rotation and reuse detection).</p>
 */
@Service
public class AuthService {

    /** Verifies username/email and password. */
    private final AuthenticationManager authenticationManager;

    /** Brute-force protection. */
    private final LoginAttemptService loginAttempts;

    /** Issues JWTs. */
    private final TokenService tokenService;

    /** Creates users on registration. */
    private final UserService userService;

    /** Reloads users on token refresh. */
    private final UserRepository userRepository;

    /** Issues and rotates refresh tokens. */
    private final RefreshTokenService refreshTokens;

    /** Revokes access tokens on logout. */
    private final TokenRevocationService revocations;

    /** Validates the token presented at logout. */
    private final JwtDecoder jwtDecoder;

    /** Whether login requires a verified email address. */
    private final boolean requireVerifiedEmail;

    /** Records security events. */
    private final AuditLog audit;

    /**
     * Creates the service.
     *
     * @param authenticationManager verifies credentials
     * @param loginAttempts         brute-force protection
     * @param tokenService          issues JWTs
     * @param userService           creates users
     * @param userRepository        reloads users on refresh
     * @param refreshTokens         issues and rotates refresh tokens
     * @param revocations           denylist of revoked access tokens
     * @param jwtDecoder            validates the token presented at logout
     * @param properties            application properties (email verification policy)
     * @param audit                 records security events
     */
    public AuthService(
            AuthenticationManager authenticationManager,
            LoginAttemptService loginAttempts,
            TokenService tokenService,
            UserService userService,
            UserRepository userRepository,
            RefreshTokenService refreshTokens,
            TokenRevocationService revocations,
            JwtDecoder jwtDecoder,
            AppProperties properties,
            AuditLog audit) {
        this.authenticationManager = authenticationManager;
        this.loginAttempts = loginAttempts;
        this.tokenService = tokenService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.refreshTokens = refreshTokens;
        this.revocations = revocations;
        this.jwtDecoder = jwtDecoder;
        this.requireVerifiedEmail = properties.account().requireVerifiedEmail();
        this.audit = audit;
    }

    /**
     * Registers a new user with the {@link Role#USER} role and sends a verification email.
     *
     * @param request registration data
     * @return the created user
     * @throws ApiException 409 if the username or email is already taken
     */
    public UserResponse register(RegisterRequest request) {
        return userService.register(request.username(), request.email(), request.password(), Role.USER, false);
    }

    /**
     * Verifies credentials and starts a session.
     *
     * <p>Failures are counted per client IP and per account. Once either is blocked, logins are
     * rejected with 429 until the lock expires, even with the correct password.</p>
     *
     * @param request  username or email, password and remember-me flag
     * @param clientIp address of the client, used for rate limiting
     * @return the access and refresh tokens
     * @throws ApiException            429 if the IP or account is blocked
     * @throws BadCredentialsException if the credentials are wrong (mapped to 401)
     */
    public AuthTokens login(LoginRequest request, String clientIp) {
        String identifier = request.identifier().trim();
        String accountKey = "account:" + identifier.toLowerCase(Locale.ROOT);
        String ipKey = "ip:" + clientIp;

        if (loginAttempts.isBlocked(ipKey) || loginAttempts.isBlocked(accountKey)) {
            audit.record(AuditEvent.LOGIN_BLOCKED, null, "identifier", identifier);
            throw ApiException.tooManyRequests(
                    "Too many failed login attempts. Please try again later.", loginAttempts.lockDuration());
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(identifier, request.password()));
        } catch (AuthenticationException ex) {
            loginAttempts.recordFailure(ipKey, accountKey);
            audit.record(AuditEvent.LOGIN_FAILED, null, "identifier", identifier);
            throw new BadCredentialsException("Invalid username or password.");
        }

        // Only the account counter is reset: one valid login must not clear the IP's failures.
        loginAttempts.reset(accountKey);
        AppUserDetails user = (AppUserDetails) authentication.getPrincipal();
        // Checked only after the password, so the response does not reveal whether an account exists.
        if (requireVerifiedEmail && !user.emailVerified()) {
            audit.record(AuditEvent.LOGIN_UNVERIFIED, user.id());
            throw new ApiException(HttpStatus.FORBIDDEN, "Email address is not verified.");
        }
        audit.record(AuditEvent.LOGIN_SUCCEEDED, user.id(), "rememberMe", request.isRememberMe());
        return new AuthTokens(
                tokenService.issue(user.id(), user.username(), user.role()),
                refreshTokens.issue(user.id(), request.isRememberMe()));
    }

    /**
     * Exchanges a refresh token for a new access token and refresh token. The access token picks
     * up role and username changes since the last login.
     *
     * @param refreshToken the refresh token presented by the client, may be {@code null}
     * @return the new tokens
     * @throws BadCredentialsException if the refresh token is missing, invalid, expired or reused (mapped to 401)
     */
    public AuthTokens refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadCredentialsException(RefreshTokenService.INVALID_REFRESH_TOKEN);
        }
        RefreshTokenService.Rotation rotation = refreshTokens.rotate(refreshToken);
        // The user cannot be missing: deleting a user deletes its refresh tokens.
        return userRepository.findById(rotation.userId())
                .map(user -> new AuthTokens(
                        tokenService.issue(user.getId(), user.getUsername(), user.getRole()), rotation.successor()))
                .orElseThrow(() -> new BadCredentialsException(RefreshTokenService.INVALID_REFRESH_TOKEN));
    }

    /**
     * Ends a session: revokes the access token and the refresh token's session. Invalid, expired
     * or missing tokens are ignored: there is nothing left to revoke.
     *
     * @param accessToken  the raw access token from the header or cookie, may be {@code null}
     * @param refreshToken the refresh token from the body or cookie, may be {@code null}
     */
    public void logout(String accessToken, String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokens.revokeSession(refreshToken);
        }
        if (accessToken == null) {
            return;
        }
        try {
            Jwt jwt = jwtDecoder.decode(accessToken);
            revocations.revoke(jwt);
            audit.record(AuditEvent.LOGOUT, TokenService.userId(jwt));
        } catch (JwtException ex) {
            // Already invalid, expired or revoked.
        }
    }
}

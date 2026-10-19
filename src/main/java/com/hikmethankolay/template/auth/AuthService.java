package com.hikmethankolay.template.auth;

import com.hikmethankolay.template.auth.dto.LoginRequest;
import com.hikmethankolay.template.auth.dto.RegisterRequest;
import com.hikmethankolay.template.common.ApiException;
import com.hikmethankolay.template.security.AppUserDetails;
import com.hikmethankolay.template.security.LoginAttemptService;
import com.hikmethankolay.template.security.TokenService;
import com.hikmethankolay.template.security.TokenService.IssuedToken;
import com.hikmethankolay.template.user.Role;
import com.hikmethankolay.template.user.UserRepository;
import com.hikmethankolay.template.user.UserService;
import com.hikmethankolay.template.user.dto.UserResponse;
import java.util.Locale;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * Registration, login and token refresh.
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

    /**
     * Creates the service.
     *
     * @param authenticationManager verifies credentials
     * @param loginAttempts         brute-force protection
     * @param tokenService          issues JWTs
     * @param userService           creates users
     * @param userRepository        reloads users on refresh
     */
    public AuthService(
            AuthenticationManager authenticationManager,
            LoginAttemptService loginAttempts,
            TokenService tokenService,
            UserService userService,
            UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.loginAttempts = loginAttempts;
        this.tokenService = tokenService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * Registers a new user with the {@link Role#USER} role.
     *
     * @param request registration data
     * @return the created user
     * @throws ApiException 409 if the username or email is already taken
     */
    public UserResponse register(RegisterRequest request) {
        return userService.register(request.username(), request.email(), request.password(), Role.USER);
    }

    /**
     * Verifies credentials and issues a token.
     *
     * <p>Failures are counted per client IP and per account. Once either is blocked, logins are
     * rejected with 429 until the lock expires, even with the correct password.</p>
     *
     * @param request  username or email, password and remember-me flag
     * @param clientIp address of the client, used for rate limiting
     * @return the issued token
     * @throws ApiException            429 if the IP or account is blocked
     * @throws BadCredentialsException if the credentials are wrong (mapped to 401)
     */
    public IssuedToken login(LoginRequest request, String clientIp) {
        String identifier = request.identifier().trim();
        String accountKey = "account:" + identifier.toLowerCase(Locale.ROOT);
        String ipKey = "ip:" + clientIp;

        if (loginAttempts.isBlocked(ipKey) || loginAttempts.isBlocked(accountKey)) {
            throw ApiException.tooManyRequests(
                    "Too many failed login attempts. Please try again later.", loginAttempts.lockDuration());
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(identifier, request.password()));
        } catch (AuthenticationException ex) {
            loginAttempts.recordFailure(ipKey, accountKey);
            throw new BadCredentialsException("Invalid username or password.");
        }

        // Only the account counter is reset: one valid login must not clear the IP's failures.
        loginAttempts.reset(accountKey);
        AppUserDetails user = (AppUserDetails) authentication.getPrincipal();
        return tokenService.issue(user.id(), user.username(), user.role(), request.isRememberMe());
    }

    /**
     * Issues a fresh token for an authenticated user, picking up role changes since the last login.
     *
     * @param userId     id from the current token
     * @param rememberMe remember-me flag from the current token, preserved in the new one
     * @return the new token
     * @throws BadCredentialsException if the user was deleted (mapped to 401)
     */
    public IssuedToken refresh(long userId, boolean rememberMe) {
        return userRepository.findById(userId)
                .map(user -> tokenService.issue(user.getId(), user.getUsername(), user.getRole(), rememberMe))
                .orElseThrow(() -> new BadCredentialsException("User no longer exists."));
    }
}

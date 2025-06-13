/**
 * @file AuthController.java
 * @brief Controller for user authentication.
 * This controller provides endpoints for user registration and authentication.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.controller
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.controller;

import com.hikmethankolay.user_auth_system.dto.*;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.EApiStatus;
import com.hikmethankolay.user_auth_system.service.UserService;
import com.hikmethankolay.user_auth_system.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * @class AuthController
 * @brief REST controller for handling authentication requests.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** User service for handling authentication logic. */
    private final UserService userService;

    /** JWT utilities for token operations. */
    private final JwtUtils jwtUtils;

    /**
     * @brief Constructor for AuthController.
     * @param userService The user service instance.
     * @param jwtUtils The JWT utility instance.
     */
    public AuthController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * @brief Handles user registration.
     * @param registerRequest The user registration request data.
     * @return Response entity containing the registration result.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<UserDTO>> register(
            @Validated(UserDTO.Registration.class) @RequestBody UserDTO registerRequest) {
        try {
            User registeredUser = userService.registerUser(registerRequest);
            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, new UserDTO(registeredUser), "User registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO<>(EApiStatus.FAILURE, null, e.getMessage()));
        }
    }

    /**
     * @brief Handles user login with Remember Me support and brute force protection.
     * @param request The HTTP request used to get client IP.
     * @param loginRequest The login request containing credentials and Remember Me preference.
     * @return Response entity with token and optional cookie or error message.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> login(HttpServletRequest request, @RequestBody LoginRequestDTO loginRequest) {
        // Get client IP address for rate limiting
        String clientIp = getClientIp(request);

        try {
            // Authentication is performed in the service layer
            String token = userService.authenticateUser(loginRequest, clientIp);

            if (token != null) {
                // Login successful
                AuthResponseDTO responseDTO = new AuthResponseDTO(token);
                ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();

                // If Remember Me is enabled, set a persistent cookie
                if (loginRequest.rememberMe()) {
                    ResponseCookie authCookie = ResponseCookie.from("auth_token", token)
                            .httpOnly(true)
                            .secure(false) // Enable in production with HTTPS
                            .path("/")
                            .maxAge(Duration.ofDays(30))
                            .sameSite("Strict")
                            .build();

                    responseBuilder.header(HttpHeaders.SET_COOKIE, authCookie.toString());
                }

                return responseBuilder.body(new ApiResponseDTO<>(EApiStatus.SUCCESS, responseDTO, "User authenticated successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponseDTO<>(EApiStatus.FAILURE, null, "Wrong username or password"));
            }
        } catch (RuntimeException e) {
            // Handle account/IP blocking errors with appropriate status code
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ApiResponseDTO<>(EApiStatus.FAILURE, null, e.getMessage()));
        }
    }

    /**
     * @brief Extracts the client IP address from the request.
     * @param request The HTTP request.
     * @return The client IP address.
     */
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    /**
     * @brief Handles user logout by clearing cookies.
     * @return Response entity with success message.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDTO<Void>> logout() {
        // Clear the auth cookie by setting its max age to 0
        ResponseCookie clearCookie = ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(new ApiResponseDTO<>(EApiStatus.SUCCESS, null, "Logged out successfully"));
    }

    /**
     * @brief Refreshes an authentication token.
     * @param request The HTTP request containing the current token.
     * @return Response entity with a new token.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> refreshToken(HttpServletRequest request) {
        try {
            // Token validation and refresh handled in service layer
            String token = jwtUtils.extractTokenFromRequest(request);

            // Service handles validating the token and creating new one
            String newToken = userService.refreshToken(token);
            boolean wasRememberMe = jwtUtils.wasRememberMe(token);

            if (newToken != null) {
                AuthResponseDTO responseDTO = new AuthResponseDTO(newToken);
                ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();

                if (wasRememberMe) {
                    ResponseCookie authCookie = ResponseCookie.from("auth_token", newToken)
                            .httpOnly(true)
                            .secure(true)
                            .path("/")
                            .maxAge(Duration.ofDays(30))
                            .sameSite("Strict")
                            .build();

                    responseBuilder.header(HttpHeaders.SET_COOKIE, authCookie.toString());
                }

                return responseBuilder.body(
                        new ApiResponseDTO<>(EApiStatus.SUCCESS, responseDTO, "Token refreshed successfully")
                );
            }
        } catch (Exception e) {
            // Token processing failed, handled by the catch block
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponseDTO<>(EApiStatus.UNAUTHORIZED, null, "Invalid or expired token"));
    }
}
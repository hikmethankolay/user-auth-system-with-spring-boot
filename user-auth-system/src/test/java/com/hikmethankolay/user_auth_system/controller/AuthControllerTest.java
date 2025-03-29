/**
 * @file AuthControllerTest.java
 * @brief Tests for the AuthController class.
 *
 * Contains unit tests for the authentication controller endpoints including
 * register, login, logout, and token refresh operations.
 *
 * @author Test Suite Generator
 * @date 2025-03-29
 */
package com.hikmethankolay.user_auth_system.controller;

import com.hikmethankolay.user_auth_system.dto.LoginRequestDTO;
import com.hikmethankolay.user_auth_system.dto.UserRegisterDTO;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.EApiStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @class AuthControllerTest
 * @brief Test class for AuthController.
 *
 * This class contains unit tests for authentication operations.
 */
public class AuthControllerTest extends BaseControllerTest {

    /**
     * @brief Test successful user registration.
     *
     * Verifies that the register endpoint correctly processes valid registration data.
     */
    @Test
    public void testRegisterSuccess() throws Exception {
        // Arrange
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("testuser123");
        registerDTO.setEmail("test@example.com");
        registerDTO.setPassword("P@ssw0rd123!");

        User registeredUser = new User(registerDTO.getUsername(), registerDTO.getEmail(), "encodedPassword");
        registeredUser.setId(1L);

        when(userService.registerUser(any(UserRegisterDTO.class))).thenReturn(registeredUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser123"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    /**
     * @brief Test registration with already existing username or email.
     *
     * Verifies that the register endpoint correctly handles duplicate username/email.
     */
    @Test
    public void testRegisterUserAlreadyExists() throws Exception {
        // Arrange
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("existinguser");
        registerDTO.setEmail("existing@example.com");
        registerDTO.setPassword("P@ssw0rd123!");

        when(userService.registerUser(any(UserRegisterDTO.class)))
                .thenThrow(new RuntimeException("Username is already taken!"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.name()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Username is already taken!"));
    }

    /**
     * @brief Test successful user login.
     *
     * Verifies that the login endpoint correctly processes valid credentials.
     */
    @Test
    public void testLoginSuccess() throws Exception {
        // Arrange
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("testuser", "password", false);
        String token = "valid.jwt.token";

        when(userService.authenticateUser(any(LoginRequestDTO.class), anyString())).thenReturn(token);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.data.token").value(token))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.message").value("User authenticated successfully"))
                .andExpect(cookie().doesNotExist("auth_token"));
    }

    /**
     * @brief Test login with Remember Me enabled.
     *
     * Verifies that the login endpoint sets cookie with token when Remember Me is enabled.
     */
    @Test
    public void testLoginWithRememberMe() throws Exception {
        // Arrange
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("testuser", "password", true);
        String token = "valid.jwt.token";

        when(userService.authenticateUser(any(LoginRequestDTO.class), anyString())).thenReturn(token);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.data.token").value(token))
                .andExpect(jsonPath("$.message").value("User authenticated successfully"))
                .andExpect(cookie().exists("auth_token"))
                .andExpect(cookie().httpOnly("auth_token", true));
    }

    /**
     * @brief Test login with invalid credentials.
     *
     * Verifies that the login endpoint correctly handles invalid login credentials.
     */
    @Test
    public void testLoginInvalidCredentials() throws Exception {
        // Arrange
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("wrong", "credentials", false);

        when(userService.authenticateUser(any(LoginRequestDTO.class), anyString())).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.name()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Wrong username or password"));
    }

    /**
     * @brief Test login with too many failed attempts.
     *
     * Verifies that the login endpoint correctly handles blocked accounts due to too many attempts.
     */
    @Test
    public void testLoginTooManyAttempts() throws Exception {
        // Arrange
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("blocked", "user", false);

        when(userService.authenticateUser(any(LoginRequestDTO.class), anyString()))
                .thenThrow(new RuntimeException("Account is temporarily locked due to too many failed login attempts"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.name()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Account is temporarily locked due to too many failed login attempts"));
    }

    /**
     * @brief Test successful logout.
     *
     * Verifies that the logout endpoint clears authentication cookies.
     */
    @Test
    public void testLogout() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
                .andExpect(cookie().exists("auth_token"))
                .andExpect(cookie().maxAge("auth_token", 0));
    }

    /**
     * @brief Test successful token refresh.
     *
     * Verifies that the refresh token endpoint correctly generates new tokens.
     */
    @Test
    public void testRefreshTokenSuccess() throws Exception {
        // Arrange
        String oldToken = "old.jwt.token";
        String newToken = "new.jwt.token";

        when(jwtUtils.extractTokenFromRequest(any())).thenReturn(oldToken);
        when(userService.refreshToken(oldToken)).thenReturn(newToken);
        when(jwtUtils.wasRememberMe(oldToken)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.data.token").value(newToken))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"));
    }

    /**
     * @brief Test token refresh with invalid token.
     *
     * Verifies that the refresh token endpoint correctly handles invalid tokens.
     */
    @Test
    public void testRefreshTokenInvalid() throws Exception {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        when(jwtUtils.extractTokenFromRequest(any())).thenReturn(invalidToken);
        when(userService.refreshToken(invalidToken)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(EApiStatus.UNAUTHORIZED.name()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }

    /**
     * @brief Test refresh token with Remember Me.
     *
     * Verifies that the refresh token endpoint correctly handles Remember Me settings.
     */
    @Test
    public void testRefreshTokenWithRememberMe() throws Exception {
        // Arrange
        String oldToken = "old.jwt.token";
        String newToken = "new.jwt.token";

        when(jwtUtils.extractTokenFromRequest(any())).thenReturn(oldToken);
        when(userService.refreshToken(oldToken)).thenReturn(newToken);
        when(jwtUtils.wasRememberMe(oldToken)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.data.token").value(newToken))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(cookie().exists("auth_token"))
                .andExpect(cookie().httpOnly("auth_token", true));
    }
}
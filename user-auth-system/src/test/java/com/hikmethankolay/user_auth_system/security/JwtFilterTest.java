/**
 * @file JwtFilterTest.java
 * @brief Tests for the JwtFilter class.
 *
 * Contains unit tests for JWT authentication filter.
 *
 * @author Test Suite Generator
 * @date 2025-03-29
 */
package com.hikmethankolay.user_auth_system.security;

import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.ERole;
import com.hikmethankolay.user_auth_system.enums.TokenStatus;
import com.hikmethankolay.user_auth_system.service.UserService;
import com.hikmethankolay.user_auth_system.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @class JwtFilterTest
 * @brief Test class for JwtFilter.
 *
 * This class contains unit tests for JWT authentication filter operations.
 */
@SpringBootTest
public class JwtFilterTest {

    /**
     * JwtFilter instance to be tested, with mocked dependencies injected.
     */
    @Autowired
    private JwtFilter jwtFilter;

    /**
     * Mock JwtUtils for filter dependencies.
     */
    @MockitoBean
    private JwtUtils jwtUtils;

    /**
     * Mock UserService for filter dependencies.
     */
    @MockitoBean
    private UserService userService;

    /**
     * Mock HttpServletRequest for testing.
     */
    @MockitoBean
    private HttpServletRequest request;

    /**
     * Mock HttpServletResponse for testing.
     */
    @MockitoBean
    private HttpServletResponse response;

    /**
     * Mock FilterChain for testing.
     */
    @MockitoBean
    private FilterChain filterChain;

    /**
     * @brief Setup method that runs before each test.
     *
     * Clears the security context to ensure tests start with a clean state.
     */
    @BeforeEach
    public void setup() {
        SecurityContextHolder.clearContext();
    }

    /**
     * @brief Test filtering with valid JWT token.
     *
     * Verifies that the filter correctly sets authentication with valid JWT token.
     */
    @Test
    public void testDoFilterInternalWithValidToken() throws ServletException, IOException {
        // Arrange
        String token = "valid.jwt.token";
        Long userId = 1L;
        
        // Setup user
        User user = new User("testuser", "test@example.com", "password");
        user.setId(userId);
        
        // Add role to user
        Role role = new Role(ERole.ROLE_USER);
        user.setRole(role);
        
        when(jwtUtils.extractTokenFromRequest(request)).thenReturn(token);
        when(jwtUtils.validateJwtToken(token)).thenReturn(TokenStatus.VALID);
        when(jwtUtils.getUserIdFromJwtToken(token)).thenReturn(userId);
        when(userService.findById(userId)).thenReturn(Optional.of(user));

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtils).extractTokenFromRequest(request);
        verify(jwtUtils).validateJwtToken(token);
        verify(jwtUtils).getUserIdFromJwtToken(token);
        verify(userService).findById(userId);
        verify(request).setAttribute("userId", userId);
        verify(filterChain).doFilter(request, response);
        
        // Verify authentication was set in SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(user, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    /**
     * @brief Test filtering with no JWT token.
     *
     * Verifies that the filter correctly continues the chain without setting authentication.
     */
    @Test
    public void testDoFilterInternalWithNoToken() throws ServletException, IOException {
        // Arrange
        when(jwtUtils.extractTokenFromRequest(request)).thenReturn(null);

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtils).extractTokenFromRequest(request);
        verify(jwtUtils, never()).validateJwtToken(anyString());
        verify(jwtUtils, never()).getUserIdFromJwtToken(anyString());
        verify(userService, never()).findById(anyLong());
        verify(filterChain).doFilter(request, response);
        
        // Verify no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * @brief Test filtering with invalid JWT token.
     *
     * Verifies that the filter correctly rejects invalid tokens without setting authentication.
     */
    @Test
    public void testDoFilterInternalWithInvalidToken() throws ServletException, IOException {
        // Arrange
        String token = "invalid.jwt.token";
        
        when(jwtUtils.extractTokenFromRequest(request)).thenReturn(token);
        when(jwtUtils.validateJwtToken(token)).thenReturn(TokenStatus.INVALID);

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtils).extractTokenFromRequest(request);
        verify(jwtUtils).validateJwtToken(token);
        verify(jwtUtils, never()).getUserIdFromJwtToken(anyString());
        verify(userService, never()).findById(anyLong());
        verify(filterChain).doFilter(request, response);
        
        // Verify no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * @brief Test filtering with expired JWT token.
     *
     * Verifies that the filter correctly rejects expired tokens without setting authentication.
     */
    @Test
    public void testDoFilterInternalWithExpiredToken() throws ServletException, IOException {
        // Arrange
        String token = "expired.jwt.token";
        
        when(jwtUtils.extractTokenFromRequest(request)).thenReturn(token);
        when(jwtUtils.validateJwtToken(token)).thenReturn(TokenStatus.EXPIRED);

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtils).extractTokenFromRequest(request);
        verify(jwtUtils).validateJwtToken(token);
        verify(jwtUtils, never()).getUserIdFromJwtToken(anyString());
        verify(userService, never()).findById(anyLong());
        verify(filterChain).doFilter(request, response);
        
        // Verify no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * @brief Test filtering with valid token but user not found.
     *
     * Verifies that the filter correctly handles case when token is valid but user is not found.
     */
    @Test
    public void testDoFilterInternalWithValidTokenButUserNotFound() throws ServletException, IOException {
        // Arrange
        String token = "valid.jwt.token";
        Long userId = 1L;
        
        when(jwtUtils.extractTokenFromRequest(request)).thenReturn(token);
        when(jwtUtils.validateJwtToken(token)).thenReturn(TokenStatus.VALID);
        when(jwtUtils.getUserIdFromJwtToken(token)).thenReturn(userId);
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtils).extractTokenFromRequest(request);
        verify(jwtUtils).validateJwtToken(token);
        verify(jwtUtils).getUserIdFromJwtToken(token);
        verify(userService).findById(userId);
        verify(request, never()).setAttribute(eq("userId"), any());
        verify(filterChain).doFilter(request, response);
        
        // Verify no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * @brief Test cleanup of security context after exception.
     *
     * Verifies that the filter cleans up SecurityContext when an exception occurs.
     */
    @Test
    public void testDoFilterInternalWithExceptionDuringChainExecution() throws ServletException, IOException {
        // Arrange
        String token = "valid.jwt.token";
        Long userId = 1L;
        
        // Setup user
        User user = new User("testuser", "test@example.com", "password");
        user.setId(userId);
        
        when(jwtUtils.extractTokenFromRequest(request)).thenReturn(token);
        when(jwtUtils.validateJwtToken(token)).thenReturn(TokenStatus.VALID);
        when(jwtUtils.getUserIdFromJwtToken(token)).thenReturn(userId);
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        
        // Simulate exception during filter chain execution
        doThrow(new RuntimeException("Simulated error")).when(filterChain).doFilter(request, response);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> jwtFilter.doFilterInternal(request, response, filterChain));
        
        // Verify proper cleanup
        verify(jwtUtils).extractTokenFromRequest(request);
        verify(jwtUtils).validateJwtToken(token);
        verify(jwtUtils).getUserIdFromJwtToken(token);
        verify(userService).findById(userId);
    }
}

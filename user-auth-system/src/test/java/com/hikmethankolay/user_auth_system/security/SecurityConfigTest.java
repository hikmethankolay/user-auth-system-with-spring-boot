/**
 * @file SecurityConfigTest.java
 * @brief Tests for the SecurityConfig class.
 *
 * Contains unit tests for security configuration including authentication and authorization rules.
 *
 * @author Test Suite Generator
 * @date 2025-03-29
 */
package com.hikmethankolay.user_auth_system.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.hikmethankolay.user_auth_system.dto.LoginRequestDTO;
import com.hikmethankolay.user_auth_system.dto.UserRegisterDTO;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @class SecurityConfigTest
 * @brief Test class for SecurityConfig.
 *
 * This class contains integration tests for security configuration.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    /**
     * MockMvc instance for simulating HTTP requests.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Object mapper for serializing and deserializing JSON.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Mock UserService for security test dependencies.
     */
    @MockitoBean
    private UserService userService;

    /**
     * @brief Test access to public endpoints.
     *
     * Verifies that public endpoints are accessible without authentication.
     */
    @Test
    public void testPublicEndpointsAccessible() throws Exception {
        // Prepare test data for registration
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("newuser123");
        registerDTO.setEmail("new@example.com");
        registerDTO.setPassword("P@ssw0rd123!");
        
        // Prepare test data for login
        LoginRequestDTO loginDTO = new LoginRequestDTO("testuser", "password", false);
        
        // Mock service responses
        User mockUser = new User("testuser", "test@example.com", "encodedPassword");
        mockUser.setId(1L);
        when(userService.registerUser(any(UserRegisterDTO.class))).thenReturn(mockUser);
        when(userService.authenticateUser(any(LoginRequestDTO.class), anyString())).thenReturn("jwt.token.string");

        // Test registration endpoint
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk());

        // Test login endpoint
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk());
    }

    /**
     * @brief Test access to protected user endpoints without authentication.
     *
     * Verifies that protected endpoints reject unauthenticated requests.
     */
    @Test
    public void testProtectedEndpointsRejectedWithoutAuth() throws Exception {
        // Test protected user endpoints
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(patch("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * @brief Test access to protected user endpoints with authentication.
     *
     * Verifies that protected endpoints accept authenticated requests.
     */
    @Test
    @WithMockUser
    public void testUserEndpointsAccessibleWithAuth() throws Exception {
        // Mock user for "me" endpoint
        User mockUser = new User("testuser", "test@example.com", "encodedPassword");
        mockUser.setId(1L);
        when(userService.findById(any())).thenReturn(java.util.Optional.of(mockUser));

        // Test protected user endpoints with authentication
        mockMvc.perform(get("/api/users/me")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk());
    }

    /**
     * @brief Test access to admin endpoints with regular user role.
     *
     * Verifies that admin endpoints reject requests from regular users.
     */
    @Test
    @WithMockUser
    public void testAdminEndpointsRejectedForRegularUsers() throws Exception {
        // Test admin endpoints with regular user role
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
        
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
        
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isForbidden());
    }

    /**
     * @brief Test access to admin endpoints with admin role.
     *
     * Verifies that admin endpoints accept requests with admin role.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAdminEndpointsAccessibleForAdmins() throws Exception {
        // Mock user list for admin endpoint
        when(userService.findAll(any())).thenReturn(new org.springframework.data.domain.PageImpl<>(
                java.util.Collections.singletonList(new User("admin", "admin@example.com", "password"))));

        // Test admin endpoints with admin role
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk());
    }

    /**
     * @brief Test CORS configuration allows specified origins.
     *
     * Verifies that CORS configuration correctly allows specified origins.
     */
    @Test
    public void testCorsConfiguration() throws Exception {
        // Test CORS preflight request
        mockMvc.perform(options("/api/auth/login")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }
}

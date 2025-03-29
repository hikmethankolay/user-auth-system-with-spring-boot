/**
 * @file RoleControllerTest.java
 * @brief Tests for the RoleController class.
 *
 * Contains unit tests for the role controller endpoints.
 *
 * @author Test Suite Generator
 * @date 2025-03-29
 */
package com.hikmethankolay.user_auth_system.controller;

import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.enums.EApiStatus;
import com.hikmethankolay.user_auth_system.enums.ERole;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @class RoleControllerTest
 * @brief Test class for RoleController.
 *
 * This class contains unit tests for role management operations.
 */
public class RoleControllerTest extends BaseControllerTest {

    /**
     * @brief Test successful retrieval of all roles.
     *
     * Verifies that the findAll endpoint correctly returns all roles.
     */
    @Test
    public void testFindAllRoles() throws Exception {
        // Arrange
        Role userRole = new Role(ERole.ROLE_USER);
        Role modRole = new Role(ERole.ROLE_MODERATOR);
        Role adminRole = new Role(ERole.ROLE_ADMIN);
        List<Role> roles = Arrays.asList(userRole, modRole, adminRole);

        when(roleService.findAll()).thenReturn(roles);

        // Act & Assert
        mockMvc.perform(get("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].name").value(ERole.ROLE_USER.name()))
                .andExpect(jsonPath("$.data[1].name").value(ERole.ROLE_MODERATOR.name()))
                .andExpect(jsonPath("$.data[2].name").value(ERole.ROLE_ADMIN.name()))
                .andExpect(jsonPath("$.message").value("Roles found successfully"));
    }

    /**
     * @brief Test successful retrieval of role by name.
     *
     * Verifies that the findByName endpoint correctly returns a specific role when found.
     */
    @Test
    public void testFindRoleByNameSuccess() throws Exception {
        // Arrange
        Role adminRole = new Role(ERole.ROLE_ADMIN);

        when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));

        // Act & Assert
        mockMvc.perform(get("/api/roles/ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.data.name").value(ERole.ROLE_ADMIN.name()))
                .andExpect(jsonPath("$.message").value("Role found successfully"));
    }

    /**
     * @brief Test role retrieval by name when role doesn't exist.
     *
     * Verifies that the findByName endpoint correctly handles non-existent roles.
     */
    @Test
    public void testFindRoleByNameNotFound() throws Exception {
        // Arrange
        when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/roles/ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.name()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Could not find role with name: ROLE_ADMIN"));
    }
}
/**
 * @file RoleServiceTest.java
 * @brief Tests for the RoleService class.
 *
 * Contains unit tests for all role service operations.
 *
 * @author Test Suite Generator
 * @date 2025-03-29
 */
package com.hikmethankolay.user_auth_system.service;

import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.enums.ERole;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @class RoleServiceTest
 * @brief Test class for RoleService.
 *
 * This class contains unit tests for all role service operations.
 */
public class RoleServiceTest extends BaseServiceTest {

    /**
     * RoleService instance to be tested, with mocked dependencies injected.
     */
    @Autowired
    private RoleService roleService;

    /**
     * @brief Test finding all roles.
     *
     * Verifies that findAll correctly returns all roles.
     */
    @Test
    public void testFindAll() {
        // Arrange
        Role userRole = new Role(ERole.ROLE_USER);
        Role modRole = new Role(ERole.ROLE_MODERATOR);
        Role adminRole = new Role(ERole.ROLE_ADMIN);
        List<Role> roles = Arrays.asList(userRole, modRole, adminRole);
        
        when(roleRepository.findAll()).thenReturn(roles);

        // Act
        List<Role> result = roleService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(ERole.ROLE_USER, result.get(0).getName());
        assertEquals(ERole.ROLE_MODERATOR, result.get(1).getName());
        assertEquals(ERole.ROLE_ADMIN, result.get(2).getName());
        
        verify(roleRepository).findAll();
    }

    /**
     * @brief Test finding role by name with success.
     *
     * Verifies that findByName correctly returns a role when found.
     */
    @Test
    public void testFindByNameSuccess() {
        // Arrange
        Role adminRole = new Role(ERole.ROLE_ADMIN);
        
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));

        // Act
        Optional<Role> result = roleService.findByName(ERole.ROLE_ADMIN);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ERole.ROLE_ADMIN, result.get().getName());
        
        verify(roleRepository).findByName(ERole.ROLE_ADMIN);
    }

    /**
     * @brief Test finding role by name when role doesn't exist.
     *
     * Verifies that findByName correctly returns empty when role not found.
     */
    @Test
    public void testFindByNameNotFound() {
        // Arrange
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.empty());

        // Act
        Optional<Role> result = roleService.findByName(ERole.ROLE_ADMIN);

        // Assert
        assertFalse(result.isPresent());
        
        verify(roleRepository).findByName(ERole.ROLE_ADMIN);
    }
}

/**
 * @file RoleServiceTest.java
 * @brief Unit tests for the RoleService class.
 *
 * This file contains test cases for verifying the behavior of RoleService,
 * including retrieving all roles and fetching roles by name.
 */


/**
 * @package com.hikmethankolay.user_auth_system
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system;

import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.ERole;
import com.hikmethankolay.user_auth_system.repository.RoleRepository;
import com.hikmethankolay.user_auth_system.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @class RoleServiceTest
 * @brief Unit tests for the RoleService class.
 *
 * This class tests the service layer responsible for role-related operations.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class RoleServiceTest {

    /**
     * @brief Mocked RoleRepository instance for unit testing.
     */
    @MockitoBean
    private RoleRepository roleRepository;

    /**
     * @brief The RoleService instance being tested.
     */
    @Autowired
    private RoleService roleService;

    /**
     * @brief Mocked role entity representing a USER role.
     */
    private Role mockRole = new Role();

    /**
     * @brief Mocked role entity representing an ADMIN role.
     */
    private Role mockRole2 = new Role();

    /**
     * @brief Mocked user entity representing a regular user.
     */
    private User mockUser = new User();

    /**
     * @brief Mocked user entity representing an admin user.
     */
    private User mockUser2  = new User();

    /**
     * @brief Sets up mock objects before each test execution.
     *
     * Initializes mock users and roles with predefined values.
     */
    @BeforeEach
    void setUp() {

        // Initialize mock user 1
        mockUser.setId(1L);
        mockUser.setUsername("test_user");
        mockUser.setEmail("test@example.com");
        mockUser.addRole(new Role(ERole.ROLE_USER));

        // Initialize mock user 2
        mockUser2.setId(2L);
        mockUser2.setUsername("test_user2");
        mockUser2.setEmail("test2@example.com");
        mockUser2.addRole(new Role(ERole.ROLE_ADMIN));
        mockUser2.addRole(new Role(ERole.ROLE_USER));

        // Initialize mock role USER
        mockRole.setId(1);
        mockRole.setName(ERole.ROLE_USER);
        mockRole.addUser(mockUser);
        mockRole.addUser(mockUser2);

        // Initialize mock role ADMIN
        mockRole2.setId(2);
        mockRole2.setName(ERole.ROLE_ADMIN);
        mockRole2.addUser(mockUser2);
    }

    /**
     * @brief Tests retrieving all roles from the database.
     *
     * Ensures that the service correctly retrieves all stored roles.
     */
    @Test
    void testFindAll() {
        // Mock behavior
        when(roleRepository.findAll()).thenReturn(List.of(mockRole, mockRole2));

        // Call service method
        List<Role> roles = roleService.findAll();

        // Verify and assert results
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertEquals(ERole.ROLE_USER, roles.get(0).getName());
        assertEquals(ERole.ROLE_ADMIN, roles.get(1).getName());

        verify(roleRepository, times(1)).findAll();
    }

    /**
     * @brief Tests retrieving a role by name when it exists.
     *
     * Ensures that the service correctly finds and returns a role by its name.
     */
    @Test
    void testFindByName_Success() {
        // Mock behavior
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(mockRole));

        // Call service method
        Optional<Role> foundRole = roleService.findByName(ERole.ROLE_USER);

        // Verify and assert results
        assertTrue(foundRole.isPresent());
        assertEquals(ERole.ROLE_USER, foundRole.get().getName());

        verify(roleRepository, times(1)).findByName(ERole.ROLE_USER);
    }

    /**
     * @brief Tests retrieving a role by name when it does not exist.
     *
     * Ensures that the service correctly handles cases where a role is not found.
     */
    @Test
    void testFindByName_NotFound() {
        // Mock behavior
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.empty());

        // Call service method
        Optional<Role> foundRole = roleService.findByName(ERole.ROLE_USER);

        // Verify and assert results
        assertFalse(foundRole.isPresent());

        verify(roleRepository, times(1)).findByName(ERole.ROLE_USER);
    }
}

/**
 * @file RoleControllerTest.java
 * @brief Unit tests for RoleController.
 *
 * This file contains test cases for verifying the behavior of RoleController,
 * including retrieving all roles and fetching roles by name.
 */


/**
 * @package com.hikmethankolay.user_auth_system
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system;

import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.EApiStatus;
import com.hikmethankolay.user_auth_system.enums.ERole;
import com.hikmethankolay.user_auth_system.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @class RoleControllerTest
 * @brief Unit tests for the RoleController API.
 *
 * This class tests endpoints related to retrieving role information.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class RoleControllerTest {

    /**
     * @brief MockMvc for performing HTTP requests in test cases.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * @brief Mocked instance of RoleService for unit testing.
     */
    @MockitoBean
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
     * @brief Tests retrieving all roles.
     *
     * Ensures that the API returns a successful response when roles exist.
     *
     * @throws Exception if the test encounters an error.
     */
    @Test
    public void testGetAlRoles() throws Exception {

        List<Role> mockRoles = List.of(mockRole, mockRole2);

        // Mocking the service method to return predefined roles
        when(roleService.findAll()).thenReturn(mockRoles);

        // Performing API request and verifying response
        ResultActions result = mockMvc.perform(get("/api/roles")
                .with(user("test_user2").roles("ADMIN")));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value(mockRole.getName().name()))
                .andExpect(jsonPath("$.data[0].users[0].username").value("test_user"))
                .andExpect(jsonPath("$.data[0].users[1].username").value("test_user2"))
                .andExpect(jsonPath("$.data[1].name").value(mockRole2.getName().name()))
                .andExpect(jsonPath("$.data[1].users[0].username").value("test_user2"))
                .andExpect(jsonPath("$.message").value("Roles found successfully"));
    }

    /**
     * @brief Tests retrieving a role by its name.
     *
     * Ensures that the API returns the correct role when searched by name.
     *
     * @throws Exception if the test encounters an error.
     */
    @Test
    public void testGetRolesByName_Success() throws Exception {

        // Mocking service method to return predefined role
        when(roleService.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(mockRole));

        // Performing API request and verifying response
        ResultActions result = mockMvc.perform(get("/api/roles/{name}", ERole.ROLE_USER)
                .with(user("test_user2").roles("ADMIN")));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.data.name").value(mockRole.getName().name()))
                .andExpect(jsonPath("$.data.users[0].username").value("test_user"))
                .andExpect(jsonPath("$.data.users[1].username").value("test_user2"))
                .andExpect(jsonPath("$.message").value("Role found successfully"));
    }

    /**
     * @brief Tests the failure case for retrieving a role by name.
     *
     * Ensures that the API returns a 404 Not Found response when the role does not exist.
     *
     * @throws Exception if the test encounters an error.
     */
    @Test
    void testFindByName_Failure() throws Exception {
        // Mock service response: Role not found
        when(roleService.findByName(ERole.ROLE_MODERATOR)).thenReturn(Optional.empty());

        // Perform GET request and verify response
        mockMvc.perform(get("/api/roles/{name}", ERole.ROLE_MODERATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user("test_user2").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.toString()))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("Could not find role with name: ROLE_MODERATOR"));
    }
}

/**
 * @file UserControllerTest.java
 * @brief Unit tests for the UserController class.
 *
 * This file contains the unit tests for UserController using Spring Boot Test, MockMvc, and Mockito.
 * It verifies the behavior of various user-related API endpoints.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-15
 */

/**
 * @package com.hikmethankolay.user_auth_system
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hikmethankolay.user_auth_system.dto.UserUpdateDTO;
import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.EApiStatus;
import com.hikmethankolay.user_auth_system.enums.ERole;
import com.hikmethankolay.user_auth_system.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @class UserControllerTest
 * @brief Test class for UserController endpoints.
 *
 * This class contains unit tests to validate the behavior of user-related API endpoints,
 * ensuring correct responses for various request scenarios.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    /**
     * @brief MockMvc for performing HTTP requests in test cases.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * @brief ObjectMapper for JSON serialization and deserialization.
     */
    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @brief Mocked instance of UserService for unit testing.
     */
    @MockitoBean
    private UserService userService;

    /**
     * @brief Tests retrieving all users.
     *
     * Ensures that the API returns a successful response when users exist.
     * @throws Exception if the test encounters an error.
     */
    @Test
    public void testGetAllUsers() throws Exception {

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("test_user");
        mockUser.setEmail("test@example.com");
        mockUser.addRole(new Role(ERole.ROLE_USER));

        List<User> mockUsers = List.of(mockUser);

        Page<User> userPage = new PageImpl<>(mockUsers);

        when(userService.findAll(any(Pageable.class))).thenReturn(userPage);

        ResultActions result = mockMvc.perform(get("/api/users")
                .with(user("admin").roles("ADMIN")));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.message").value("Users found successfully"));
    }


    /**
     * @brief Tests retrieving a user by ID (Success case).
     *
     * Ensures a user with a valid ID is returned correctly.
     * @throws Exception if the test encounters an error.
     */
    @Test
    public void testGetUserById_Success() throws Exception {

        Long userId = 1L;

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("test_user");
        mockUser.setEmail("test@example.com");
        mockUser.addRole(new Role(ERole.ROLE_USER));


        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));

        ResultActions result = mockMvc.perform(get("/api/users/{id}", userId)
                .with(user("admin").roles("ADMIN")));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("test_user"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.message").value("User found successfully"));
    }


    /**
     * @brief Tests retrieving a user by ID (Failure case).
     *
     * Ensures that a non-existent user ID returns a 404 Not Found response.
     * @throws Exception if the test encounters an error.
     */
    @Test
    public void testGetUserById_Failure() throws Exception {

        Long userId = Long.MAX_VALUE;

        mockMvc.perform(get("/api/users/{id}", userId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound()) // Expecting 404 Not Found
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("FAILURE")) // Expected failure response
                .andExpect(jsonPath("$.message").value("Could not find user with id: " + userId)); // Error message
    }


    /**
     * @brief Tests retrieving the logged-in user (Success case).
     *
     * Ensures the correct user information is returned when authenticated.
     * @throws Exception if the test encounters an error.
     */
    @Test
    public void testGetLoggedInUser_Success() throws Exception {

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("test_user");
        mockUser.setEmail("test@example.com");
        mockUser.addRole(new Role(ERole.ROLE_USER));

        when(userService.findById(1L)).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/users/me")
                        .with(user("test_user").roles("USER"))
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("test_user"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.message").value("User found successfully"));
    }

    /**
     * @brief Tests retrieving the logged-in user (Unauthorized case).
     *
     * Ensures an unauthorized request returns a 401 status.
     * @throws Exception if the test encounters an error.
     */
    @Test
    public void testGetLoggedInUser_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.toString()))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    /**
     * @brief Tests retrieving a user by username (Success case).
     *
     * Ensures a user can be retrieved by their username.
     * @throws Exception if the test encounters an error.
     */
    @Test
    public void testGetUserByUsername_Success() throws Exception {
        String username = "test_user";

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("test_user");
        mockUser.setEmail("test@example.com");
        mockUser.addRole(new Role(ERole.ROLE_USER));

        when(userService.findByUsernameOrEmail(username)).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/users").param("username", username)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("test_user"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.message").value("User found successfully"));
    }

    /**
     * @brief Tests retrieving a user by username (Failure case).
     *
     * Ensures a non-existent username returns a 404 Not Found response.
     * @throws Exception if the test encounters an error.
     */
    @Test
    public void testGetUserByUsername_Failure() throws Exception {
        String username = "nonexistent";
        when(userService.findByUsernameOrEmail(username)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users").param("username", username)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.toString()))
                .andExpect(jsonPath("$.message").value("Could not find user with username: " + username));
    }


    /**
     * @brief Tests retrieving a user by email (Success case).
     *
     * Ensures a user can be retrieved by their username.
     * @throws Exception if the test encounters an error.
     */
    @Test
    public void testGetUserByEmail_Success() throws Exception {
        String email = "test@example.com";

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("test_user");
        mockUser.setEmail("test@example.com");
        mockUser.addRole(new Role(ERole.ROLE_USER));

        when(userService.findByUsernameOrEmail(email)).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/users").param("email", email)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("test_user"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.message").value("User found successfully"));
    }


    /**
     * @brief Tests retrieving a user by email (Failure case).
     *
     * Ensures a non-existent username returns a 404 Not Found response.
     * @throws Exception if the test encounters an error.
     */
    @Test
    public void testGetUserByEmail_Failure() throws Exception {
        String email = "doesnotexist@example.com";
        when(userService.findByUsernameOrEmail(email)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users").param("email", email)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.toString()))
                .andExpect(jsonPath("$.message").value("Could not find user with email: " + email));
    }

    /**
     * @brief Tests updating a user (Success case).
     *
     * Ensures that a valid user update request succeeds.
     * @throws Exception if the test encounters an error.
     */
    @Test
    public void testUpdateUser_Success() throws Exception {
        Long userId = 1L;

        User updatedUser = new User("updatedUser","updated@example.com","");
        updatedUser.setId(userId);

        updatedUser.addRole(new Role(ERole.ROLE_USER));

        UserUpdateDTO updateDTO = new UserUpdateDTO(updatedUser);

        when(userService.updateUser(any(UserUpdateDTO.class), eq(userId))).thenReturn(updatedUser);

        mockMvc.perform(patch("/api/users/{id}", userId)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO))) // Convert DTO to JSON
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("updatedUser"))
                .andExpect(jsonPath("$.data.email").value("updated@example.com"))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.message").value("User updated successfully"));
    }

    /**
     * @brief Tests updating logged-in user (Success case).
     *
     * Ensures that a valid user update request succeeds.
     * @throws Exception if the test encounters an error.
     */
    @Test
    public void testUpdateLoggedInUser_Success() throws Exception {
        Long userId = 1L;

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setId(userId);
        updateDTO.setUsername("User");
        updateDTO.setEmail("User@example.com");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setUsername("updatedUser");
        updatedUser.setEmail("updatedUser@example.com");

        when(userService.updateUser(any(UserUpdateDTO.class), eq(userId))).thenReturn(updatedUser);

        mockMvc.perform(patch("/api/users/me")
                        .with(user("User").roles("USER"))
                        .requestAttr("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.username").value("updatedUser"))
                .andExpect(jsonPath("$.data.email").value("updatedUser@example.com"))
                .andExpect(jsonPath("$.message").value("User updated successfully"));
    }

    /**
     * @brief Tests updating logged-in user (Unauthorized case).
     *
     * Ensures that an unauthorized user update request fails.
     * @throws Exception if the test encounters an error.
     */
    @Test
    public void testUpdateLoggedInUser_Unauthorized() throws Exception {
        mockMvc.perform(patch("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.toString()))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }


    /**
     * @brief Tests updating a user (Failure case).
     *
     * Ensures that updating a non-existent user returns a failure response.
     * @throws Exception if the test encounters an error.
     */
    @Test
    public void testUpdateUser_NotFound() throws Exception {
        Long userId = Long.MAX_VALUE;

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setId(userId);
        updateDTO.setUsername("updatedUser");
        updateDTO.setEmail("updated@example.com");

        when(userService.updateUser(any(UserUpdateDTO.class), eq(userId)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(patch("/api/users/{id}", userId)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.toString()))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    /**
     * @brief Tests deleting a user (Success case).
     *
     * Ensures that a user can be deleted successfully.
     * @throws Exception if the test encounters an error.
     */
    @Test
    public void testDeleteUser_Success() throws Exception {
        Long userId = 1L;

        doNothing().when(userService).deleteById(userId);

        mockMvc.perform(delete("/api/users/{id}", userId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }


    /**
     * @brief Tests deleting a user (Failure case).
     *
     * Ensures that deleting a non-existent user returns a failure response.
     * @throws Exception if the test encounters an error.
     */
    @Test
    public void testDeleteUser_Failure() throws Exception {
        Long userId = Long.MAX_VALUE;

        doThrow(new RuntimeException("User not found")).when(userService).deleteById(userId);

        mockMvc.perform(delete("/api/users/{id}", userId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.toString()))
                .andExpect(jsonPath("$.message").value("User not found"));
    }


}

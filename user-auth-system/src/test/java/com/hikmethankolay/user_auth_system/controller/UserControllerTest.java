/**
 * @file UserControllerTest.java
 * @brief Tests for the UserController class.
 *
 * Contains unit tests for the user controller endpoints.
 *
 * @author Test Suite Generator
 * @date 2025-03-29
 */
package com.hikmethankolay.user_auth_system.controller;

import com.hikmethankolay.user_auth_system.dto.UserUpdateDTO;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.EApiStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @class UserControllerTest
 * @brief Test class for UserController.
 *
 * This class contains unit tests for user management operations.
 */
public class UserControllerTest extends BaseControllerTest {

    /**
     * @brief Test successful retrieval of paginated users.
     *
     * Verifies that the getUsers endpoint correctly returns paginated user data.
     */
    @Test
    public void testGetUsersSuccess() throws Exception {
        // Arrange
        User user1 = new User("testuser1", "test1@example.com", "password1");
        user1.setId(1L);
        User user2 = new User("testuser2", "test2@example.com", "password2");
        user2.setId(2L);

        List<User> userList = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(userList);

        when(userService.findAll(any(Pageable.class))).thenReturn(userPage);

        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].username").value("testuser1"))
                .andExpect(jsonPath("$.data.content[1].id").value(2))
                .andExpect(jsonPath("$.data.content[1].username").value("testuser2"))
                .andExpect(jsonPath("$.message").value("Users found successfully"));
    }

    /**
     * @brief Test successful retrieval of a user by ID.
     *
     * Verifies that the getUserById endpoint correctly returns a specific user when found.
     */
    @Test
    public void testGetUserByIdSuccess() throws Exception {
        // Arrange
        User user = new User("testuser", "test@example.com", "password");
        user.setId(1L);

        when(userService.findById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(get("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").value("User found successfully"));
    }

    /**
     * @brief Test user retrieval by ID when user doesn't exist.
     *
     * Verifies that the getUserById endpoint correctly handles non-existent users.
     */
    @Test
    public void testGetUserByIdNotFound() throws Exception {
        // Arrange
        when(userService.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.name()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Could not find user with id: 99"));
    }

    /**
     * @brief Test successful retrieval of the logged-in user.
     *
     * Verifies that the getLoggedInUser endpoint correctly returns the user's information.
     */
    @Test
    public void testGetLoggedInUserSuccess() throws Exception {
        // Arrange
        User user = new User("testuser", "test@example.com", "password");
        user.setId(1L);

        when(userService.findById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").value("User found successfully"));
    }

    /**
     * @brief Test successful retrieval of a user by username.
     *
     * Verifies that the getUserByUsername endpoint correctly returns a specific user when found.
     */
    @Test
    public void testGetUserByUsernameSuccess() throws Exception {
        // Arrange
        User user = new User("testuser", "test@example.com", "password");
        user.setId(1L);

        when(userService.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(get("/api/users?username=testuser")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").value("User found successfully"));
    }

    /**
     * @brief Test user retrieval by username when user doesn't exist.
     *
     * Verifies that the getUserByUsername endpoint correctly handles non-existent users.
     */
    @Test
    public void testGetUserByUsernameNotFound() throws Exception {
        // Arrange
        when(userService.findByUsernameOrEmail("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users?username=nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.name()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Could not find user with username: nonexistent"));
    }

    /**
     * @brief Test successful retrieval of a user by email.
     *
     * Verifies that the getUserByEmail endpoint correctly returns a specific user when found.
     */
    @Test
    public void testGetUserByEmailSuccess() throws Exception {
        // Arrange
        User user = new User("testuser", "test@example.com", "password");
        user.setId(1L);

        when(userService.findByUsernameOrEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(get("/api/users?email=test@example.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").value("User found successfully"));
    }

    /**
     * @brief Test user retrieval by email when user doesn't exist.
     *
     * Verifies that the getUserByEmail endpoint correctly handles non-existent users.
     */
    @Test
    public void testGetUserByEmailNotFound() throws Exception {
        // Arrange
        when(userService.findByUsernameOrEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users?email=nonexistent@example.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.name()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Could not find user with email: nonexistent@example.com"));
    }

    /**
     * @brief Test successful user update.
     *
     * Verifies that the updateUser endpoint correctly processes valid update data.
     */
    @Test
    public void testUpdateUserSuccess() throws Exception {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setUsername("updateduser");
        updateDTO.setEmail("updated@example.com");

        User updatedUser = new User("updateduser", "updated@example.com", "password");
        updatedUser.setId(1L);

        when(userService.updateUser(any(UserUpdateDTO.class), eq(1L), eq(2L))).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO))
                        .requestAttr("userId", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("updateduser"))
                .andExpect(jsonPath("$.data.email").value("updated@example.com"))
                .andExpect(jsonPath("$.message").value("User updated successfully"));
    }

    /**
     * @brief Test user update with validation errors.
     *
     * Verifies that the updateUser endpoint correctly handles validation errors.
     */
    @Test
    public void testUpdateUserValidationError() throws Exception {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setUsername("short"); // Too short username
        updateDTO.setEmail("invalid-email"); // Invalid email format

        when(userService.updateUser(any(UserUpdateDTO.class), eq(1L), eq(2L)))
                .thenThrow(new RuntimeException("Validation failed: Username must be between 8 and 32 characters"));

        // Act & Assert
        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO))
                        .requestAttr("userId", 2L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.name()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Validation failed: Username must be between 8 and 32 characters"));
    }

    /**
     * @brief Test update logged-in user.
     *
     * Verifies that the updateLoggedInUser endpoint correctly updates the user's own information.
     */
    @Test
    public void testUpdateLoggedInUserSuccess() throws Exception {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setUsername("updateduser");
        updateDTO.setEmail("updated@example.com");

        User updatedUser = new User("updateduser", "updated@example.com", "password");
        updatedUser.setId(1L);

        when(userService.updateUser(any(UserUpdateDTO.class), eq(1L), eq(1L))).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO))
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("updateduser"))
                .andExpect(jsonPath("$.data.email").value("updated@example.com"))
                .andExpect(jsonPath("$.message").value("User updated successfully"));
    }

    /**
     * @brief Test successful user deletion.
     *
     * Verifies that the deleteUser endpoint correctly deletes a user.
     */
    @Test
    public void testDeleteUserSuccess() throws Exception {
        // Arrange
        // No need to set up any return since the method is void

        // Act & Assert
        mockMvc.perform(delete("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("userId", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    /**
     * @brief Test user deletion with error.
     *
     * Verifies that the deleteUser endpoint correctly handles errors during deletion.
     */
    @Test
    public void testDeleteUserError() throws Exception {
        // Arrange
        // Simulate an error during deletion
        doThrow(new RuntimeException("User not found with id 99")).when(userService).deleteById(eq(99L), anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("userId", 2L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.name()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("User not found with id 99"));
    }

    /**
     * @brief Test attempt to delete own account.
     *
     * Verifies that the deleteUser endpoint prevents users from deleting their own account.
     */
    @Test
    public void testDeleteOwnAccountError() throws Exception {
        // Arrange
        // Simulate an error when trying to delete own account
        doThrow(new RuntimeException("Cannot delete your own account")).when(userService).deleteById(eq(1L), eq(1L));

        // Act & Assert
        mockMvc.perform(delete("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.name()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Cannot delete your own account"));
    }
}
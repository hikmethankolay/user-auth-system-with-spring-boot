/**
 * @file AuthControllerTest.java
 * @brief Unit tests for the AuthController class.
 *
 * This file contains the unit tests for AuthController using Spring Boot Test, MockMvc, and Mockito.
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
import com.hikmethankolay.user_auth_system.dto.LoginRequestDTO;
import com.hikmethankolay.user_auth_system.dto.UserRegisterDTO;
import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.EApiStatus;
import com.hikmethankolay.user_auth_system.enums.ERole;
import com.hikmethankolay.user_auth_system.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @class AuthControllerTest
 * @brief Test class for AuthController endpoints.
 *
 * This class contains unit tests to validate the behavior of auth-related API endpoints,
 * ensuring correct responses for various request scenarios.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

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
     * @brief Tests successful user registration.
     *
     * Mocks `userService.registerUser()` to return a newly created user.
     * Expects HTTP 200 OK with user details in the response.
     *
     * @throws Exception if an error occurs during the test.
     */
    @Test
    public void testRegisterUser_Success() throws Exception {
        UserRegisterDTO registerRequest = new UserRegisterDTO();
        registerRequest.setUsername("newUser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");

        User registeredUser = new User();
        registeredUser.setId(1L);
        registeredUser.setUsername("newUser");
        registeredUser.setEmail("newuser@example.com");
        registeredUser.addRole(new Role(ERole.ROLE_USER));

        when(userService.registerUser(any(UserRegisterDTO.class))).thenReturn(registeredUser);

        mockMvc.perform(post("/api/auth/register")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.data.username").value("newUser"))
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    /**
     * @brief Tests failed registration due to duplicate username.
     *
     * Mocks `userService.registerUser()` to throw an exception for an existing user.
     * Expects HTTP 400 Bad Request with an error message.
     *
     * @throws Exception if an error occurs during the test.
     */
    @Test
    public void testRegisterUser_Failure() throws Exception {
        UserRegisterDTO registerRequest = new UserRegisterDTO();
        registerRequest.setUsername("existingUser");
        registerRequest.setEmail("existinguser@example.com");
        registerRequest.setPassword("password123");

        when(userService.registerUser(any(UserRegisterDTO.class)))
                .thenThrow(new RuntimeException("Username is already taken!"));

        mockMvc.perform(post("/api/auth/register")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.toString()))
                .andExpect(jsonPath("$.message").value("Username is already taken!"));
    }

    /**
     * @brief Tests failed login due to wrong username or password.
     *
     * Mocks `userService.authenticateUser()` to return a null Token.
     * Expects HTTP 200 OK with JWT token in the response.
     *
     * @throws Exception if an error occurs during the test.
     */
    @Test
    public void testLoginUser_Success() throws Exception {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("username", "password123");

        when(userService.authenticateUser(any(LoginRequestDTO.class))).thenReturn("Mocked-JWT-Token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.SUCCESS.toString()))
                .andExpect(jsonPath("$.data.token").value("Mocked-JWT-Token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.message").value("User authenticated successfully"));
    }

    /**
     * @brief Tests successful user login.
     *
     * Mocks `userService.authenticateUser()` to return a newly created JWT Token.
     * Expects HTTP 400 Bad Request with an error message.
     *
     * @throws Exception if an error occurs during the test.
     */
    @Test
    public void testLoginUser_Failure() throws Exception {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("wrong_username", "wrong_password123");

        when(userService.authenticateUser(any(LoginRequestDTO.class))).thenReturn(null);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(EApiStatus.FAILURE.toString()))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("Wrong username or password"));
    }


}

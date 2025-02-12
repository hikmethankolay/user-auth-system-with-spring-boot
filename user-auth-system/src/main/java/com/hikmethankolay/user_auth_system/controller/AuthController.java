/**
 * @file AuthController.java
 * @brief Controller for user authentication.
 * This controller provides endpoints for user registration and authentication.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.controller
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.controller;

import com.hikmethankolay.user_auth_system.dto.ApiResponseDTO;
import com.hikmethankolay.user_auth_system.dto.AuthResponseDTO;
import com.hikmethankolay.user_auth_system.dto.LoginRequestDTO;
import com.hikmethankolay.user_auth_system.dto.UserInfoDTO;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.EApiStatus;
import com.hikmethankolay.user_auth_system.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @class AuthController
 * @brief REST controller for handling authentication requests.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** User service for handling authentication logic. */
    private final UserService userService;

    /**
     * @brief Constructor for AuthController.
     * @param userService The user service instance.
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * @brief Handles user registration.
     * @param registerRequest The user registration request data.
     * @return Response entity containing the registration result.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserInfoDTO registerRequest) {
        try {
            User registeredUser = userService.registerUser(registerRequest);
            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS,new UserInfoDTO(registeredUser),"User registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(EApiStatus.FAILURE,"",e.getMessage()));
        }
    }

    /**
     * @brief Handles user login.
     * @param loginRequest The user login request data.
     * @return Response entity containing authentication result or error message.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        String token = userService.authenticateUser(loginRequest);

        if (token != null) {
            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS,new AuthResponseDTO(token),"User authenticated successfully"));
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseDTO<>(EApiStatus.FAILURE,"","Wrong username or password"));
        }
    }
}

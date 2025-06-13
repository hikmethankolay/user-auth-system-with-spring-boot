/**
 * @file UserController.java
 * @brief Controller for managing users.
 *
 * This controller provides endpoints for retrieving, updating, and deleting users.
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
import com.hikmethankolay.user_auth_system.dto.UserDTO;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.EApiStatus;
import com.hikmethankolay.user_auth_system.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * @class UserController
 * @brief REST controller for handling user-related requests.
 */
@RestController
@RequestMapping("/api")
public class UserController {

    /** User service for handling user-related operations. */
    private final UserService userService;

    /**
     * Controller for user operations.
     * @param userService The service managing user operations.
     * @author Hikmethan Kolay
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * @brief Retrieves paginated list of users.
     * @param pageable Pagination details.
     * @return Response entity containing paginated list of users.
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponseDTO<Page<UserDTO>>> getUsers(
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable
    ) {
        Page<User> userPage = userService.findAll(pageable);
        Page<UserDTO> dtoPage = userPage.map(UserDTO::new);
        return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, dtoPage, "Users found successfully"));
    }

    /**
     * @brief Retrieves a user by ID.
     * @param id The user ID.
     * @return Response entity containing user details or error message.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponseDTO<UserDTO>> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            UserDTO userDTO = new UserDTO(user.get());
            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, userDTO, "User found successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO<>(EApiStatus.FAILURE, null, "Could not find user with id: " + id));
        }
    }

    /**
     * @brief Retrieves the logged-in user.
     * @param id The ID of the logged-in user.
     * @return Response entity containing user details or error message.
     */
    @GetMapping("/users/me")
    public ResponseEntity<ApiResponseDTO<UserDTO>> getLoggedInUser(@RequestAttribute("userId") Long id) {
        return getUserById(id);
    }

    /**
     * @brief Retrieves a user by username or email.
     * @param username The username.
     * @param email The email.
     * @return Response entity containing user details or error message.
     */
    @GetMapping(value = "/users", params = {"username"})
    public ResponseEntity<ApiResponseDTO<UserDTO>> getUserByUsername(@RequestParam String username) {
        return findUserByIdentifier(username, "username");
    }

    @GetMapping(value = "/users", params = {"email"})
    public ResponseEntity<ApiResponseDTO<UserDTO>> getUserByEmail(@RequestParam String email) {
        return findUserByIdentifier(email, "email");
    }

    /**
     * @brief Helper method to find user by identifier.
     * @param identifier The username or email.
     * @param type The type of identifier for error message.
     * @return Response entity containing user details or error message.
     */
    private ResponseEntity<ApiResponseDTO<UserDTO>> findUserByIdentifier(String identifier, String type) {
        Optional<User> user = userService.findByUsernameOrEmail(identifier);
        if (user.isPresent()) {
            UserDTO userDTO = new UserDTO(user.get());
            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, userDTO, "User found successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO<>(EApiStatus.FAILURE, null, "Could not find user with " + type + ": " + identifier));
        }
    }

    /**
     * @brief Updates a user by ID.
     * @param userDTO The user update request data.
     * @param id The user ID.
     * @param requesterId The ID of the user making the request.
     * @return Response entity containing update status.
     */
    @PatchMapping("/users/{id}")
    public ResponseEntity<ApiResponseDTO<UserDTO>> updateUser(
            @Validated(UserDTO.Update.class) @RequestBody UserDTO userDTO, 
            @PathVariable Long id, 
            @RequestAttribute("userId") Long requesterId) {
        try {
            User updatedUser = userService.updateUser(userDTO, id, requesterId);
            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, new UserDTO(updatedUser), "User updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO<>(EApiStatus.FAILURE, null, e.getMessage()));
        }
    }

    /**
     * @brief Updates logged-in user.
     * @param userDTO The user update request data.
     * @param id The ID of the logged-in user.
     * @return Response entity containing user details or error message.
     */
    @PatchMapping("/users/me")
    public ResponseEntity<ApiResponseDTO<UserDTO>> updateLoggedInUser(
            @Validated(UserDTO.Update.class) @RequestBody UserDTO userDTO, 
            @RequestAttribute("userId") Long id) {
        return updateUser(userDTO, id, id);
    }

    /**
     * @brief Deletes a user by ID.
     * @param id The user ID.
     * @param requesterId The ID of the user making the request.
     * @return Response entity containing delete status.
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUser(
            @PathVariable Long id, 
            @RequestAttribute("userId") Long requesterId) {
        try {
            userService.deleteById(id, requesterId);
            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, null, "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO<>(EApiStatus.FAILURE, null, e.getMessage()));
        }
    }
}
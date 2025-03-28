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
import com.hikmethankolay.user_auth_system.dto.UserInfoDTO;
import com.hikmethankolay.user_auth_system.dto.UserUpdateDTO;
import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.EApiStatus;
import com.hikmethankolay.user_auth_system.enums.ERole;
import com.hikmethankolay.user_auth_system.repository.UserRepository;
import com.hikmethankolay.user_auth_system.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @class UserController
 * @brief REST controller for handling user-related requests.
 */
@RestController
@RequestMapping("/api")
public class UserController {

    /** User service for handling user-related operations. */
    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * Controller for user operations.
     * @param userService The service managing user operations.
     * @param userRepository The repository handling user persistence.
     * @author Hikmethan Kolay
     */
    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * @brief Retrieves paginated list of users.
     * @param pageable Pagination details.
     * @return Response entity containing paginated list of users.
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponseDTO<Page<UserInfoDTO>>> getUsers(
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable
    ) {
        Page<User> userPage = userService.findAll(pageable);
        Page<UserInfoDTO> dtoPage = userPage.map(UserInfoDTO::new);
        return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, dtoPage, "Users found successfully"));
    }

    /**
     * @brief Retrieves a user by ID.
     * @param id The user ID.
     * @return Response entity containing user details or error message.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            UserInfoDTO userDTO = new UserInfoDTO(user.get());
            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, userDTO, "User found successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO<>(EApiStatus.FAILURE, "", "Could not find user with id: " + id));
        }
    }

    /**
     * @brief Retrieves the logged-in user.
     * @param id The ID of the logged-in user.
     * @return Response entity containing user details or error message.
     */
    @GetMapping("/users/me")
    public ResponseEntity<?> getLoggedInUser(@RequestAttribute("userId") Long id) {

        return getUserById(id);
    }

    /**
     * @brief Retrieves a user by username.
     * @param username The username.
     * @return Response entity containing user details or error message.
     */
    @GetMapping(value = "/users", params = "username")
    public ResponseEntity<?> getUserByUsername(@RequestParam String username) {
        Optional<User> user = userService.findByUsernameOrEmail(username);
        if (user.isPresent()) {
            UserInfoDTO userDTO = new UserInfoDTO(user.get());
            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, userDTO, "User found successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO<>(EApiStatus.FAILURE, "", "Could not find user with username: " + username));
        }
    }

    /**
     * @brief Retrieves a user by email.
     * @param email The username.
     * @return Response entity containing user details or error message.
     */
    @GetMapping(value = "/users", params = "email")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        Optional<User> user = userService.findByUsernameOrEmail(email);
        if (user.isPresent()) {
            UserInfoDTO userDTO = new UserInfoDTO(user.get());
            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, userDTO, "User found successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO<>(EApiStatus.FAILURE, "", "Could not find user with email: " + email));
        }
    }

    /**
     * @brief Updates a user by ID.
     * @param UserUpdateDTO The user update request data.
     * @param id The user ID.
     * @return Response entity containing update status.
     */
    @PatchMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@RequestBody UserUpdateDTO UserUpdateDTO, @PathVariable Long id, @RequestAttribute("userId") Long requesterId) {
        try {
            User updatedUser = userService.updateUser(UserUpdateDTO, id, requesterId);
            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS,new UserInfoDTO(updatedUser),"User updated successfully"));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(EApiStatus.FAILURE,"",e.getMessage()));
        }
    }

    /**
     * @brief Updates logged-in user by ID.
     * @param UserUpdateDTO The user update request data.
     * @param id The ID of the logged-in user.
     * @return Response entity containing user details or error message.
     */
    @PatchMapping("/users/me")
    public ResponseEntity<?> updateLoggedInUser(@RequestBody UserUpdateDTO UserUpdateDTO, @RequestAttribute("userId") Long id) {
        return updateUser(UserUpdateDTO, id, id);
    }

    /**
     * @brief Deletes a user by ID.
     * @param id The user ID.
     * @return Response entity containing delete status.
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @RequestAttribute("userId") Long requesterId) {
        try {
            userService.deleteById(id,requesterId);
            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS,"","User deleted successfully"));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(EApiStatus.FAILURE,"",e.getMessage()));
        }
    }
}

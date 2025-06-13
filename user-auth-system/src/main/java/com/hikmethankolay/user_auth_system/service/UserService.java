/**
 * @file UserService.java
 * @brief Service class for user management.
 *
 * This class provides methods for managing users, authentication, validation, and role assignment.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.service
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.service;

import com.hikmethankolay.user_auth_system.dto.*;
import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.ERole;
import com.hikmethankolay.user_auth_system.enums.TokenStatus;
import com.hikmethankolay.user_auth_system.repository.RoleRepository;
import com.hikmethankolay.user_auth_system.repository.UserRepository;
import com.hikmethankolay.user_auth_system.util.JwtUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @class UserService
 * @brief Service class for handling user-related operations.
 *
 * This service interacts with repositories to manage user authentication, validation, and user-related data.
 */
@Service
public class UserService {

    /** Repository for user data access. */
    private final UserRepository userRepository;

    /** Repository for role data access. */
    private final RoleRepository roleRepository;

    /** Password encoder for hashing user passwords. */
    private final PasswordEncoder passwordEncoder;

    /** Utility class for JWT operations. */
    private final JwtUtils jwtUtils;

    /** Validator for user input validation. */
    private final Validator validator;

    /** Login attempt service for tracking login attempts. */
    private final LoginAttemptService loginAttemptService;

    /**
     * @brief Constructor for UserService.
     * @param userRepository The user repository instance.
     * @param roleRepository The role repository instance.
     * @param passwordEncoder The password encoder instance.
     * @param jwtUtils The JWT utility instance.
     * @param validator The validator instance.
     */
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, Validator validator, LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.validator = validator;
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * @brief Retrieves all users with pagination.
     * @param pageable The pagination details.
     * @return A paginated list of users.
     */
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * @brief Finds a user by username or email.
     * @param identifier The username or email of the user.
     * @return An Optional containing the user if found.
     */
    public Optional<User> findByUsernameOrEmail(String identifier) {
        return userRepository.findByUsernameOrEmail(identifier, identifier);
    }

    /**
     * @brief Deletes a user by ID.
     * @param id The ID of the user to delete.
     */
    public void deleteById(Long id, Long requesterId) {
        if (id.equals(requesterId)) {
            throw new RuntimeException("Cannot delete your own account");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));

        userRepository.delete(user);
    }

    /**
     * @brief Finds a user by ID.
     * @param id The user ID.
     * @return An Optional containing the user if found.
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * @brief Registers a new user.
     * @param userDTO The user information for registration.
     * @return The newly registered user entity.
     */
    @Transactional
    public User registerUser(UserDTO userDTO) {
        // Validate using Registration group
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO, UserDTO.Registration.class);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        
        checkUserUniqueness(userDTO, null);

        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        // Assign default role
        assignRoleToUser(user, ERole.ROLE_USER);

        return userRepository.save(user);
    }

    /**
     * @brief Authenticates a user.
     * @param loginRequest The login request containing username or email and password.
     * @param clientIp The client IP for rate limiting.
     * @return A JWT token if authentication is successful, otherwise null.
     * @throws RuntimeException if the account or IP is blocked due to too many failed login attempts.
     */
    public String authenticateUser(LoginRequestDTO loginRequest, String clientIp) {
        String identifier = loginRequest.identifier();

        // Check if the IP address is blocked due to too many failed login attempts
        if (loginAttemptService.isBlocked(clientIp)) {
            throw new RuntimeException("Too many failed login attempts from this IP. Please try again later.");
        }

        // Check if the user is blocked due to too many failed attempts
        if (loginAttemptService.isBlocked(identifier)) {
            throw new RuntimeException("Account is temporarily locked due to too many failed login attempts. Please try again later.");
        }

        Optional<User> user = userRepository.findByUsernameOrEmail(identifier, identifier);

        if (user.isPresent() && passwordEncoder.matches(loginRequest.password(), user.get().getPassword())) {
            // Authentication successful - reset failed attempts counter
            loginAttemptService.loginSucceeded(clientIp);
            loginAttemptService.loginSucceeded(identifier);

            return jwtUtils.generateJwtToken(
                    String.valueOf(user.get().getId()),
                    user.get().getUsername(),
                    loginRequest.rememberMe()
            );
        } else {
            // Authentication failed - increment failed attempts counter
            loginAttemptService.loginFailed(clientIp);
            loginAttemptService.loginFailed(identifier);
            return null;
        }
    }

    /**
     * @brief Refreshes an authentication token.
     * @param token The current token to refresh.
     * @return A new JWT token if refresh is successful, otherwise null.
     */
    public String refreshToken(String token) {
        if (token != null && jwtUtils.validateJwtToken(token) != TokenStatus.INVALID) {
            try {
                Long userId = jwtUtils.getUserIdFromJwtToken(token);
                Optional<User> userOpt = findById(userId);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    boolean wasRememberMe = jwtUtils.wasRememberMe(token);

                    return jwtUtils.generateJwtToken(
                            String.valueOf(user.getId()),
                            user.getUsername(),
                            wasRememberMe
                    );
                }
            } catch (Exception e) {
                // Token processing failed
                return null;
            }
        }
        return null;
    }

    /**
     * Updates a user using the provided update DTO, applying only non-null fields.
     * Validates the update data and ensures username/email uniqueness.
     *
     * @param updates DTO with update data (must not be null)
     * @param id the ID of the user to update
     * @return the updated User
     * @throws IllegalArgumentException if updates is null
     * @throws RuntimeException if the user is not found or uniqueness checks fail
     */
    @Transactional
    public User updateUser(UserDTO updates, Long id, Long requesterId) {
        if (updates == null) {
            throw new IllegalArgumentException("Updates cannot be null");
        }

        // Validate using Update group
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(updates, UserDTO.Update.class);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        checkUserUniqueness(updates, id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found with id: " + requesterId));

        boolean isAdminAction = requester.getRole() != null && 
                requester.getRole().getName().equals(ERole.ROLE_ADMIN);

        if (StringUtils.hasText(updates.getUsername())) {
            user.setUsername(updates.getUsername().trim());
        }

        if (StringUtils.hasText(updates.getEmail())) {
            user.setEmail(updates.getEmail().trim());
        }

        if (StringUtils.hasText(updates.getPassword())) {
            user.setPassword(passwordEncoder.encode(updates.getPassword().trim()));
        }

        if (updates.getRole() != null && isAdminAction) {
            assignRoleToUser(user, updates.getRole());
        }

        return userRepository.save(user);
    }

    /**
     * Validates user uniqueness (username and email).
     *
     * @param userDTO the user DTO to validate
     * @param userId the ID of the user (to exclude self-check)
     * @throws RuntimeException if username or email is already taken
     */
    private void checkUserUniqueness(UserDTO userDTO, Long userId) {
        if (userDTO.getUsername() != null) {
            userRepository.findByUsername(userDTO.getUsername())
                    .filter(user -> !Objects.equals(user.getId(), userId))
                    .ifPresent(user -> { throw new RuntimeException("Username is already taken!"); });
        }

        if (userDTO.getEmail() != null) {
            userRepository.findByEmail(userDTO.getEmail())
                    .filter(user -> !Objects.equals(user.getId(), userId))
                    .ifPresent(user -> { throw new RuntimeException("Email is already taken!"); });
        }
    }

    /**
     * @brief Assigns a role to a user.
     * @param user The user to whom the role will be assigned.
     * @param roleName The role to assign.
     */
    private void assignRoleToUser(User user, ERole roleName) {
        Optional<Role> role = roleRepository.findByName(roleName);
        if (role.isPresent()) {
            user.setRole(role.get());
        } else {
            throw new RuntimeException("Role not found with name: " + roleName);
        }
    }
}
/**
 * @file UserService.java
 * @brief Service class for user management.
 *
 * This class provides methods for managing users, authentication, validation, and role assignments.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.service
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.service;

import com.hikmethankolay.user_auth_system.dto.LoginRequestDTO;
import com.hikmethankolay.user_auth_system.dto.UserInfo;
import com.hikmethankolay.user_auth_system.dto.UserInfoDTO;
import com.hikmethankolay.user_auth_system.dto.UserUpdateDTO;
import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.ERole;
import com.hikmethankolay.user_auth_system.repository.RoleRepository;
import com.hikmethankolay.user_auth_system.repository.UserRepository;
import com.hikmethankolay.user_auth_system.util.JwtUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    /**
     * @brief Constructor for UserService.
     * @param userRepository The user repository instance.
     * @param roleRepository The role repository instance.
     * @param passwordEncoder The password encoder instance.
     * @param jwtUtils The JWT utility instance.
     * @param validator The validator instance.
     */
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils,Validator validator) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.validator = validator;

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
    public void deleteById(Long id) {
        User user = userRepository.findById(id).orElse(null);

        if (user != null) {
            userRepository.delete(user);
        }
        else {
            throw new RuntimeException("User not found with id " + id);
        }
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
     * @param userInfoDTO The user information for registration.
     * @return The newly registered user entity.
     */
    @Transactional
    public User registerUser(UserInfoDTO userInfoDTO) {

        checkUserValidation(userInfoDTO,null);

        User user = new User();

        user.setEmail(userInfoDTO.getEmail());
        user.setPassword(userInfoDTO.getPassword());
        user.setUsername(userInfoDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userInfoDTO.getPassword()));

        Set<ERole> roles = new HashSet<>(Set.of(ERole.ROLE_USER));
        assignRolesToUser(user,roles);

        return userRepository.save(user);
    }

    /**
     * @brief Authenticates a user.
     * @param loginRequest The login request containing username or email and password.
     * @return A JWT token if authentication is successful, otherwise null.
     */
    public String authenticateUser(LoginRequestDTO loginRequest) {
        Optional<User> user = userRepository.findByUsernameOrEmail(loginRequest.identifier(), loginRequest.identifier());

        if (user.isPresent() && passwordEncoder.matches(loginRequest.password(), user.get().getPassword())) {
            return jwtUtils.generateJwtToken(String.valueOf(user.get().getId()),user.get().getUsername());
        }
        else {
            return null;
        }
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
    public User updateUser(UserUpdateDTO updates, Long id) {
        if (updates == null) {
            throw new IllegalArgumentException("Updates cannot be null");
        }

        checkUserValidation(updates, id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(ERole.ROLE_ADMIN));

        if (StringUtils.hasText(updates.getUsername())) {
            user.setUsername(updates.getUsername().trim());
        }

        if (StringUtils.hasText(updates.getEmail())) {
            user.setEmail(updates.getEmail().trim());
        }

        if (StringUtils.hasText(updates.getPassword())) {
            user.setPassword(passwordEncoder.encode(updates.getPassword().trim()));
        }

        if (updates.getRoles() != null && !updates.getRoles().isEmpty() && isAdmin) {
            user.setRoles(new HashSet<>());
            assignRolesToUser(user, updates.getRoles());
        }

        return userRepository.save(user);
    }

    /**
     * Validates user information and checks that username and email are unique.
     *
     * @param userInfo the user info to validate
     * @param userId the ID of the user (to exclude self-check)
     * @throws ConstraintViolationException if validation fails
     * @throws RuntimeException if username or email is already taken
     */
    private <T extends UserInfo> void checkUserValidation(T userInfo, Long userId) {
        Set<ConstraintViolation<T>> violations = validator.validate(userInfo);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        userRepository.findByUsername(userInfo.getUsername())
                .filter(user -> !Objects.equals(user.getId(), userId))
                .ifPresent(user -> { throw new RuntimeException("Username is already taken!"); });

        userRepository.findByEmail(userInfo.getEmail())
                .filter(user -> !Objects.equals(user.getId(), userId))
                .ifPresent(user -> { throw new RuntimeException("Email is already taken!"); });
    }



    /**
     * @brief Assigns roles to a user.
     * @param user The user to whom roles will be assigned.
     * @param roles The set of roles to assign.
     */
    private void assignRolesToUser(User user, Set<ERole> roles) {
        for (ERole roleName : roles) {
            Optional<Role> role = roleRepository.findByName(roleName);

            if (role.isPresent()) {
                user.addRole(role.get());
            } else {
                throw new RuntimeException("Role not found with name: " + roleName);
            }
        }
    }
}

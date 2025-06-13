/**
 * @file UserDTO.java
 * @brief Unified Data Transfer Object for all user operations.
 *
 * This DTO handles user registration, updates, and display operations using
 * validation groups to conditionally apply validation rules based on the
 * operation type. It provides a single, unified interface for all user-related
 * data transfer operations while maintaining security and validation integrity.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 * @version 1.0
 */

package com.hikmethankolay.user_auth_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.ERole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * @class UserDTO
 * @brief Unified Data Transfer Object for all user operations.
 *
 * This class serves as a single DTO for handling user registration, updates,
 * and display operations. It uses validation groups to apply different validation
 * rules depending on the operation context:
 * - Registration.class: Strict validation for new user registration
 * - Update.class: Lenient validation for user updates (optional fields)
 * 
 * The class includes security features such as write-only password serialization
 * to prevent password leakage in API responses while allowing password input
 * during registration and updates.
 *
 * @see User
 * @see ERole
 */
public class UserDTO {

    /**
     * @brief Unique identifier for the user.
     * 
     * This field represents the primary key of the user entity.
     * It is typically null during registration and populated for existing users.
     */
    private Long id;

    /**
     * @brief Username of the user.
     * 
     * Must be unique across the system and follow length constraints.
     * Validation:
     * - Required for registration (Registration.class)
     * - Length: 8-32 characters for both registration and updates
     * - Optional for updates (can be null or empty)
     */
    @NotBlank(message = "Username cannot be blank.", groups = Registration.class)
    @Size(min = 8, max = 32, message = "Username must be between 8 and 32 characters", 
          groups = {Registration.class, Update.class})
    private String username;

    /**
     * @brief Email address of the user.
     * 
     * Must be unique across the system and follow valid email format.
     * Validation:
     * - Required for registration (Registration.class)
     * - Must be valid email format for both registration and updates
     * - Optional for updates (can be null or empty)
     */
    @NotBlank(message = "Email cannot be blank.", groups = Registration.class)
    @Email(message = "Invalid email format", groups = {Registration.class, Update.class})
    private String email;

    /**
     * @brief Password of the user.
     * 
     * This field is write-only for security purposes, meaning it can be
     * deserialized from JSON (for input) but will never be serialized to JSON
     * (for output), preventing password exposure in API responses.
     * 
     * Validation:
     * - Required for registration (Registration.class)
     * - Must contain: uppercase, lowercase, digit, special character
     * - Length: 8-32 characters
     * - Optional for updates (allows password changes)
     */
    @NotBlank(message = "Password cannot be blank.", groups = Registration.class)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Pattern(
            regexp = "^(?=.*?[0-9])(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[^0-9A-Za-z]).{8,32}$",
            message = "Password must be 8-32 characters and include at least: one uppercase letter, one lowercase letter, one digit, and one special character.",
            groups = {Registration.class, Update.class}
    )
    private String password;

    /**
     * @brief Role assigned to the user.
     * 
     * Represents the user's role in the system (USER, MODERATOR, ADMIN).
     * This field is typically set by administrators and not directly
     * modifiable by regular users during registration.
     */
    private ERole role;

    /**
     * @interface Registration
     * @brief Validation group for user registration operations.
     * 
     * This validation group applies strict validation rules where most fields
     * are required. Used when creating new user accounts.
     */
    public interface Registration {}

    /**
     * @interface Update
     * @brief Validation group for user update operations.
     * 
     * This validation group applies lenient validation rules where fields
     * are optional but must meet format requirements if provided.
     * Used when modifying existing user accounts.
     */
    public interface Update {}

    /**
     * @brief Default constructor.
     * 
     * Creates an empty UserDTO instance. All fields will be null
     * and must be set using the appropriate setter methods.
     */
    public UserDTO() {}

    /**
     * @brief Constructor that creates UserDTO from User entity.
     * 
     * This constructor is used to convert a User entity to a DTO for
     * API responses. The password field is intentionally not copied
     * for security reasons.
     * 
     * @param user The user entity to convert to DTO
     * @throws NullPointerException if user is null (handled gracefully)
     */
    public UserDTO(User user) {
        if (user != null) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.role = user.getRole() != null ? user.getRole().getName() : null;
            // Password is not set for security (read operations)
        }
    }

    /**
     * @brief Gets the user ID.
     * 
     * @return The unique identifier of the user, or null if not set
     */
    public Long getId() { return id; }

    /**
     * @brief Sets the user ID.
     * 
     * @param id The unique identifier to set for the user
     */
    public void setId(Long id) { this.id = id; }

    /**
     * @brief Gets the username.
     * 
     * @return The username of the user, or null if not set
     */
    public String getUsername() { return username; }

    /**
     * @brief Sets the username.
     * 
     * @param username The username to set for the user
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * @brief Gets the email address.
     * 
     * @return The email address of the user, or null if not set
     */
    public String getEmail() { return email; }

    /**
     * @brief Sets the email address.
     * 
     * @param email The email address to set for the user
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * @brief Gets the password.
     * 
     * Note: This method will return the password value but due to the
     * @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) annotation,
     * the password will never be serialized to JSON responses.
     * 
     * @return The password of the user, or null if not set
     */
    public String getPassword() { return password; }

    /**
     * @brief Sets the password.
     * 
     * @param password The password to set for the user
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * @brief Gets the user role.
     * 
     * @return The role assigned to the user, or null if not set
     */
    public ERole getRole() { return role; }

    /**
     * @brief Sets the user role.
     * 
     * @param role The role to assign to the user
     */
    public void setRole(ERole role) { this.role = role; }

    /**
     * @brief Returns a string representation of the UserDTO.
     * 
     * This method provides a readable string representation of the UserDTO
     * object for debugging and logging purposes. The password field is
     * intentionally excluded for security reasons.
     * 
     * @return A string representation containing id, username, email, and role
     */
    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
} 
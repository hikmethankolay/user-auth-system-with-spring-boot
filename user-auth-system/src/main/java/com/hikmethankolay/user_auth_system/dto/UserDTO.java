/**
 * @file UserDTO.java
 * @brief Unified Data Transfer Object for all user operations.
 *
 * This DTO handles user registration, updates, and display operations.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
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
 * @brief Unified DTO for all user operations.
 *
 * This class handles registration, updates, and display of user information.
 */
public class UserDTO {

    /** User ID. */
    private Long id;

    /** Username of the user. */
    @NotBlank(message = "Username cannot be blank.", groups = Registration.class)
    @Size(min = 8, max = 32, message = "Username must be between 8 and 32 characters", 
          groups = {Registration.class, Update.class})
    private String username;

    /** Email of the user. */
    @NotBlank(message = "Email cannot be blank.", groups = Registration.class)
    @Email(message = "Invalid email format", groups = {Registration.class, Update.class})
    private String email;

    /** Password of the user. */
    @NotBlank(message = "Password cannot be blank.", groups = Registration.class)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Pattern(
            regexp = "^(?=.*?[0-9])(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[^0-9A-Za-z]).{8,32}$",
            message = "Password must be 8-32 characters and include at least: one uppercase letter, one lowercase letter, one digit, and one special character.",
            groups = {Registration.class, Update.class}
    )
    private String password;

    /** Role assigned to the user. */
    private ERole role;

    // Validation groups
    public interface Registration {}
    public interface Update {}

    /**
     * @brief Default constructor.
     */
    public UserDTO() {}

    /**
     * @brief Constructs a UserDTO from a User entity.
     * @param user The user entity.
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

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public ERole getRole() { return role; }
    public void setRole(ERole role) { this.role = role; }

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
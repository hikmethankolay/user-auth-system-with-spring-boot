/**
 * @file UserUpdateDTO.java
 * @brief Data Transfer Object for updating user information.
 *
 * This DTO is used for updating user details such as username, email, password, and roles.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.dto
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.ERole;
import com.hikmethankolay.user_auth_system.validator.ValidRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @class UserUpdateDTO
 * @brief DTO for updating user information.
 *
 * This class represents the user information that can be updated.
 */
public class UserUpdateDTO implements UserInfo {

    /** User ID. */
    private Long id;

    /** Updated username of the user. */
    @Size(min = 8, max = 32, message = "Username must be between 8 and 32 characters")
    private String username;

    /** Updated email of the user. */
    @Email(message = "Invalid email format")
    private String email;

    /** Updated password of the user. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Pattern(
            regexp = "^(?=.*?[0-9])(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[^0-9A-Za-z]).{8,32}$",
            message = "Password must be at least 8 characters long, include one uppercase letter, one lowercase letter, one digit, and one special character."
    )
    private String password;

    /** Updated roles assigned to the user. */
    @ValidRole
    private Set<ERole> roles;

    /**
     * @brief Default constructor.
     */
    public UserUpdateDTO() {}

    /**
     * @brief Constructs a UserUpdateDTO from a User entity.
     * @param user The user entity.
     */
    public UserUpdateDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.roles = user.getRoles() != null
                ? user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
                : Set.of();
    }

    /**
     * @brief Gets the user ID.
     * @return The ID of the user.
     */
    public Long getId() {
        return id;
    }

    /**
     * @brief Sets the user ID.
     * @param id The ID to be set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @brief Gets the username of the user.
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @brief Sets the username of the user.
     * @param username The username to be set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @brief Gets the email of the user.
     * @return The email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * @brief Sets the email of the user.
     * @param email The email to be set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @brief Gets the password of the user.
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @brief Sets the password of the user.
     * @param password The password to be set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @brief Gets the roles assigned to the user.
     * @return A set of roles.
     */
    public Set<ERole> getRoles() {
        return roles;
    }

    /**
     * @brief Sets the roles assigned to the user.
     * @param roles The set of roles to be assigned.
     */
    public void setRoles(Set<ERole> roles) {
        this.roles = roles;
    }

    /**
     * @brief Converts the UserUpdateDTO object to a string representation.
     * @return A string representation of UserUpdateDTO.
     */
    @Override
    public String toString() {
        return "UserUpdateDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", roles=" + roles +
                '}';
    }
}

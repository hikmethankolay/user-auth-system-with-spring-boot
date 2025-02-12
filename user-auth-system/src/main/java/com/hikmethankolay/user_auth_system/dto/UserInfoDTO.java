/**
 * @file UserInfoDTO.java
 * @brief Data Transfer Object for user information.
 *
 * This DTO encapsulates user details including username, email, password, and roles.
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @class UserInfoDTO
 * @brief DTO for user information.
 *
 * This class represents user details including credentials and roles.
 */
public class UserInfoDTO implements UserInfo {

        /** User ID. */
        private Long id;

        /** Username of the user. */
        @NotBlank(message = "Username can not be blank.")
        @Size(min = 8, max = 32, message = "Username must be between 8 and 32 characters")
        private String username;

        /** Email of the user. */
        @NotBlank(message = "Email can not be blank.")
        @Email(message = "Invalid email format")
        private String email;

        /** Password of the user. */
        @NotBlank(message = "Password can not be blank.")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @Pattern(
                regexp = "^(?=.*?[0-9])(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[^0-9A-Za-z]).{8,32}$",
                message = "Password must be at least 8 characters long, include one uppercase letter, one lowercase letter, one digit, and one special character."
        )
        private String password;

        /** Set of roles assigned to the user. */
        @ValidRole
        private Set<ERole> roles;

        /**
         * @brief Default constructor.
         */
        public UserInfoDTO() {}

        /**
         * @brief Constructs a UserInfoDTO from a User entity.
         * @param user The user entity.
         */
        public UserInfoDTO(User user) {
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
         * @brief Converts the UserInfoDTO object to a string representation.
         * @return A string representation of UserInfoDTO.
         */
        @Override
        public String toString() {
                return "UserInfoDTO{" +
                        "id=" + id +
                        ", username='" + username + '\'' +
                        ", email='" + email + '\'' +
                        ", password='" + password + '\'' +
                        ", roles=" + roles +
                        '}';
        }
}

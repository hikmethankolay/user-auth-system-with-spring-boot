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

import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.ERole;
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
        private String username;

        /** Email of the user. */
        private String email;

        /** Set of roles assigned to the user. */
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
                        ", roles=" + roles +
                        '}';
        }
}

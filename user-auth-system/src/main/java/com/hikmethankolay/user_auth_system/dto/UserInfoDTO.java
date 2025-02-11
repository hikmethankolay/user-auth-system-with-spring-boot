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

public class UserInfoDTO implements UserInfo {

        private Long id;

        @NotBlank(message = "Username can not be blank.")
        @Size(min = 8, max = 32, message = "Username must be between 8 and 32 characters")
        private String username;

        @NotBlank(message = "Email can not be blank.")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password can not be blank.")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @Pattern(
                regexp = "^(?=.*?[0-9])(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[^0-9A-Za-z]).{8,32}$",
                message = "Password must be at least 8 characters long, include one uppercase letter, one lowercase letter, one digit, and one special character."
        )
        private String password;

        @ValidRole
        private Set<ERole> roles;

        // Default constructor
        public UserInfoDTO() {
        }

        // Constructor from User entity
        public UserInfoDTO(User user) {
                this.id = user.getId();
                this.username = user.getUsername();
                this.email = user.getEmail();
                this.password = user.getPassword();
                this.roles = user.getRoles() != null
                        ? user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
                        : Set.of();
        }


        public Long Id() {
                return id;
        }

        public void setId(Long id) {
                this.id = id;
        }

        public String username() {
                return username;
        }

        public void setUsername(String username) {
                this.username = username;
        }

        public String email() {
                return email;
        }

        public void setEmail(String email) {
                this.email = email;
        }

        public String password() {
                return password;
        }

        public void setPassword(String password) {
                this.password = password;
        }

        public Set<ERole> roles() {
                return roles;
        }

        public void setRoles(Set<ERole> roles) {
                this.roles = roles;
        }

        // toString method (optional, for debugging)
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
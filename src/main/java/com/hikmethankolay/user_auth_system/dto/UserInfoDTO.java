package com.hikmethankolay.user_auth_system.dto;

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

public record UserInfoDTO(

        Long id,

        @Size(min = 8, max = 32, message = "Username must be between 8 and 32 characters")
        @NotBlank(message = "Username cannot be blank")
        String username,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email cannot be blank")
        String email,

        @Pattern(
                regexp = "^(?=.*?[0-9])(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[^0-9A-Za-z]).{8,32}$",
                message = "Password must be at least 8 characters long, include one uppercase letter, one lowercase letter, one digit, and one special character."
        )
        String password,

        @ValidRole
        Set<ERole> roles
) {
        public UserInfoDTO(User user) {
                this(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getRoles() != null
                                ? user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
                                : Set.of()
                );
        }

}

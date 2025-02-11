package com.hikmethankolay.user_auth_system.dto;

import com.hikmethankolay.user_auth_system.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

public record RoleUserInfoDTO(
        Long id,

        String username,

        String email,

        String password
) {
    public RoleUserInfoDTO(User user) {
        this(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword()
        );
    }
}

package com.hikmethankolay.user_auth_system.dto;

import com.hikmethankolay.user_auth_system.entity.Role;

public record UserRoleInfoDTO(String name) {
    public UserRoleInfoDTO(Role role) {
        this(
                role.getName().name()
        );
    }
}

package com.hikmethankolay.user_auth_system.dto;

import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

public record RoleInfoDTO(
        String name,
        Set<RoleUserInfoDTO> users
) {
    public RoleInfoDTO(Role role) {
        this(
                role.getName().name(),
                role.getUsers() != null
                        ? role.getUsers().stream().map(RoleUserInfoDTO::new).collect(Collectors.toSet())
                        : Set.of()
        );
    }
}

package com.hikmethankolay.user_auth_system.dto;

import com.hikmethankolay.user_auth_system.entity.Role;

import java.util.Set;

public record UserResponseDTO(String username, String email, Set<Role> roles) {
}

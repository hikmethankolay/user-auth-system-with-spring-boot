package com.hikmethankolay.user_auth_system.dto;

import com.hikmethankolay.user_auth_system.entity.Role;

import java.util.Set;

public record UpdateUserDTO(String username, String email, String password, Set<Role> roles) {
}

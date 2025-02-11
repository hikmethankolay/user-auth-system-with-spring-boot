package com.hikmethankolay.user_auth_system.dto;

public record AuthResponseDTO(String token, String tokenType) {

    public AuthResponseDTO(String token) {
        this(token, "Bearer");
    }
}
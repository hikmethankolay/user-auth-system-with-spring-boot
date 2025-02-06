package com.hikmethankolay.user_auth_system.dto;

public record ApiResponseDTO<T>(String status, T data, String message) {
}

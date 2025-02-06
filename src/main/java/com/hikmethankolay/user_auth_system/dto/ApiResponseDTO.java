package com.hikmethankolay.user_auth_system.dto;

import com.hikmethankolay.user_auth_system.enums.EApiStatus;

public record ApiResponseDTO<T>(EApiStatus status, T data, String message) {
}

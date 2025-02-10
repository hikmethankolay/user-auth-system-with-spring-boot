package com.hikmethankolay.user_auth_system.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hikmethankolay.user_auth_system.dto.ApiResponseDTO;
import com.hikmethankolay.user_auth_system.enums.EApiStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

        private final ApiKeyConfig apiKeyConfig;

        public ApiKeyInterceptor(ApiKeyConfig apiKeyConfig) {
            this.apiKeyConfig = apiKeyConfig;
        }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {
        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey == null || !apiKey.equals(apiKeyConfig.getKey())) {
            ApiResponseDTO<String> apiResponse = new ApiResponseDTO<>(
                    EApiStatus.UNAUTHORIZED, "", "Invalid API Key");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");

            response.getWriter().write(jsonResponse);
            return false;
        }

        return true;
    }
}

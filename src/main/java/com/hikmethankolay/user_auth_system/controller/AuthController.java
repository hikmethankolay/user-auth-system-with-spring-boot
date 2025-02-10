package com.hikmethankolay.user_auth_system.controller;

import com.hikmethankolay.user_auth_system.dto.ApiResponseDTO;
import com.hikmethankolay.user_auth_system.dto.AuthResponseDTO;
import com.hikmethankolay.user_auth_system.dto.LoginRequestDTO;
import com.hikmethankolay.user_auth_system.dto.UserInfoDTO;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.EApiStatus;
import com.hikmethankolay.user_auth_system.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserInfoDTO registerRequest) {
        try {
            User registeredUser = userService.registerUser(registerRequest);
            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS,new UserInfoDTO(registeredUser),"User registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(EApiStatus.FAILURE,"",e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        String token = userService.authenticateUser(loginRequest);

        if (token != null) {
            return ResponseEntity.ok(new AuthResponseDTO(token));
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseDTO<>(EApiStatus.FAILURE,"","Wrong username or password"));
        }
    }
}

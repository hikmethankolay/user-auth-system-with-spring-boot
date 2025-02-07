package com.hikmethankolay.user_auth_system.controller;

import com.hikmethankolay.user_auth_system.dto.ApiResponseDTO;
import com.hikmethankolay.user_auth_system.dto.UserInfoDTO;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.EApiStatus;
import com.hikmethankolay.user_auth_system.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponseDTO<List<UserInfoDTO>>> getUsers() {
        List<UserInfoDTO> userDTOs = userService.findAll().stream()
                .map(UserInfoDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, userDTOs, "Users found successfully"));
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);

        if (user.isPresent()) {
            UserInfoDTO userDTO = new UserInfoDTO(user.get());

            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, userDTO, "User found successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO<>(EApiStatus.FAILURE, "", "Could not find user with id: " + id));
        }
    }

    @GetMapping("identifier/{identifier}")
    public ResponseEntity<?> getUserByIdentifier(@PathVariable String identifier) {
        Optional<User> user = userService.findByUsernameOrEmail(identifier);

        if (user.isPresent()) {
            UserInfoDTO userDTO = new UserInfoDTO(user.get());

            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, userDTO, "User found successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO<>(EApiStatus.FAILURE, "", "Could not find user with identifier: " + identifier));
        }
    }



    @PutMapping("/{id}/update")
    public ResponseEntity<?> updateUser(@RequestBody UserInfoDTO userInfoDTO, @PathVariable long id) {
        try {
            userService.updateUser(userInfoDTO, id);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(EApiStatus.FAILURE,"",e.getMessage()));
        }

        return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS,"","User updated successfully"));
    }

}

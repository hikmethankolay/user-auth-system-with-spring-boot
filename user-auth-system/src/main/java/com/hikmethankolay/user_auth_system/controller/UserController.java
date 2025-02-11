package com.hikmethankolay.user_auth_system.controller;

import com.hikmethankolay.user_auth_system.dto.ApiResponseDTO;
import com.hikmethankolay.user_auth_system.dto.UserInfoDTO;
import com.hikmethankolay.user_auth_system.dto.UserUpdateDTO;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.EApiStatus;
import com.hikmethankolay.user_auth_system.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponseDTO<Page<UserInfoDTO>>> getUsers(
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable
    ) {
        Page<User> userPage = userService.findAll(pageable);

        Page<UserInfoDTO> dtoPage = userPage.map(UserInfoDTO::new);

        return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, dtoPage, "Users found successfully"));
    }


    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);

        if (user.isPresent()) {
            UserInfoDTO userDTO = new UserInfoDTO(user.get());

            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, userDTO, "User found successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO<>(EApiStatus.FAILURE, "", "Could not find user with id: " + id));
        }
    }

    @GetMapping(value = "/users", params = "username")
    public ResponseEntity<?> getUserByUsername(@RequestParam String username) {
        Optional<User> user = userService.findByUsernameOrEmail(username);

        if (user.isPresent()) {
            UserInfoDTO userDTO = new UserInfoDTO(user.get());

            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, userDTO, "User found successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO<>(EApiStatus.FAILURE, "", "Could not find user with username: " + username));
        }
    }

    @GetMapping(value = "/users", params = "email")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        Optional<User> user = userService.findByUsernameOrEmail(email);

        if (user.isPresent()) {
            UserInfoDTO userDTO = new UserInfoDTO(user.get());

            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, userDTO, "User found successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO<>(EApiStatus.FAILURE, "", "Could not find user with email: " + email));
        }
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@RequestBody UserUpdateDTO UserUpdateDTO, @PathVariable long id) {
        try {
            User updatedUser = userService.updateUser(UserUpdateDTO, id);
            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS,new UserInfoDTO(updatedUser),"User updated successfully"));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(EApiStatus.FAILURE,"",e.getMessage()));
        }

    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteById(id);
            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS,"","User deleted successfully"));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(EApiStatus.FAILURE,"",e.getMessage()));
        }
    }

}

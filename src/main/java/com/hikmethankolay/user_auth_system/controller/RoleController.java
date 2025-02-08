package com.hikmethankolay.user_auth_system.controller;

import com.hikmethankolay.user_auth_system.dto.ApiResponseDTO;
import com.hikmethankolay.user_auth_system.dto.RoleInfoDTO;
import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.enums.EApiStatus;
import com.hikmethankolay.user_auth_system.enums.ERole;
import com.hikmethankolay.user_auth_system.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RoleController {


    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/roles")
    public ResponseEntity<?> findAll() {
        List<RoleInfoDTO> roleInfoDTOS = roleService.findAll().stream().map(RoleInfoDTO::new).toList();
        return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, roleInfoDTOS, "Roles found successfully"));
    }

    @GetMapping("/roles/{name}")
    public ResponseEntity<?> findByName(@PathVariable ERole name) {
        Optional<Role> role = roleService.findByName(name);

        if (role.isPresent()) {
            RoleInfoDTO roleInfoDTO = new RoleInfoDTO(role.get());

            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, roleInfoDTO, "Role found successfully"));
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO<>(EApiStatus.FAILURE, "", "Could not find role with name: " + name));
        }

    }

}

/**
 * @file RoleController.java
 * @brief Controller for managing user roles.
 * This controller provides endpoints for retrieving user roles.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */


/**
 * @package com.hikmethankolay.user_auth_system.controller
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.controller;

import com.hikmethankolay.user_auth_system.dto.ApiResponseDTO;
import com.hikmethankolay.user_auth_system.dto.RoleInfoDTO;
import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.enums.EApiStatus;
import com.hikmethankolay.user_auth_system.enums.ERole;
import com.hikmethankolay.user_auth_system.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @class RoleController
 * @brief REST controller for handling role-related requests.
 */
@RestController
@RequestMapping("/api")
public class RoleController {

    /** Role service for handling role-related operations. */
    private final RoleService roleService;

    /**
     * @brief Constructor for RoleController.
     * @param roleService The role service instance.
     */
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * @brief Retrieves all roles.
     * @return Response entity containing a list of all roles.
     */
    @GetMapping("/roles")
    public ResponseEntity<?> findAll() {
        List<RoleInfoDTO> roleInfoDTOS = roleService.findAll().stream().map(RoleInfoDTO::new).toList();
        return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, roleInfoDTOS, "Roles found successfully"));
    }

    /**
     * @brief Retrieves a role by its name.
     * @param name The name of the role to retrieve.
     * @return Response entity containing the role information or an error message if not found.
     */
    @GetMapping("/roles/{name}")
    public ResponseEntity<?> findByName(@PathVariable ERole name) {
        Optional<Role> role = roleService.findByName(name);

        if (role.isPresent()) {
            RoleInfoDTO roleInfoDTO = new RoleInfoDTO(role.get());
            return ResponseEntity.ok(new ApiResponseDTO<>(EApiStatus.SUCCESS, roleInfoDTO, "Role found successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ApiResponseDTO<>(EApiStatus.FAILURE, "", "Could not find role with name: " + name));
        }
    }
}
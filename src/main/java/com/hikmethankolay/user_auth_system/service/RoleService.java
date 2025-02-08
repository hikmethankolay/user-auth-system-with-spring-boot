package com.hikmethankolay.user_auth_system.service;

import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.enums.ERole;
import com.hikmethankolay.user_auth_system.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    RoleRepository roleRepository;

    RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Optional<Role> findByName(ERole name) {
        return roleRepository.findByName(name);
    }
}

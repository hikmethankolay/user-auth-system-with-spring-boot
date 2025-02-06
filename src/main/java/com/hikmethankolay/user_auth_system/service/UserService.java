package com.hikmethankolay.user_auth_system.service;

import com.hikmethankolay.user_auth_system.dto.LoginRequestDTO;
import com.hikmethankolay.user_auth_system.dto.RegisterRequestDTO;
import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.ERole;
import com.hikmethankolay.user_auth_system.repository.RoleRepository;
import com.hikmethankolay.user_auth_system.repository.UserRepository;
import com.hikmethankolay.user_auth_system.util.JwtUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final Validator validator;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils,Validator validator) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.validator = validator;

    }

    public void registerUser(RegisterRequestDTO registerRequestDTO) {

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(registerRequestDTO);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        if (userRepository.findByUsername(registerRequestDTO.username()).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.findByEmail(registerRequestDTO.email()).isPresent()) {
            throw new RuntimeException("Email is already taken!");
        }

        User user = new User();

        user.setEmail(registerRequestDTO.email());
        user.setPassword(registerRequestDTO.password());
        user.setUsername(registerRequestDTO.username());
        user.setPassword(passwordEncoder.encode(registerRequestDTO.password()));

        Optional<Role> role = roleRepository.findByName(ERole.ROLE_USER);

        if (role.isPresent()) {
            user.addRole(role.get());
        }
        else{
            Role new_role = new Role(ERole.ROLE_USER);
            roleRepository.save(new_role);
            user.addRole(new_role);
        }

        userRepository.save(user);
    }

    public String authenticateUser(LoginRequestDTO loginRequest) {
        Optional<User> user = userRepository.findByUsernameOrEmail(loginRequest.identifier(), loginRequest.identifier());

        if (user.isPresent() && passwordEncoder.matches(loginRequest.password(), user.get().getPassword())) {
            return jwtUtils.generateJwtToken(user.get().getUsername());
        }
        else {
            return null;
        }
    }
}

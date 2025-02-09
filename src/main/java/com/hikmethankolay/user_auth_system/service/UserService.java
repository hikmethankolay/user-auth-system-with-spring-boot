package com.hikmethankolay.user_auth_system.service;

import com.hikmethankolay.user_auth_system.dto.LoginRequestDTO;
import com.hikmethankolay.user_auth_system.dto.UserInfoDTO;
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

import java.util.*;

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

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findByUsernameOrEmail(String identifier) {
        return userRepository.findByUsernameOrEmail(identifier,identifier);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void registerUser(UserInfoDTO userInfoDTO) {

        checkUserValidation(userInfoDTO);

        User user = new User();

        user.setEmail(userInfoDTO.email());
        user.setPassword(userInfoDTO.password());
        user.setUsername(userInfoDTO.username());
        user.setPassword(passwordEncoder.encode(userInfoDTO.password()));

        Set<ERole> roles = new HashSet<>(Set.of(ERole.ROLE_USER));
        assignRolesToUser(user,roles);

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

    public void updateUser(UserInfoDTO userInfoDTO, Long Id) {

        checkUserValidation(userInfoDTO);

        Optional<User> user = userRepository.findById(Id);
        if (user.isPresent()) {
           User updatedUser = user.get();
           updatedUser.setUsername(userInfoDTO.username());
           updatedUser.setPassword(passwordEncoder.encode(userInfoDTO.password()));
           updatedUser.setEmail(userInfoDTO.email());

            assignRolesToUser(updatedUser,userInfoDTO.roles());

            userRepository.save(updatedUser);
        }
        else {

            throw new RuntimeException("User not found with id: " + Id);

        }
    }

    private void checkUserValidation(UserInfoDTO userInfoDTO) {
        Set<ConstraintViolation<UserInfoDTO>> violations = validator.validate(userInfoDTO);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        if (userRepository.findByUsername(userInfoDTO.username()).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.findByEmail(userInfoDTO.email()).isPresent()) {
            throw new RuntimeException("Email is already taken!");
        }
    }

    private void assignRolesToUser(User user, Set<ERole> roles) {
        for (ERole roleName : roles) {
            Optional<Role> role = roleRepository.findByName(roleName);

            if (role.isPresent()) {
                user.addRole(role.get());
            } else {
                Role newRole = new Role(roleName);
                roleRepository.save(newRole);
                user.addRole(newRole);
            }
        }
    }

}

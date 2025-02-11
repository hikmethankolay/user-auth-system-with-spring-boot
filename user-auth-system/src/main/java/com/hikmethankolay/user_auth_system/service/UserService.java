package com.hikmethankolay.user_auth_system.service;

import com.hikmethankolay.user_auth_system.dto.LoginRequestDTO;
import com.hikmethankolay.user_auth_system.dto.UserInfo;
import com.hikmethankolay.user_auth_system.dto.UserInfoDTO;
import com.hikmethankolay.user_auth_system.dto.UserUpdateDTO;
import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.ERole;
import com.hikmethankolay.user_auth_system.repository.RoleRepository;
import com.hikmethankolay.user_auth_system.repository.UserRepository;
import com.hikmethankolay.user_auth_system.util.JwtUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

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

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> findByUsernameOrEmail(String identifier) {
        return userRepository.findByUsernameOrEmail(identifier,identifier);
    }

    public void deleteById(Long id) {
        User user = userRepository.findById(id).orElse(null);

        if (user != null) {
            userRepository.delete(user);
        }
        else {
            throw new RuntimeException("User not found with id " + id);
        }
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User registerUser(UserInfoDTO userInfoDTO) {

        checkUserValidation(userInfoDTO,null);

        User user = new User();

        user.setEmail(userInfoDTO.email());
        user.setPassword(userInfoDTO.password());
        user.setUsername(userInfoDTO.username());
        user.setPassword(passwordEncoder.encode(userInfoDTO.password()));

        Set<ERole> roles = new HashSet<>(Set.of(ERole.ROLE_USER));
        assignRolesToUser(user,roles);

        return userRepository.save(user);
    }

    public String authenticateUser(LoginRequestDTO loginRequest) {
        Optional<User> user = userRepository.findByUsernameOrEmail(loginRequest.identifier(), loginRequest.identifier());

        if (user.isPresent() && passwordEncoder.matches(loginRequest.password(), user.get().getPassword())) {
            return jwtUtils.generateJwtToken(String.valueOf(user.get().getId()),user.get().getUsername());
        }
        else {
            return null;
        }
    }

    @Transactional
    public User updateUser(UserUpdateDTO updates, Long id) {
        if (updates == null) {
            throw new IllegalArgumentException("Updates cannot be null");
        }

        checkUserValidation(updates, id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Update only if the field is not null
        if (updates.username() != null) {
            user.setUsername(updates.username());
        }

        if (updates.email() != null) {
            user.setEmail(updates.email());
        }

        if (updates.password() != null) {
            user.setPassword(passwordEncoder.encode(updates.password()));
        }

        if (updates.roles() != null) {
            user.setRoles(new HashSet<>());
            assignRolesToUser(user, updates.roles());
        }

        return userRepository.save(user);
    }

    private <T extends UserInfo> void checkUserValidation (T userInfo, Long userId) {
        Set<ConstraintViolation<T>> violations = validator.validate(userInfo);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        userRepository.findByUsername(userInfo.username())
                .filter(user -> !Objects.equals(user.getId(), userId))
                .ifPresent(user -> { throw new RuntimeException("Username is already taken!"); });


        userRepository.findByEmail(userInfo.email())
                .filter(user -> !Objects.equals(user.getId(), userId))
                .ifPresent(user -> { throw new RuntimeException("Email is already taken!"); });

    }


    private void assignRolesToUser(User user, Set<ERole> roles) {
        for (ERole roleName : roles) {
            Optional<Role> role = roleRepository.findByName(roleName);

            if (role.isPresent()) {
                user.addRole(role.get());
            } else {
                throw new RuntimeException("Role not found with name: " + roleName);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}

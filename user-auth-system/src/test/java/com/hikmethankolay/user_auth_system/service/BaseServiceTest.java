/**
 * @file BaseServiceTest.java
 * @brief Base test class for service tests.
 *
 * This class provides a common setup for service tests, including mocking
 * common dependencies.
 *
 * @author Test Suite Generator
 * @date 2025-03-29
 */
package com.hikmethankolay.user_auth_system.service;

import com.hikmethankolay.user_auth_system.repository.RoleRepository;
import com.hikmethankolay.user_auth_system.repository.UserRepository;

import com.hikmethankolay.user_auth_system.util.JwtUtils;
import jakarta.validation.Validator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * @class BaseServiceTest
 * @brief Base class for service tests.
 *
 * Provides common mocks and mockito extension for service tests.
 */
@SpringBootTest
public abstract class BaseServiceTest {

    /**
     * Mock UserRepository for service dependencies.
     */
    @MockitoBean
    protected UserRepository userRepository;

    /**
     * Mock RoleRepository for service dependencies.
     */
    @MockitoBean
    protected RoleRepository roleRepository;

    /**
     * Mock PasswordEncoder for service dependencies.
     */
    @MockitoBean
    protected PasswordEncoder passwordEncoder;

    /**
     * Mock JwtUtils for service dependencies.
     */
    @MockitoBean
    protected JwtUtils jwtUtils;

    /**
     * Mock Validator for service dependencies.
     */
    @MockitoBean
    protected Validator validator;

    /**
     * Mock LoginAttemptService for service dependencies.
     */
    @MockitoBean
    protected LoginAttemptService loginAttemptService;
}
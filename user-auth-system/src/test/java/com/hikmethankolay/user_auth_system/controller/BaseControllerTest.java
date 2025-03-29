/**
 * @file BaseControllerTest.java
 * @brief Base test class for controller tests.
 *
 * This class provides a common setup for controller tests, including mocking
 * common dependencies and setting up MockMvc.
 *
 * @author Test Suite Generator
 * @date 2025-03-29
 */
package com.hikmethankolay.user_auth_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hikmethankolay.user_auth_system.service.RoleService;
import com.hikmethankolay.user_auth_system.service.UserService;
import com.hikmethankolay.user_auth_system.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;



/**
 * @class BaseControllerTest
 * @brief Base class for controller tests.
 *
 * Provides common setup and mockito extension for controller tests.
 */
@AutoConfigureMockMvc
@SpringBootTest
public abstract class BaseControllerTest {

    /**
     * Web application context for controller tests.
     */
    @Autowired
    protected WebApplicationContext webApplicationContext;

    /**
     * MockMvc instance for simulating HTTP requests.
     */
    protected MockMvc mockMvc;

    /**
     * Object mapper for serializing and deserializing JSON.
     */
    protected ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Mock UserService for controller dependencies.
     */
    @MockitoBean
    protected UserService userService;

    /**
     * Mock RoleService for controller dependencies.
     */
    @MockitoBean
    protected RoleService roleService;

    /**
     * Mock JwtUtils for JWT operations.
     */
    @MockitoBean
    protected JwtUtils jwtUtils;

    /**
     * @brief Setup method that runs before each test.
     *
     * Initializes MockMvc with web application context.
     */
    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }
}
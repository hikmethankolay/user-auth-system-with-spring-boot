/**
 * @file ValidRolesValidatorTest.java
 * @brief Unit tests for ValidRolesValidator class.
 *
 * This file contains JUnit tests using Spring Boot Test framework to verify the functionality of the ValidRolesValidator class.
 * The tests ensure that role validation correctly allows valid roles and rejects invalid ones.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-16
 */

/**
 * @package com.hikmethankolay.user_auth_system
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system;

import com.hikmethankolay.user_auth_system.enums.ERole;
import com.hikmethankolay.user_auth_system.validator.ValidRolesValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @class ValidRolesValidatorTest
 * @brief Unit tests for ValidRolesValidator class.
 *
 * This test class verifies that the role validator correctly validates allowed roles.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ValidRolesValidatorTest {

    /**
     * Instance of ValidRolesValidator to be tested.
     */
    private ValidRolesValidator validator;

    /**
     * Mock of ConstraintValidatorContext for testing purposes.
     */
    @MockitoBean
    private ConstraintValidatorContext context;

    /**
     * @brief Sets up test preconditions.
     *
     * Initializes the validator and a mock validation context before each test.
     */
    @BeforeEach
    void setUp() {
        validator = new ValidRolesValidator();
        context = Mockito.mock(ConstraintValidatorContext.class);
    }

    /**
     * @brief Tests validation of a valid set of roles.
     *
     * Ensures that the validator correctly recognizes valid roles.
     */
    @Test
    void testValidRoles() {
        Set<ERole> validRoles = Set.of(ERole.ROLE_USER, ERole.ROLE_ADMIN);
        assertTrue(validator.isValid(validRoles, context));
    }

    /**
     * @brief Tests validation of an invalid role.
     *
     * Ensures that the validator correctly rejects invalid roles.
     */
    @Test
    void testInvalidRole() {
        Set<ERole> invalidRoles = new HashSet<>();
        invalidRoles.add(ERole.ROLE_USER);
        invalidRoles.add(null);
        assertFalse(validator.isValid(invalidRoles, context));
    }

    /**
     * @brief Tests validation with an empty role set.
     *
     * Ensures that an empty set is considered valid.
     */
    @Test
    void testEmptyRoles() {
        Set<ERole> emptyRoles = Set.of();
        assertTrue(validator.isValid(emptyRoles, context));
    }
}

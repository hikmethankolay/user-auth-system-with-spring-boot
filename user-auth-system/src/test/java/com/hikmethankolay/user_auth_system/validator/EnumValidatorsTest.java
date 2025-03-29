/**
 * @file EnumValidatorsTest.java
 * @brief Tests for the enum validator classes.
 *
 * Contains unit tests for the enum validators, including string validation,
 * enum validation, and collection validation.
 *
 * @author Test Suite Generator
 * @date 2025-03-29
 */
package com.hikmethankolay.user_auth_system.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @class EnumValidatorsTest
 * @brief Test class for enum validators.
 *
 * This class contains unit tests for all enum validator implementations.
 */
@SpringBootTest
public class EnumValidatorsTest {

    /**
     * Enum for testing validators.
     */
    private enum TestEnum {
        OPTION_ONE,
        OPTION_TWO,
        OPTION_THREE
    }

    /**
     * Mock ValidEnum annotation for testing.
     */
    @MockitoBean
    private ValidEnum validEnum;

    /**
     * Mock validator context for testing.
     */
    private ConstraintValidatorContext context;

    /**
     * String validator to be tested.
     */
    private EnumValidatorForString stringValidator;

    /**
     * Enum validator to be tested.
     */
    private EnumValidatorForEnum enumValidator;

    /**
     * Collection validator to be tested.
     */
    private EnumValidatorForCollection collectionValidator;

    /**
     * @brief Setup method that runs before each test.
     *
     * Initializes validators and common mocks.
     */
    @BeforeEach
    public void setup() {
        context = mock(ConstraintValidatorContext.class);
        when(validEnum.enumClass()).thenReturn((Class) TestEnum.class);
        when(validEnum.ignoreCase()).thenReturn(false);

        stringValidator = new EnumValidatorForString();
        stringValidator.initialize(validEnum);

        enumValidator = new EnumValidatorForEnum();
        enumValidator.initialize(validEnum);

        collectionValidator = new EnumValidatorForCollection();
        collectionValidator.initialize(validEnum);
    }

    /**
     * @brief Test string validator with valid enum value.
     *
     * Verifies that EnumValidatorForString correctly validates valid enum values.
     */
    @Test
    public void testStringValidatorValidValue() {
        assertTrue(stringValidator.isValid("OPTION_ONE", context));
        assertTrue(stringValidator.isValid("OPTION_TWO", context));
        assertTrue(stringValidator.isValid("OPTION_THREE", context));
    }

    /**
     * @brief Test string validator with invalid enum value.
     *
     * Verifies that EnumValidatorForString correctly rejects invalid enum values.
     */
    @Test
    public void testStringValidatorInvalidValue() {
        assertFalse(stringValidator.isValid("INVALID_OPTION", context));
        assertFalse(stringValidator.isValid("option_one", context)); // case matters
    }

    /**
     * @brief Test string validator with null value.
     *
     * Verifies that EnumValidatorForString correctly handles null values.
     */
    @Test
    public void testStringValidatorNullValue() {
        assertTrue(stringValidator.isValid(null, context)); // null is considered valid
    }

    /**
     * @brief Test string validator with case-insensitive validation.
     *
     * Verifies that EnumValidatorForString correctly handles case-insensitive validation.
     */
    @Test
    public void testStringValidatorIgnoreCase() {
        // Configure validator to ignore case
        when(validEnum.ignoreCase()).thenReturn(true);

        // Reinitialize with new settings
        stringValidator = new EnumValidatorForString();
        stringValidator.initialize(validEnum);

        assertTrue(stringValidator.isValid("OPTION_ONE", context));
        assertTrue(stringValidator.isValid("option_one", context)); // case ignored
        assertTrue(stringValidator.isValid("OpTiOn_OnE", context)); // mixed case
        assertFalse(stringValidator.isValid("INVALID_OPTION", context)); // still invalid
    }

    /**
     * @brief Test enum validator with valid enum instance.
     *
     * Verifies that EnumValidatorForEnum correctly validates valid enum instances.
     */
    @Test
    public void testEnumValidatorValidEnum() {
        assertTrue(enumValidator.isValid(TestEnum.OPTION_ONE, context));
        assertTrue(enumValidator.isValid(TestEnum.OPTION_TWO, context));
        assertTrue(enumValidator.isValid(TestEnum.OPTION_THREE, context));
    }

    /**
     * @brief Test enum validator with null value.
     *
     * Verifies that EnumValidatorForEnum correctly handles null values.
     */
    @Test
    public void testEnumValidatorNullValue() {
        assertFalse(enumValidator.isValid(null, context)); // null is invalid for enums
    }

    /**
     * @brief Test enum validator with enum from different class.
     *
     * Verifies that EnumValidatorForEnum correctly handles enum instances from different classes.
     */
    @Test
    public void testEnumValidatorDifferentEnumClass() {
        // Different enum type for testing
        enum AnotherEnum { VALUE_ONE, VALUE_TWO }

        assertFalse(enumValidator.isValid(AnotherEnum.VALUE_ONE, context));
    }

    /**
     * @brief Test collection validator with valid values.
     *
     * Verifies that EnumValidatorForCollection correctly validates collections with valid values.
     */
    @Test
    public void testCollectionValidatorValidValues() {
        Collection<String> validCollection = Arrays.asList("OPTION_ONE", "OPTION_TWO");
        assertTrue(collectionValidator.isValid(validCollection, context));
    }

    /**
     * @brief Test collection validator with some invalid values.
     *
     * Verifies that EnumValidatorForCollection correctly rejects collections with any invalid values.
     */
    @Test
    public void testCollectionValidatorSomeInvalidValues() {
        Collection<String> mixedCollection = Arrays.asList("OPTION_ONE", "INVALID_OPTION");
        assertFalse(collectionValidator.isValid(mixedCollection, context));
    }

    /**
     * @brief Test collection validator with null collection.
     *
     * Verifies that EnumValidatorForCollection correctly handles null collections.
     */
    @Test
    public void testCollectionValidatorNullCollection() {
        assertTrue(collectionValidator.isValid(null, context)); // null collection is valid
    }

    /**
     * @brief Test collection validator with empty collection.
     *
     * Verifies that EnumValidatorForCollection correctly handles empty collections.
     */
    @Test
    public void testCollectionValidatorEmptyCollection() {
        Collection<String> emptyCollection = Collections.emptyList();
        assertTrue(collectionValidator.isValid(emptyCollection, context)); // empty collection is valid
    }

    /**
     * @brief Test collection validator with null element.
     *
     * Verifies that EnumValidatorForCollection correctly handles collections with null elements.
     */
    @Test
    public void testCollectionValidatorNullElement() {
        Collection<String> collectionWithNull = Arrays.asList("OPTION_ONE", null);
        assertFalse(collectionValidator.isValid(collectionWithNull, context)); // null element is invalid
    }

    /**
     * @brief Test collection validator with case-insensitive validation.
     *
     * Verifies that EnumValidatorForCollection correctly handles case-insensitive validation.
     */
    @Test
    public void testCollectionValidatorIgnoreCase() {
        // Configure validator to ignore case
        when(validEnum.ignoreCase()).thenReturn(true);

        // Reinitialize with new settings
        collectionValidator = new EnumValidatorForCollection();
        collectionValidator.initialize(validEnum);

        Collection<String> mixedCaseCollection = Arrays.asList("option_one", "OPTION_TWO", "OpTiOn_ThReE");
        assertTrue(collectionValidator.isValid(mixedCaseCollection, context)); // case ignored

        Collection<String> invalidCollection = Arrays.asList("option_one", "INVALID_OPTION");
        assertFalse(collectionValidator.isValid(invalidCollection, context)); // still invalid content
    }
}
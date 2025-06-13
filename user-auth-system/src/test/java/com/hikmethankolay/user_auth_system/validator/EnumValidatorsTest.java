/**
 * @file EnumValidatorTest.java
 * @brief Unit tests for custom enum validators with comprehensive Doxygen documentation.
 * @details This file contains refactored unit tests for the following validators:
 *          - EnumValidatorForCollection
 *          - EnumValidatorForEnum
 *          - EnumValidatorForString
 *          The tests are grouped using JUnit 5's @Nested classes.
 */

/**
 * @package com.hikmethankolay.user_auth_system.validator
 * @brief This package contains test classes for the backend application, including validator tests.
 */
package com.hikmethankolay.user_auth_system.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * @class EnumValidatorTest
 * @brief Unit tests for the custom enum validators.
 * @details This test class validates the functionality of the custom enum validators
 *          using a sample enum and a custom ValidEnum annotation instance.
 */
class EnumValidatorTest {

    /**
     * @brief Validator for validating collections of enum values.
     */
    private EnumValidatorForCollection collectionValidator;

    /**
     * @brief Validator for validating single enum values.
     */
    private EnumValidatorForEnum enumValidator;

    /**
     * @brief Validator for validating string representations of enum values.
     */
    private EnumValidatorForString stringValidator;

    /**
     * @brief Shared mock context used for constraint validations.
     */
    private ConstraintValidatorContext context;

    /**
     * @enum TestEnum
     * @brief Sample enum for testing purposes.
     * @details This enum contains sample values used to test the validation logic.
     */
    private enum TestEnum {
        VALUE_ONE,  /**< First test value. */
        VALUE_TWO,  /**< Second test value. */
        VALUE_THREE /**< Third test value. */
    }

    /**
     * @brief Helper method to create a custom ValidEnum annotation instance.
     * @return A custom instance of the ValidEnum annotation configured for TestEnum.
     */
    private ValidEnum createValidEnumAnnotation() {
        return new ValidEnum() {
            @Override
            public Class<? extends Enum<?>> enumClass() {
                return TestEnum.class;
            }

            @Override
            public boolean ignoreCase() {
                return true;
            }

            @Override
            public String message() {
                return "Invalid enum value";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return ValidEnum.class;
            }
        };
    }

    /**
     * @brief Setup method executed before each test.
     * @details Initializes all validators with a custom ValidEnum annotation and creates a shared mock context.
     */
    @BeforeEach
    void setUp() {
        ValidEnum annotation = createValidEnumAnnotation();

        collectionValidator = new EnumValidatorForCollection();
        collectionValidator.initialize(annotation);

        enumValidator = new EnumValidatorForEnum();
        enumValidator.initialize(annotation);

        stringValidator = new EnumValidatorForString();
        stringValidator.initialize(annotation);

        context = mock(ConstraintValidatorContext.class);
    }

    /**
     * @class CollectionValidatorTests
     * @brief Group of tests for the EnumValidatorForCollection.
     */
    @Nested
    class CollectionValidatorTests {

        /**
         * @brief Test that a collection of valid enum values passes validation.
         */
        @Test
        void validValuesShouldPass() {
            List<String> validValues = Arrays.asList("VALUE_ONE", "VALUE_TWO");
            assertTrue(collectionValidator.isValid(validValues, context));
        }

        /**
         * @brief Test that a collection of valid enum values with different cases passes validation.
         */
        @Test
        void validValuesIgnoreCaseShouldPass() {
            List<String> validValuesLowerCase = Arrays.asList("value_one", "value_two");
            assertTrue(collectionValidator.isValid(validValuesLowerCase, context));
        }

        /**
         * @brief Test that a collection containing an invalid enum value fails validation.
         */
        @Test
        void invalidValuesShouldFail() {
            List<String> invalidValues = Arrays.asList("VALUE_ONE", "INVALID_VALUE");
            assertFalse(collectionValidator.isValid(invalidValues, context));
        }

        /**
         * @brief Test that a null collection is considered valid.
         */
        @Test
        void nullCollectionShouldBeValid() {
            assertTrue(collectionValidator.isValid(null, context));
        }

        /**
         * @brief Test that a collection containing a null element fails validation.
         */
        @Test
        void collectionWithNullElementShouldFail() {
            List<String> valuesWithNull = Arrays.asList("VALUE_ONE", null);
            assertFalse(collectionValidator.isValid(valuesWithNull, context));
        }
    }

    /**
     * @class EnumValidatorTests
     * @brief Group of tests for the EnumValidatorForEnum.
     */
    @Nested
    class EnumValidatorTests {

        /**
         * @brief Test that a valid enum value passes validation.
         */
        @Test
        void validEnumValueShouldPass() {
            assertTrue(enumValidator.isValid(TestEnum.VALUE_ONE, context));
        }

        /**
         * @brief Test that a null enum value fails validation.
         */
        @Test
        void nullEnumValueShouldFail() {
            assertFalse(enumValidator.isValid(null, context));
        }
    }

    /**
     * @class StringValidatorTests
     * @brief Group of tests for the EnumValidatorForString.
     */
    @Nested
    class StringValidatorTests {

        /**
         * @brief Test that a valid string representation of an enum value passes validation.
         */
        @Test
        void validStringValueShouldPass() {
            assertTrue(stringValidator.isValid("VALUE_ONE", context));
        }

        /**
         * @brief Test that a valid string representation with different case passes validation.
         */
        @Test
        void validStringValueIgnoreCaseShouldPass() {
            assertTrue(stringValidator.isValid("value_one", context));
        }

        /**
         * @brief Test that an invalid string representation fails validation.
         */
        @Test
        void invalidStringValueShouldFail() {
            assertFalse(stringValidator.isValid("INVALID_VALUE", context));
        }

        /**
         * @brief Test that a null string value is considered valid.
         */
        @Test
        void nullStringValueShouldBeValid() {
            assertTrue(stringValidator.isValid(null, context));
        }
    }
}

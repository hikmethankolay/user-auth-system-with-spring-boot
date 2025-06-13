/**
 * @file EnumValidatorForString.java
 * @brief Validator for validating string values against enum values.
 * @details This class provides validation logic for strings to ensure they match a specified enum type.
 * @version 1.0
 * @date 2025
 */

package com.hikmethankolay.user_auth_system.validator; /**< @package com.hikmethankolay.user_auth_system.validator
                                                 *   @brief Package for custom validators in the application.
                                                 *   @details This package contains custom validation logic for various use cases.
                                                 */

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @class EnumValidatorForString
 * @brief Validator for validating string values against enum values.
 * @details Validates that a string matches a value in a specified enum type.
 */
public class EnumValidatorForString implements ConstraintValidator<ValidEnum, String> {

    private List<String> acceptedValues;
    private boolean ignoreCase;

    /**
     * @brief Initializes the validator with the specified annotation parameters.
     * @param annotation The ValidEnum annotation instance.
     */
    @Override
    public void initialize(ValidEnum annotation) {
        ignoreCase = annotation.ignoreCase();
        acceptedValues = Arrays.stream(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toList());
        if (ignoreCase) {
            acceptedValues = acceptedValues.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
        }
    }

    /**
     * @brief Validates a string against the accepted enum values.
     * @param value The string to validate.
     * @param context The validation context.
     * @return True if the string is a valid enum value, otherwise false.
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        String valueToCheck = ignoreCase ? value.toLowerCase() : value;
        return acceptedValues.contains(valueToCheck);
    }
}

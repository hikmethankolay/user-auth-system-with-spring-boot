/**
 * @file EnumValidatorForCollection.java
 * @brief Validator for validating collections of enum values.
 * @details This class provides validation logic for collections of strings to ensure they match enum values.
 * @version 1.0
 * @date 2025
 */

package com.hikmethankolay.user_auth_system.validator; /**< @package com.tubitak_1001.backend_app.valiadator
                                                 *   @brief Package for custom validators in the application.
                                                 *   @details This package contains custom validation logic for various use cases.
                                                 */

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @class EnumValidatorForCollection
 * @brief Validator for validating collections of enum values.
 * @details Validates that each string in a collection matches a value in a specified enum.
 */
public class EnumValidatorForCollection implements ConstraintValidator<ValidEnum, Collection<String>> {

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
     * @brief Validates a collection of strings against the accepted enum values.
     * @param values The collection of strings to validate.
     * @param context The validation context.
     * @return True if all strings are valid enum values, otherwise false.
     */
    @Override
    public boolean isValid(Collection<String> values, ConstraintValidatorContext context) {
        if (values == null) {
            return true;
        }
        for (String value : values) {
            if (value == null) {
                return false;
            }
            String valueToCheck = ignoreCase ? value.toLowerCase() : value;
            if (!acceptedValues.contains(valueToCheck)) {
                return false;
            }
        }
        return true;
    }
}

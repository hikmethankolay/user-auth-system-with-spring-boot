/**
 * @file EnumValidatorForEnum.java
 * @brief Validator for validating enum values.
 * @details This class provides validation logic for enum values to ensure they match a specified enum type.
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

/**
 * @class EnumValidatorForEnum
 * @brief Validator for validating enum values.
 * @details Validates that an enum value matches a value in a specified enum type.
 */
public class EnumValidatorForEnum implements ConstraintValidator<ValidEnum, Enum<?>> {

    private Class<? extends Enum<?>> enumClass;

    /**
     * @brief Initializes the validator with the specified annotation parameters.
     * @param annotation The ValidEnum annotation instance.
     */
    @Override
    public void initialize(ValidEnum annotation) {
        this.enumClass = annotation.enumClass();
    }

    /**
     * @brief Validates an enum value against the accepted enum type.
     * @param value The enum value to validate.
     * @param context The validation context.
     * @return True if the value is a valid enum value, otherwise false.
     */
    @Override
    public boolean isValid(Enum<?> value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        return Arrays.stream(enumClass.getEnumConstants())
                .anyMatch(validEnum -> validEnum.name().equals(value.name()));
    }
}

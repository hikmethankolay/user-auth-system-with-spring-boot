/**
 * @file ValidEnum.java
 * @brief Annotation for validating enum values.
 * @details This annotation is used to validate that a field or parameter matches a specified enum type.
 * @version 1.0
 * @date 2025
 */

package com.hikmethankolay.user_auth_system.validator; /**< @package com.tubitak_1001.backend_app.valiadator
                                                 *   @brief Package for custom validators in the application.
                                                 *   @details This package contains custom validation logic for various use cases.
                                                 */

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * @annotation ValidEnum
 * @brief Annotation for validating enum values.
 * @details Validates that a field or parameter matches a specified enum type.
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { EnumValidatorForString.class, EnumValidatorForCollection.class, EnumValidatorForEnum.class })
@Documented
public @interface ValidEnum {

    /**
     * @brief Error message to be returned if validation fails.
     * @return The error message.
     */
    String message() default "Alan {enumClass} değerlerinden biri olmalı.";

    /**
     * @brief Groups for validation.
     * @return The groups.
     */
    Class<?>[] groups() default {};

    /**
     * @brief Payload for validation.
     * @return The payload.
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * @brief The enum class to validate against.
     * @return The enum class.
     */
    Class<? extends Enum<?>> enumClass();

    /**
     * @brief Whether to ignore case during validation.
     * @return True if case should be ignored, otherwise false.
     */
    boolean ignoreCase() default false;
}


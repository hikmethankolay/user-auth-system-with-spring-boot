/**
 * @file ValidRole.java
 * @brief Annotation for validating role fields.
 *
 * This annotation ensures that only allowed roles (ROLE_USER, ROLE_ADMIN) are accepted.
 * The validation logic is implemented in {@link ValidRolesValidator}.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.validator
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.validator;

import com.hikmethankolay.user_auth_system.enums.ERole;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to validate user roles.
 *
 * Ensures that assigned roles are within the allowed set of {@link ERole}.
 *
 * @see ValidRolesValidator
 */
@Constraint(validatedBy = ValidRolesValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRole {

    /**
     * The error message to be returned when validation fails.
     */
    String message() default "Invalid role(s) provided. Allowed values: ROLE_USER, ROLE_ADMIN";

    /**
     * Allows the specification of validation groups.
     */
    Class<?>[] groups() default {};

    /**
     * Can be used by clients to assign custom payload objects to a constraint.
     */
    Class<? extends Payload>[] payload() default {};

}

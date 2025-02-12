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

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to validate that a role is permitted.
 *
 * <p>Used on fields or parameters to ensure they contain only allowed roles.
 * The allowed roles are: ROLE_USER, ROLE_ADMIN.</p>
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

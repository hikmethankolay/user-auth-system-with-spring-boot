/**
 * @file ValidRolesValidator.java
 * @brief Validator for the @ValidRole annotation.
 *
 * Ensures that all roles in a given set are valid according to the ERole enum.
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
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validator for the {@link ValidRole} annotation.
 * Validates that every role in the set is one of the allowed roles defined in {@link ERole}.
 */
public class ValidRolesValidator implements ConstraintValidator<ValidRole, Set<ERole>> {

    /**
     * The set of valid role names fetched from {@link ERole}.
     */
    private static final Set<String> VALID_ROLE_NAMES =
            Arrays.stream(ERole.values())
                    .map(Enum::name)
                    .collect(Collectors.toSet());

    /**
     * Checks if all roles in the provided set are valid.
     *
     * @param roles   the set of roles to validate
     * @param context the context in which the constraint is evaluated
     * @return {@code true} if the roles are valid or the set is null/empty, {@code false} otherwise
     */
    @Override
    public boolean isValid(Set<ERole> roles, ConstraintValidatorContext context) {
        if (roles == null || roles.isEmpty()) {
            return true;
        }

        boolean allValid = roles.stream().allMatch(role -> VALID_ROLE_NAMES.contains(role.name()));

        if (!allValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid role(s) provided. Allowed values: " + VALID_ROLE_NAMES)
                    .addConstraintViolation();
        }

        return allValid;
    }
}

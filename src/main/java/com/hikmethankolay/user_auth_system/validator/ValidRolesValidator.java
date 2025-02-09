package com.hikmethankolay.user_auth_system.validator;

import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.enums.ERole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidRolesValidator implements ConstraintValidator<ValidRole, Set<ERole>>
{

    // Dynamically fetch all valid role names from the ERole enum
    private static final Set<String> VALID_ROLE_NAMES =
            Arrays.stream(ERole.values())
                    .map(Enum::name)
                    .collect(Collectors.toSet());

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

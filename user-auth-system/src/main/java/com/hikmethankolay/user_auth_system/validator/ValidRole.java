package com.hikmethankolay.user_auth_system.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidRolesValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRole {

    String message() default "Invalid role(s) provided. Allowed values: ROLE_USER, ROLE_ADMIN";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}

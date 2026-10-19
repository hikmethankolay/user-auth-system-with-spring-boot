package com.hikmethankolay.template.user.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Username rule: 3-32 characters of letters, digits, '.', '_' or '-'.
 *
 * <p>{@code null} is valid; combine with {@code @NotBlank} when the field is required.
 * '@' is not allowed, so a login identifier containing '@' is always an email.</p>
 */
@Documented
@Constraint(validatedBy = {})
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Size(min = 3, max = 32)
@Pattern(regexp = "^[A-Za-z0-9._-]+$")
@ReportAsSingleViolation
public @interface ValidUsername {

    /**
     * Error message reported to the client.
     *
     * @return the message
     */
    String message() default "must be 3-32 characters and contain only letters, digits, '.', '_' or '-'";

    /**
     * Validation groups.
     *
     * @return the groups
     */
    Class<?>[] groups() default {};

    /**
     * Validation payload.
     *
     * @return the payload types
     */
    Class<? extends Payload>[] payload() default {};
}

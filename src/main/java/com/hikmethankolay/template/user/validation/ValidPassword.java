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
 * Password strength rule: 8-72 characters with at least one uppercase letter, one lowercase
 * letter, one digit and one symbol, and at most 72 bytes in UTF-8 (BCrypt's input limit; longer
 * input makes hashing fail), checked by {@link ValidPasswordValidator}.
 *
 * <p>{@code null} is valid; combine with {@code @NotBlank} when the field is required.</p>
 */
@Documented
@Constraint(validatedBy = ValidPasswordValidator.class)
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Size(min = 8, max = 72)
@Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[^0-9A-Za-z]).*$")
@ReportAsSingleViolation
public @interface ValidPassword {

    /**
     * Error message reported to the client.
     *
     * @return the message
     */
    String message() default
            "must be 8-72 characters (at most 72 bytes in UTF-8) and include an uppercase letter, a lowercase letter, a digit and a symbol";

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

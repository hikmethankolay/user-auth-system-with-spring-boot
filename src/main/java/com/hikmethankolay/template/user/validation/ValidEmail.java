package com.hikmethankolay.template.user.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Email rule shared by every request that carries an address: a well-formed address of 3-254 characters
 * (254 is the longest address SMTP allows).
 *
 * <p>{@code null} is valid; combine with {@code @NotBlank} when the field is required. Each part reports
 * its own message, e.g. "must be a well-formed email address".</p>
 */
@Documented
@Constraint(validatedBy = {})
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Email
@Size(min = 3, max = 254)
public @interface ValidEmail {

    /**
     * Error message; unused because each composing constraint reports its own message.
     *
     * @return the message
     */
    String message() default "must be a valid email address";

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

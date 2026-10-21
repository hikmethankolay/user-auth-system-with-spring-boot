package com.hikmethankolay.template.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.charset.StandardCharsets;

/**
 * The part of {@link ValidPassword} that annotations cannot express: BCrypt hashes at most 72 bytes,
 * and a 72-character password with multi-byte characters is longer than that.
 */
public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    /** BCrypt's input limit in bytes. */
    static final int MAX_BYTES = 72;

    /** Creates the validator; Bean Validation instantiates it. */
    public ValidPasswordValidator() {
        // Nothing to initialise.
    }

    /**
     * Checks the UTF-8 length of the password.
     *
     * @param value   the password, may be {@code null}
     * @param context validation context
     * @return {@code true} if the password is {@code null} or at most {@link #MAX_BYTES} bytes
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.getBytes(StandardCharsets.UTF_8).length <= MAX_BYTES;
    }
}

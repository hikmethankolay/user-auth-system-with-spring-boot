package com.hikmethankolay.template.account;

import com.hikmethankolay.template.account.dto.EmailRequest;
import com.hikmethankolay.template.account.dto.PasswordResetRequest;
import com.hikmethankolay.template.account.dto.TokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public account endpoints under {@code /api/v1/auth}.
 *
 * <table>
 *   <caption>Endpoints</caption>
 *   <tr><th>Method</th><th>Path</th><th>Success</th></tr>
 *   <tr><td>POST</td><td>/verify-email</td><td>204</td></tr>
 *   <tr><td>POST</td><td>/verify-email/resend</td><td>202 (always)</td></tr>
 *   <tr><td>POST</td><td>/password-reset</td><td>202 (always)</td></tr>
 *   <tr><td>POST</td><td>/password-reset/confirm</td><td>204</td></tr>
 * </table>
 *
 * <p>The two 202 endpoints answer the same way whether or not the address has an account.</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Account")
public class AccountController {

    /** Account business logic. */
    private final AccountService accountService;

    /**
     * Creates the controller.
     *
     * @param accountService account business logic
     */
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Confirms an email address with the token from the verification email.
     *
     * @param request the token
     */
    @PostMapping("/verify-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Confirm an email address")
    public void verifyEmail(@Valid @RequestBody TokenRequest request) {
        accountService.verifyEmail(request.token());
    }

    /**
     * Sends a new verification email.
     *
     * @param request the address to verify
     */
    @PostMapping("/verify-email/resend")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Resend the verification email")
    public void resendVerification(@Valid @RequestBody EmailRequest request) {
        accountService.resendVerification(request.email());
    }

    /**
     * Sends a password reset email.
     *
     * @param request the account's address
     */
    @PostMapping("/password-reset")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Request a password reset email")
    public void requestPasswordReset(@Valid @RequestBody EmailRequest request) {
        accountService.requestPasswordReset(request.email());
    }

    /**
     * Sets a new password with the token from the reset email.
     *
     * @param request the token and the new password
     */
    @PostMapping("/password-reset/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Set a new password with a reset token")
    public void resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        accountService.resetPassword(request.token(), request.newPassword());
    }
}

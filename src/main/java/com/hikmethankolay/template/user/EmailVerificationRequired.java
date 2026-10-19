package com.hikmethankolay.template.user;

/**
 * Published when a user's email address needs (re)verification: after registration and after
 * an email change. Handled after the transaction commits, so no email is sent for a rolled-back change.
 *
 * @param userId id of the user whose address must be verified
 */
public record EmailVerificationRequired(long userId) {
}

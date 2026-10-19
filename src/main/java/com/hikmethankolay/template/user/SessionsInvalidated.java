package com.hikmethankolay.template.user;

/**
 * Published when every session of a user must end: after a password change or reset, and after
 * an administrator changes the user's password or role or deletes the user. The {@code auth}
 * package revokes the user's refresh tokens in the same transaction and the access tokens once it
 * commits.
 *
 * @param userId id of the user whose sessions end
 */
public record SessionsInvalidated(long userId) {
}

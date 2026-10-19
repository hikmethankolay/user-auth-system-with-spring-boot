package com.hikmethankolay.template.user;

/**
 * Contributes a feature's data about a user to the personal data export.
 *
 * <p>Implement it as a Spring bean in every package that stores data linked to a user (orders,
 * uploads, settings...), so {@code GET /api/v1/users/me/export} stays complete as the application
 * grows. Data must also disappear when the user is deleted: reference {@code users(id)} with
 * {@code ON DELETE CASCADE}, or listen to {@link SessionsInvalidated}.</p>
 */
public interface PersonalDataProvider {

    /**
     * Returns the name of this feature's section in the export.
     *
     * @return a short, stable key such as {@code sessions}
     */
    String section();

    /**
     * Collects this feature's data about a user.
     *
     * @param userId the user requesting the export
     * @return a JSON-serializable value, typically a list of records
     */
    Object export(long userId);
}

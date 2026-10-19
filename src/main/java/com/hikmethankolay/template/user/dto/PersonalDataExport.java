package com.hikmethankolay.template.user.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Everything the application stores about a user, returned by {@code GET /api/v1/users/me/export}
 * (right of access and data portability, GDPR articles 15 and 20).
 *
 * @param exportedAt when the export was created
 * @param profile    the user's account data
 * @param related    data kept by other features, one entry per
 *                   {@link com.hikmethankolay.template.user.PersonalDataProvider}, e.g. {@code sessions}
 */
public record PersonalDataExport(Instant exportedAt, UserResponse profile, Map<String, Object> related) {
}

package com.hikmethankolay.template.social;

/**
 * What a provider says about the user who logged in there.
 *
 * @param provider      registration id, e.g. {@code github}
 * @param subject       the provider's stable user id
 * @param email         the user's email address at the provider, may be {@code null}
 * @param emailVerified whether the provider verified that address
 * @param username      a name to derive a username from, e.g. the GitHub login, may be {@code null}
 */
public record SocialProfile(String provider, String subject, String email, boolean emailVerified, String username) {}

package com.hikmethankolay.template.social;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link SocialIdentity}. */
public interface SocialIdentityRepository extends JpaRepository<SocialIdentity, Long> {

    /**
     * Finds the link of a provider account.
     *
     * @param provider registration id
     * @param subject  the provider's user id
     * @return the link, or empty if the account is not linked to anyone
     */
    Optional<SocialIdentity> findByProviderAndSubject(String provider, String subject);

    /**
     * Lists the provider accounts linked to a user.
     *
     * @param userId the user
     * @return the links, oldest first
     */
    List<SocialIdentity> findAllByUserIdOrderByIdAsc(long userId);
}

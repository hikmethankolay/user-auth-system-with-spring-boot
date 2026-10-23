package com.hikmethankolay.template.social;

import com.hikmethankolay.template.user.PersonalDataProvider;
import org.springframework.stereotype.Component;

/** Adds the user's linked provider accounts to the personal data export. */
@Component
public class SocialAccountDataProvider implements PersonalDataProvider {

    private final SocialAccountService socialAccounts;

    /**
     * Creates the provider.
     *
     * @param socialAccounts lists the linked accounts
     */
    public SocialAccountDataProvider(SocialAccountService socialAccounts) {
        this.socialAccounts = socialAccounts;
    }

    /**
     * Returns the section name.
     *
     * @return {@code socialAccounts}
     */
    @Override
    public String section() {
        return "socialAccounts";
    }

    /**
     * Lists the user's linked provider accounts.
     *
     * @param userId the user
     * @return one {@link SocialAccountService.LinkedAccount} per provider account
     */
    @Override
    public Object export(long userId) {
        return socialAccounts.linkedAccounts(userId);
    }
}

package com.hikmethankolay.template.social;

import com.hikmethankolay.template.config.AppProperties;
import com.hikmethankolay.template.config.AppProperties.SocialLogin.Client;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The configured login providers, built from {@code app.social-login} with Spring Security's GitHub and Google
 * defaults. A provider without a client id and secret is left out.
 */
@Component
public class SocialLoginProviders {

    private final Map<String, ClientRegistration> registrations = new LinkedHashMap<>();

    /**
     * Creates the registrations.
     *
     * @param properties application properties holding the client credentials and the redirect URI
     */
    public SocialLoginProviders(AppProperties properties) {
        AppProperties.SocialLogin social = properties.socialLogin();
        add(
                social.github(),
                CommonOAuth2Provider.GITHUB
                        .getBuilder("github")
                        // user:email lets the API read private and unverified addresses, see SocialProfileClient.
                        .scope("read:user", "user:email"),
                social.redirectUri());
        add(social.google(), CommonOAuth2Provider.GOOGLE.getBuilder("google"), social.redirectUri());
    }

    private void add(Client client, ClientRegistration.Builder builder, String redirectUri) {
        if (client.isConfigured()) {
            ClientRegistration registration = builder.clientId(client.clientId())
                    .clientSecret(client.clientSecret())
                    .redirectUri(redirectUri)
                    .build();
            registrations.put(registration.getRegistrationId(), registration);
        }
    }

    /**
     * Finds a configured provider.
     *
     * @param provider registration id, {@code github} or {@code google}
     * @return the registration, or empty if the provider is unknown or not configured
     */
    public Optional<ClientRegistration> find(String provider) {
        return Optional.ofNullable(registrations.get(provider));
    }

    /**
     * Returns the authorization URL of every configured provider, without {@code state} and the PKCE challenge,
     * which the frontend adds.
     *
     * @return URLs by provider, in a stable order
     */
    public Map<String, String> authorizationUris() {
        Map<String, String> uris = new LinkedHashMap<>();
        registrations.forEach((id, registration) -> uris.put(
                id,
                UriComponentsBuilder.fromUriString(
                                registration.getProviderDetails().getAuthorizationUri())
                        .queryParam("response_type", "code")
                        .queryParam("client_id", registration.getClientId())
                        .queryParam("redirect_uri", registration.getRedirectUri())
                        .queryParam("scope", String.join(" ", registration.getScopes()))
                        .encode()
                        .build()
                        .toUriString()));
        return uris;
    }
}

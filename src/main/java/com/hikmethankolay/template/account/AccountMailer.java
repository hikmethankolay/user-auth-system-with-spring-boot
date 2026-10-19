package com.hikmethankolay.template.account;

import com.hikmethankolay.template.config.AppProperties;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Writes and sends the account emails. Links point to the frontend ({@code app.mail.link-base-url}),
 * which posts the token to the API.
 *
 * <p>Sending failures are logged, not thrown: the user can request a new email, and a mail outage
 * must not break registration.</p>
 */
@Component
public class AccountMailer {

    /** Logger for sending failures. */
    private static final Logger log = LoggerFactory.getLogger(AccountMailer.class);

    /** Sends the messages (SMTP via {@code spring.mail.*}). */
    private final MailSender mailSender;

    /** Sender address and link base URL. */
    private final AppProperties.Mail properties;

    /**
     * Creates the mailer.
     *
     * @param mailSender sends the messages
     * @param properties application properties holding the mail settings
     */
    public AccountMailer(MailSender mailSender, AppProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties.mail();
    }

    /**
     * Sends the email verification link.
     *
     * @param to    recipient address
     * @param token the raw verification token
     * @param ttl   how long the link is valid
     */
    public void sendEmailVerification(String to, String token, Duration ttl) {
        send(to, "Confirm your email address",
                "Confirm your email address by opening this link:%n%n%s%n%nThe link expires in %d hours."
                        .formatted(link("/verify-email", token), ttl.toHours()));
    }

    /**
     * Sends the password reset link.
     *
     * @param to    recipient address
     * @param token the raw reset token
     * @param ttl   how long the link is valid
     */
    public void sendPasswordReset(String to, String token, Duration ttl) {
        send(to, "Reset your password",
                ("Someone asked to reset the password of your account. If that was you, open this link:%n%n%s%n%n"
                        + "The link expires in %d minutes. If you did not ask for this, ignore this email.")
                        .formatted(link("/reset-password", token), ttl.toMinutes()));
    }

    /**
     * Builds a frontend link carrying the token.
     *
     * @param path  frontend path
     * @param token the raw token
     * @return the absolute link
     */
    private String link(String path, String token) {
        return UriComponentsBuilder.fromUriString(properties.linkBaseUrl())
                .path(path)
                .queryParam("token", token)
                .toUriString();
    }

    /**
     * Sends a plain-text message, logging failures.
     *
     * @param to      recipient address
     * @param subject subject line
     * @param text    message body
     */
    private void send(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.from());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Could not send '{}' email: {}", subject, ex.getMessage());
        }
    }
}

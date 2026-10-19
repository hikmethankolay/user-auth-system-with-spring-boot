package com.hikmethankolay.template.account;

import com.hikmethankolay.template.common.ApiException;
import com.hikmethankolay.template.config.AppProperties;
import com.hikmethankolay.template.user.EmailVerificationRequired;
import com.hikmethankolay.template.user.User;
import com.hikmethankolay.template.user.UserRepository;
import com.hikmethankolay.template.user.UserService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Email verification and password reset.
 *
 * <p>Tokens are 256-bit random values sent by email; only their SHA-256 hash is stored. Each token
 * is single-use, expires, and issuing a new one invalidates the previous one of the same type.
 * Requests that take an email address behave the same whether or not the address exists, so they
 * cannot be used to find out which addresses have accounts.</p>
 */
@Service
public class AccountService {

    /** Logger for token cleanup. */
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    /** Error for unknown, used or expired tokens; deliberately the same for all three. */
    private static final String INVALID_TOKEN = "The link is invalid or has expired.";

    /** Source of token randomness. */
    private final SecureRandom random = new SecureRandom();

    /** Token persistence. */
    private final AccountTokenRepository tokens;

    /** User lookups by id and email. */
    private final UserRepository users;

    /** User changes, with cache eviction. */
    private final UserService userService;

    /** Sends the emails. */
    private final AccountMailer mailer;

    /** Token lifetimes. */
    private final AppProperties.Account properties;

    /**
     * Creates the service.
     *
     * @param tokens      token persistence
     * @param users       user lookups
     * @param userService user changes
     * @param mailer      sends the emails
     * @param properties  application properties holding the token lifetimes
     */
    public AccountService(
            AccountTokenRepository tokens,
            UserRepository users,
            UserService userService,
            AccountMailer mailer,
            AppProperties properties) {
        this.tokens = tokens;
        this.users = users;
        this.userService = userService;
        this.mailer = mailer;
        this.properties = properties.account();
    }

    /**
     * Sends a verification email once the transaction that registered or changed the user has committed.
     *
     * @param event identifies the user to verify
     */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onEmailVerificationRequired(EmailVerificationRequired event) {
        users.findById(event.userId()).ifPresent(this::sendVerification);
    }

    /**
     * Sends a new verification email if the address belongs to an unverified account.
     *
     * @param email the address to verify
     */
    @Transactional
    public void resendVerification(String email) {
        users.findByEmail(normalize(email))
                .filter(user -> !user.isEmailVerified())
                .ifPresent(this::sendVerification);
    }

    /**
     * Confirms an email address.
     *
     * @param token the token from the verification email
     * @throws ApiException 400 if the token is unknown, already used or expired
     */
    @Transactional
    public void verifyEmail(String token) {
        userService.markEmailVerified(consume(token, AccountTokenType.EMAIL_VERIFICATION).getUserId());
    }

    /**
     * Sends a password reset email if the address belongs to an account.
     *
     * @param email the account's address
     */
    @Transactional
    public void requestPasswordReset(String email) {
        users.findByEmail(normalize(email)).ifPresent(user -> mailer.sendPasswordReset(
                user.getEmail(),
                issue(user.getId(), AccountTokenType.PASSWORD_RESET, properties.passwordResetTokenTtl()),
                properties.passwordResetTokenTtl()));
    }

    /**
     * Sets a new password using a reset token.
     *
     * @param token       the token from the reset email
     * @param newPassword the new password
     * @throws ApiException 400 if the token is unknown, already used or expired
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        userService.setPassword(consume(token, AccountTokenType.PASSWORD_RESET).getUserId(), newPassword);
    }

    /** Deletes expired tokens once an hour. Safe to run on several instances at once. */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void deleteExpiredTokens() {
        int deleted = tokens.deleteAllExpiredBefore(Instant.now());
        if (deleted > 0) {
            log.info("Deleted {} expired account tokens", deleted);
        }
    }

    /**
     * Issues a verification token and emails it.
     *
     * @param user the user to verify
     */
    private void sendVerification(User user) {
        Duration ttl = properties.verificationTokenTtl();
        mailer.sendEmailVerification(user.getEmail(), issue(user.getId(), AccountTokenType.EMAIL_VERIFICATION, ttl), ttl);
    }

    /**
     * Creates a token, replacing earlier ones of the same type.
     *
     * @param userId owner of the token
     * @param type   purpose of the token
     * @param ttl    lifetime of the token
     * @return the raw token to put in the email
     */
    private String issue(long userId, AccountTokenType type, Duration ttl) {
        tokens.deleteAllByUserIdAndType(userId, type);
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.save(new AccountToken(userId, type, hash(token), Instant.now().plus(ttl)));
        return token;
    }

    /**
     * Validates a token and deletes it (and any other token of the same type for that user).
     *
     * @param token the raw token
     * @param type  the required purpose
     * @return the consumed token
     * @throws ApiException 400 if the token is unknown, already used or expired
     */
    private AccountToken consume(String token, AccountTokenType type) {
        AccountToken stored = tokens.findByTokenHashAndType(hash(token), type)
                .filter(candidate -> !candidate.isExpired(Instant.now()))
                .orElseThrow(() -> ApiException.badRequest(INVALID_TOKEN));
        tokens.deleteAllByUserIdAndType(stored.getUserId(), type);
        return stored;
    }

    /**
     * Hashes a raw token for storage and lookup.
     *
     * @param token the raw token
     * @return the hex SHA-256 of the token
     */
    static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    /**
     * Normalises an email address the same way {@code UserService} stores it.
     *
     * @param email the address as entered
     * @return the trimmed, lower-case address
     */
    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

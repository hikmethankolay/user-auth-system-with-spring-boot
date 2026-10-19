package com.hikmethankolay.template.auth;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/** Spring Data repository for {@link RefreshToken}. */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Finds a token by its hash and locks the row, so two concurrent refreshes with the same
     * token are handled one after the other.
     *
     * @param tokenHash hex SHA-256 of the token
     * @return the token, if it exists
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Lists the unused, unexpired tokens of a user: one per active session.
     *
     * @param userId owner of the tokens
     * @param now    the current time
     * @return the active tokens, newest first
     */
    @Query("select t from RefreshToken t where t.userId = :userId and t.usedAt is null and t.expiresAt > :now "
            + "order by t.createdAt desc")
    List<RefreshToken> findActiveByUserId(long userId, Instant now);

    /**
     * Deletes every token of a session.
     *
     * @param familyId the session
     * @return the number of deleted tokens
     */
    @Modifying
    @Query("delete from RefreshToken t where t.familyId = :familyId")
    int deleteAllByFamilyId(UUID familyId);

    /**
     * Deletes every token of a user, ending all their sessions.
     *
     * @param userId owner of the tokens
     * @return the number of deleted tokens
     */
    @Modifying
    @Query("delete from RefreshToken t where t.userId = :userId")
    int deleteAllByUserId(long userId);

    /**
     * Deletes all tokens that expired before the given time.
     *
     * @param time cut-off time
     * @return the number of deleted tokens
     */
    @Modifying
    @Query("delete from RefreshToken t where t.expiresAt < :time")
    int deleteAllExpiredBefore(Instant time);
}

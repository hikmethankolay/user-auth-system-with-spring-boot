package com.hikmethankolay.template.mfa;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Data access for {@link RecoveryCode}. */
public interface RecoveryCodeRepository extends JpaRepository<RecoveryCode, Long> {

    /**
     * Marks an unused code as used, atomically: of two requests with the same code, only one succeeds.
     *
     * @param userId   the owner
     * @param codeHash SHA-256 of the normalized code
     * @param now      the time of use
     * @return 1 if the code existed and was unused, 0 otherwise
     */
    @Modifying
    @Query(
            value = "UPDATE recovery_codes SET used_at = :now "
                    + "WHERE user_id = :userId AND code_hash = :codeHash AND used_at IS NULL",
            nativeQuery = true)
    int markUsed(@Param("userId") long userId, @Param("codeHash") String codeHash, @Param("now") Instant now);

    /**
     * Deletes every code of a user.
     *
     * @param userId the owner
     */
    @Modifying
    @Query("delete from RecoveryCode c where c.userId = :userId")
    void deleteAllByUserId(@Param("userId") long userId);
}

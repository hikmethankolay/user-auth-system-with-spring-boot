package com.hikmethankolay.template.mail;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data repository for {@link OutboxEmail}.
 *
 * <p>The locking queries use {@code FOR UPDATE SKIP LOCKED}, so several instances can send at the
 * same time without sending any email twice.</p>
 */
public interface OutboxEmailRepository extends JpaRepository<OutboxEmail, Long> {

    /**
     * Locks the emails that are due, oldest first, skipping rows another instance is sending.
     *
     * @param now   the current time
     * @param limit maximum number of emails
     * @return the locked emails
     */
    @Query(value = "select * from email_outbox where next_attempt_at <= :now order by id limit :limit "
            + "for update skip locked", nativeQuery = true)
    List<OutboxEmail> lockDue(Instant now, int limit);

    /**
     * Locks one email, unless another instance is sending it.
     *
     * @param id the email id
     * @return the locked email, or empty if it is gone or locked
     */
    @Query(value = "select * from email_outbox where id = :id for update skip locked", nativeQuery = true)
    Optional<OutboxEmail> lockById(long id);
}

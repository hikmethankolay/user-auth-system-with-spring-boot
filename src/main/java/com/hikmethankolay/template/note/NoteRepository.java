package com.hikmethankolay.template.note;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Note}. Every query is scoped to an owner, so one user never sees another's notes. */
public interface NoteRepository extends JpaRepository<Note, Long> {

    /**
     * Finds a note of one user.
     *
     * @param id     note id
     * @param userId owner id
     * @return the note, or empty if it does not exist or belongs to someone else
     */
    Optional<Note> findByIdAndUserId(long id, long userId);

    /**
     * Lists the notes of one user.
     *
     * @param userId   owner id
     * @param pageable page, size and sort order
     * @return one page of notes
     */
    Page<Note> findAllByUserId(long userId, Pageable pageable);

    /**
     * Lists every note of one user, for the personal data export.
     *
     * @param userId owner id
     * @return the notes, oldest first
     */
    List<Note> findAllByUserIdOrderByIdAsc(long userId);
}

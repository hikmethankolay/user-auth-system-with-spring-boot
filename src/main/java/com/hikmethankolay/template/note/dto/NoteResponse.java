package com.hikmethankolay.template.note.dto;

import com.hikmethankolay.template.note.Note;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Public representation of a note.
 *
 * @param id        note id
 * @param title     title
 * @param body      text
 * @param createdAt creation time (ISO-8601, UTC)
 * @param updatedAt time of the last update (ISO-8601, UTC)
 */
@Schema(requiredProperties = {"id", "title", "body", "createdAt", "updatedAt"})
public record NoteResponse(long id, String title, String body, Instant createdAt, Instant updatedAt) {

    /**
     * Maps an entity to its public representation.
     *
     * @param note the entity
     * @return the response body
     */
    public static NoteResponse from(Note note) {
        return new NoteResponse(
                note.getId(), note.getTitle(), note.getBody(), note.getCreatedAt(), note.getUpdatedAt());
    }
}

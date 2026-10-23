package com.hikmethankolay.template.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code POST /api/v1/notes} and {@code PUT /api/v1/notes/{id}}.
 *
 * @param title title, 1 to 200 characters, not only whitespace
 * @param body  text, at most 10,000 characters, may be empty
 */
public record NoteRequest(
        @NotBlank @Size(max = 200) String title,
        @NotNull @Size(max = 10000) String body) {}

package com.hikmethankolay.template.note;

import com.hikmethankolay.template.user.PersonalDataProvider;
import org.springframework.stereotype.Component;

/** Adds the user's notes to the personal data export. */
@Component
public class NoteDataProvider implements PersonalDataProvider {

    private final NoteService noteService;

    /**
     * Creates the provider.
     *
     * @param noteService lists the notes
     */
    public NoteDataProvider(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * Returns the section name.
     *
     * @return {@code notes}
     */
    @Override
    public String section() {
        return "notes";
    }

    /**
     * Lists the user's notes.
     *
     * @param userId the user
     * @return every note, oldest first
     */
    @Override
    public Object export(long userId) {
        return noteService.export(userId);
    }
}

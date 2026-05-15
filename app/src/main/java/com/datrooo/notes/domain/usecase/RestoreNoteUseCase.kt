package com.datrooo.notes.domain.usecase

import com.datrooo.notes.domain.model.Note
import com.datrooo.notes.domain.repository.NotesRepository
import javax.inject.Inject

class RestoreNoteUseCase @Inject constructor(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(note: Note): Note {
        return repository.restoreNote(note)
    }
}


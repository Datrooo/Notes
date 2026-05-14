package com.datrooo.notes.domain.usecase

import com.datrooo.notes.domain.model.Note
import com.datrooo.notes.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow

class GetNoteUseCase(
    private val repository: NotesRepository
) {
    operator fun invoke(noteId: Long): Flow<Note?> {
        return repository.observeNote(noteId)
    }
}

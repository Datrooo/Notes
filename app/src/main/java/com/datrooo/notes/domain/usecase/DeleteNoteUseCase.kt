package com.datrooo.notes.domain.usecase

import com.datrooo.notes.domain.repository.NotesRepository

class DeleteNoteUseCase(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(noteId: Long) {
        repository.deleteNote(noteId)
    }
}

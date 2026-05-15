package com.datrooo.notes.domain.usecase

import com.datrooo.notes.domain.repository.NotesRepository
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(noteId: Long) {
        repository.deleteNote(noteId)
    }
}


package com.datrooo.notes.domain.usecase

import com.datrooo.notes.domain.model.Note
import com.datrooo.notes.domain.repository.NotesRepository

class CreateNoteUseCase(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(title: String, content: String): Note {
        return repository.createNote(title = title, content = content)
    }
}

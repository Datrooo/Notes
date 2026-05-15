package com.datrooo.notes.domain.usecase

import com.datrooo.notes.domain.model.Note
import com.datrooo.notes.domain.repository.NotesRepository

class UpdateNoteUseCase(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(noteId: Long, title: String, content: String, tags: List<String>): Note? {
        return repository.updateNote(noteId = noteId, title = title, content = content, tags = tags)
    }
}

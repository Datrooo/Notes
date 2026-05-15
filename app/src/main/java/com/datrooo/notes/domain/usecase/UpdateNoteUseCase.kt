package com.datrooo.notes.domain.usecase

import com.datrooo.notes.domain.model.Note
import com.datrooo.notes.domain.model.NoteContentBlock
import com.datrooo.notes.domain.repository.NotesRepository
import javax.inject.Inject

class UpdateNoteUseCase @Inject constructor(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(noteId: Long, title: String, content: List<NoteContentBlock>, tags: List<String>): Note? {
        return repository.updateNote(noteId = noteId, title = title, content = content, tags = tags)
    }
}


package com.datrooo.notes.data.repository

import com.datrooo.notes.data.local.NoteDao
import com.datrooo.notes.data.local.NoteEntity
import com.datrooo.notes.data.mapper.toDomain
import com.datrooo.notes.data.mapper.toEntity
import com.datrooo.notes.domain.model.Note
import com.datrooo.notes.domain.model.NoteContentBlock
import com.datrooo.notes.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomNotesRepository(
    private val noteDao: NoteDao
) : NotesRepository {
    override fun observeNotes(): Flow<List<Note>> {
        return noteDao.observeNotes().map { notes ->
            notes.map { it.toDomain() }
        }
    }

    override fun observeNote(noteId: Long): Flow<Note?> {
        return noteDao.observeNoteById(noteId).map { note ->
            note?.toDomain()
        }
    }

    override suspend fun createNote(title: String, content: List<NoteContentBlock>, tags: List<String>): Note {
        val now = System.currentTimeMillis()
        val noteId = noteDao.insertNote(
            NoteEntity(
                title = title,
                content = content,
                tags = tags,
                createdAt = now,
                updatedAt = now
            )
        )

        return requireNotNull(noteDao.getNoteById(noteId)) {
            "Inserted note with id=$noteId was not found"
        }.toDomain()
    }

    override suspend fun restoreNote(note: Note): Note {
        val restoredId = noteDao.insertNote(note.toEntity())
        return requireNotNull(noteDao.getNoteById(restoredId)) {
            "Restored note with id=$restoredId was not found"
        }.toDomain()
    }

    override suspend fun updateNote(noteId: Long, title: String, content: List<NoteContentBlock>, tags: List<String>): Note? {
        val currentNote = noteDao.getNoteById(noteId) ?: return null
        val updatedNote = currentNote.copy(
            title = title,
            content = content,
            tags = tags,
            updatedAt = System.currentTimeMillis()
        )

        noteDao.updateNote(updatedNote)
        return noteDao.getNoteById(noteId)?.toDomain()
    }

    override suspend fun deleteNote(noteId: Long) {
        noteDao.deleteNoteById(noteId)
    }
}

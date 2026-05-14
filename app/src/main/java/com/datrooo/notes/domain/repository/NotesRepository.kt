package com.datrooo.notes.domain.repository

import com.datrooo.notes.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun observeNotes(): Flow<List<Note>>
    fun observeNote(noteId: Long): Flow<Note?>
    suspend fun createNote(title: String, content: String, tags: List<String>): Note
    suspend fun restoreNote(note: Note): Note
    suspend fun updateNote(noteId: Long, title: String, content: String, tags: List<String>): Note?
    suspend fun deleteNote(noteId: Long)
}

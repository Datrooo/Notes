package com.datrooo.notes

import android.content.Context
import androidx.room.Room
import com.datrooo.notes.data.local.NotesDatabase
import com.datrooo.notes.data.repository.RoomNotesRepository
import com.datrooo.notes.domain.repository.NotesRepository

interface AppContainer {
    val notesRepository: NotesRepository
}

class DefaultAppContainer(
    context: Context
) : AppContainer {
    private val database: NotesDatabase by lazy {
        Room.databaseBuilder(
            context,
            NotesDatabase::class.java,
            NotesDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    override val notesRepository: NotesRepository by lazy {
        RoomNotesRepository(noteDao = database.noteDao())
    }
}

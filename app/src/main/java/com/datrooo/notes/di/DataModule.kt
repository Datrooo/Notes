package com.datrooo.notes.di

import android.content.Context
import androidx.room.Room
import com.datrooo.notes.data.local.ImageStorage
import com.datrooo.notes.data.local.NotesDatabase
import com.datrooo.notes.data.repository.RoomNotesRepository
import com.datrooo.notes.domain.repository.NotesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NotesDatabase {
        return Room.databaseBuilder(
            context,
            NotesDatabase::class.java,
            NotesDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideNotesRepository(database: NotesDatabase): NotesRepository {
        return RoomNotesRepository(noteDao = database.noteDao())
    }

    @Provides
    @Singleton
    fun provideImageStorage(@ApplicationContext context: Context): ImageStorage {
        return ImageStorage(context)
    }
}

package com.datrooo.notes.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.datrooo.notes.domain.model.NoteContentBlock

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: List<NoteContentBlock>,
    val tags: List<String>,
    val createdAt: Long,
    val updatedAt: Long
)

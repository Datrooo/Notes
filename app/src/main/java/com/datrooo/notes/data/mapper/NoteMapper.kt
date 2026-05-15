package com.datrooo.notes.data.mapper

import com.datrooo.notes.data.local.NoteEntity
import com.datrooo.notes.domain.model.Note

fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        tags = if (tags.isBlank()) emptyList() else tags.split(",").filter { it.isNotBlank() },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        tags = tags.joinToString(","),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

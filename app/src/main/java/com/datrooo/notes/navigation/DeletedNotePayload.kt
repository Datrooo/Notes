package com.datrooo.notes.navigation

import com.datrooo.notes.domain.model.Note
import java.io.Serializable

data class DeletedNotePayload(
    val id: Long,
    val title: String,
    val content: String,
    val tags: List<String>,
    val createdAt: Long,
    val updatedAt: Long
) : Serializable {
    fun toDomain(): Note {
        return Note(
            id = id,
            title = title,
            content = content,
            tags = tags,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

fun Note.toDeletedNotePayload(): DeletedNotePayload {
    return DeletedNotePayload(
        id = id,
        title = title,
        content = content,
        tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

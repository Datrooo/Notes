package com.datrooo.notes.data.local

import androidx.room.TypeConverter
import com.datrooo.notes.domain.model.NoteContentBlock
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromTagsList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toTagsList(value: String): List<String> {
        return if (value.isBlank()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromContentList(value: List<NoteContentBlock>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toContentList(value: String): List<NoteContentBlock> {
        return try {
            Json.decodeFromString(value)
        } catch (_: Exception) {
            listOf(NoteContentBlock.Text(value))
        }
    }
}

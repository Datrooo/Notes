package com.datrooo.notes.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class NoteContentBlock {
    @Serializable
    data class Text(val text: String) : NoteContentBlock()
    
    @Serializable
    data class Image(val uri: String) : NoteContentBlock()
}

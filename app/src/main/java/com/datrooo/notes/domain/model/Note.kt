package com.datrooo.notes.domain.model

data class Note(
    val id: Long,
    val title: String,
    val content: String,
    val tags: List<String>,
    val createdAt: Long,
    val updatedAt: Long
)

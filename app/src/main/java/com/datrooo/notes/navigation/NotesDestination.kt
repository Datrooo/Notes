package com.datrooo.notes.navigation

object NotesDestination {
    const val LIST_ROUTE = "notes"
    const val DETAILS_ROUTE = "details/{noteId}"
    const val EDITOR_ROUTE = "editor?noteId={noteId}"
    const val NOTE_ID_ARG = "noteId"
    const val EMPTY_NOTE_ID = -1L
    const val DELETED_NOTE_PAYLOAD_KEY = "deleted_note_payload"

    fun detailsRoute(noteId: Long): String = "details/$noteId"

    fun editorRoute(noteId: Long? = null): String {
        return if (noteId == null) {
            "editor"
        } else {
            "editor?noteId=$noteId"
        }
    }
}

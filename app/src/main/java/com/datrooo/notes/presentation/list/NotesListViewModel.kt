package com.datrooo.notes.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.datrooo.notes.domain.model.Note
import com.datrooo.notes.domain.model.NoteContentBlock
import com.datrooo.notes.domain.repository.NotesRepository
import com.datrooo.notes.domain.usecase.GetNotesUseCase
import com.datrooo.notes.domain.usecase.RestoreNoteUseCase
import com.datrooo.notes.navigation.DeletedNotePayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotesListUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val availableTags: List<String> = emptyList(),
    val selectedTags: Set<String> = emptySet(),
    val notes: List<Note> = emptyList(),
    val totalNotesCount: Int = 0
)

class NotesListViewModel(
    private val getNotesUseCase: GetNotesUseCase,
    private val restoreNoteUseCase: RestoreNoteUseCase
) : ViewModel() {
    private var allNotes: List<Note> = emptyList()
    private val _uiState = MutableStateFlow(NotesListUiState())
    val uiState: StateFlow<NotesListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getNotesUseCase().collect { notes ->
                allNotes = notes
                refreshVisibleNotes()
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            current.copy(searchQuery = query)
        }
        refreshVisibleNotes()
    }

    fun onTagToggle(tag: String) {
        _uiState.update { current ->
            val newSelectedTags = if (current.selectedTags.contains(tag)) {
                current.selectedTags - tag
            } else {
                current.selectedTags + tag
            }
            current.copy(selectedTags = newSelectedTags)
        }
        refreshVisibleNotes()
    }

    fun restoreDeletedNote(payload: DeletedNotePayload) {
        viewModelScope.launch {
            restoreNoteUseCase(payload.toDomain())
        }
    }

    private fun refreshVisibleNotes() {
        _uiState.update { current ->
            val allAvailableTags = allNotes.flatMap { it.tags }.distinct().sorted()
            
            val filteredNotes = allNotes.filter { note ->
                val matchesSearch = if (current.searchQuery.isBlank()) {
                    true
                } else {
                    note.title.contains(current.searchQuery, ignoreCase = true) ||
                            note.tags.any { it.contains(current.searchQuery, ignoreCase = true) } ||
                            note.content.any { block ->
                                (block as? NoteContentBlock.Text)?.text?.contains(current.searchQuery, ignoreCase = true) == true
                            }
                }

                val matchesTags = if (current.selectedTags.isEmpty()) {
                    true
                } else {
                    current.selectedTags.all { selectedTag ->
                        note.tags.contains(selectedTag)
                    }
                }

                matchesSearch && matchesTags
            }

            current.copy(
                isLoading = false,
                notes = filteredNotes,
                availableTags = allAvailableTags,
                totalNotesCount = allNotes.size
            )
        }
    }

    companion object {
        fun factory(repository: NotesRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                NotesListViewModel(
                    getNotesUseCase = GetNotesUseCase(repository),
                    restoreNoteUseCase = RestoreNoteUseCase(repository)
                )
            }
        }
    }
}

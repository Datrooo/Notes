package com.datrooo.notes.presentation.details

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.datrooo.notes.data.local.ImageStorage
import com.datrooo.notes.domain.model.Note
import com.datrooo.notes.domain.repository.NotesRepository
import com.datrooo.notes.domain.usecase.DeleteNoteUseCase
import com.datrooo.notes.domain.usecase.GetNoteUseCase
import com.datrooo.notes.navigation.DeletedNotePayload
import com.datrooo.notes.navigation.toDeletedNotePayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NoteDetailsUiState(
    val isLoading: Boolean = true,
    val note: Note? = null,
)

class NoteDetailsViewModel(
    private val noteId: Long,
    private val getNoteUseCase: GetNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val imageStorage: ImageStorage
) : ViewModel() {
    private val _uiState = MutableStateFlow(NoteDetailsUiState())
    val uiState: StateFlow<NoteDetailsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getNoteUseCase(noteId).collect { note ->
                _uiState.update { current ->
                    current.copy(isLoading = false, note = note)
                }
            }
        }
    }

    fun getShareableUri(uriString: String): Uri {
        return imageStorage.getShareableUri(uriString)
    }

    fun getPreferredPackage(): String? {
        return imageStorage.getPreferredPackage()
    }

    fun setPreferredPackage(packageName: String?) {
        imageStorage.setPreferredPackage(packageName)
    }

    fun getAvailableViewers(): List<Pair<String, String>> {
        return imageStorage.getAvailableViewers()
    }

    fun deleteNote(onDeleted: (DeletedNotePayload) -> Unit) {
        viewModelScope.launch {
            val deletedNote = _uiState.value.note ?: return@launch
            deleteNoteUseCase(noteId)
            onDeleted(deletedNote.toDeletedNotePayload())
        }
    }

    companion object {
        fun factory(
            repository: NotesRepository,
            imageStorage: ImageStorage,
            noteId: Long
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                NoteDetailsViewModel(
                    noteId = noteId,
                    getNoteUseCase = GetNoteUseCase(repository),
                    deleteNoteUseCase = DeleteNoteUseCase(repository),
                    imageStorage = imageStorage
                )
            }
        }
    }
}

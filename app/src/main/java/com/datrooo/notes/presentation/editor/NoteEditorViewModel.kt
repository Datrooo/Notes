package com.datrooo.notes.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import android.net.Uri
import com.datrooo.notes.data.local.ImageStorage
import com.datrooo.notes.domain.model.NoteContentBlock
import com.datrooo.notes.domain.repository.NotesRepository
import com.datrooo.notes.domain.usecase.CreateNoteUseCase
import com.datrooo.notes.domain.usecase.GetNoteUseCase
import com.datrooo.notes.domain.usecase.UpdateNoteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NoteEditorUiState(
    val noteId: Long? = null,
    val title: String = "",
    val content: List<NoteContentBlock> = listOf(NoteContentBlock.Text("")),
    val tags: String = "",
    val isLoading: Boolean = false,
    val isExistingNote: Boolean = false,
) {
    val canSave: Boolean
        get() = (title.isNotBlank() || content.any { block -> (block as? NoteContentBlock.Text)?.text?.isNotBlank() == true } || content.any { it is NoteContentBlock.Image }) &&
                title.length <= NoteEditorViewModel.MAX_TITLE_LENGTH &&
                !hasTooLongTags

    val isTitleTooLong: Boolean
        get() = title.length > NoteEditorViewModel.MAX_TITLE_LENGTH

    val hasTooLongTags: Boolean
        get() = tags.split(Regex("[,\\s]+"))
            .asSequence()
            .map { it.trim().removePrefix("#") }
            .any { it.length > NoteEditorViewModel.MAX_TAG_LENGTH }
}

class NoteEditorViewModel(
    noteId: Long?,
    private val getNoteUseCase: GetNoteUseCase,
    private val createNoteUseCase: CreateNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val imageStorage: ImageStorage
) : ViewModel() {
    companion object {
        const val MAX_TITLE_LENGTH = 50
        const val MAX_TAG_LENGTH = 20

        fun factory(
            repository: NotesRepository,
            imageStorage: ImageStorage,
            noteId: Long?
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                NoteEditorViewModel(
                    noteId = noteId,
                    getNoteUseCase = GetNoteUseCase(repository),
                    createNoteUseCase = CreateNoteUseCase(repository),
                    updateNoteUseCase = UpdateNoteUseCase(repository),
                    imageStorage = imageStorage
                )
            }
        }
    }
    private val _uiState = MutableStateFlow(
        NoteEditorUiState(
            noteId = noteId,
            isLoading = noteId != null,
            isExistingNote = noteId != null
        )
    )
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    init {
        if (noteId != null) {
            viewModelScope.launch {
                getNoteUseCase(noteId).collect { note ->
                    _uiState.update { current ->
                        current.copy(
                            title = note?.title.orEmpty(),
                            content = note?.content ?: listOf(NoteContentBlock.Text("")),
                            tags = note?.tags?.joinToString(" ") ?: "",
                            isLoading = false,
                            isExistingNote = note != null
                        )
                    }
                }
            }
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { current ->
            current.copy(title = title)
        }
    }

    fun onContentBlockChanged(index: Int, block: NoteContentBlock) {
        _uiState.update { current ->
            val newContent = current.content.toMutableList()
            newContent[index] = block
            current.copy(content = newContent)
        }
    }

    fun addImageBlock(uri: String, atBlockIndex: Int? = null, atOffset: Int = 0) {
        viewModelScope.launch {
            val internalUri = imageStorage.saveImageToInternalStorage(uri) ?: return@launch
            _uiState.update { current ->
                val newContent = current.content.toMutableList()
                
                if (atBlockIndex != null && atBlockIndex < newContent.size) {
                    val targetBlock = newContent[atBlockIndex]
                    if (targetBlock is NoteContentBlock.Text) {
                        val textBefore = targetBlock.text.substring(0, atOffset)
                        val textAfter = targetBlock.text.substring(atOffset)
                        
                        newContent[atBlockIndex] = NoteContentBlock.Text(textBefore)
                        newContent.add(atBlockIndex + 1, NoteContentBlock.Image(internalUri))
                        newContent.add(atBlockIndex + 2, NoteContentBlock.Text(textAfter))
                    } else {
                        newContent.add(atBlockIndex + 1, NoteContentBlock.Image(internalUri))
                        newContent.add(atBlockIndex + 2, NoteContentBlock.Text(""))
                    }
                } else {
                    newContent.add(NoteContentBlock.Image(internalUri))
                    newContent.add(NoteContentBlock.Text(""))
                }
                current.copy(content = newContent)
            }
        }
    }

    fun removeBlock(index: Int) {
        _uiState.update { current ->
            if (current.content.size <= 1) return@update current
            val newContent = current.content.toMutableList()
            newContent.removeAt(index)

            if (index > 0 && index < newContent.size) {
                val prevBlock = newContent[index - 1]
                val nextBlock = newContent[index]
                if (prevBlock is NoteContentBlock.Text && nextBlock is NoteContentBlock.Text) {
                    newContent[index - 1] = NoteContentBlock.Text(prevBlock.text + nextBlock.text)
                    newContent.removeAt(index)
                }
            }
            current.copy(content = newContent)
        }
    }

    fun onTagsChanged(tags: String) {
        _uiState.update { current ->
            current.copy(tags = tags)
        }
    }

    fun getTempCameraUri(): Uri {
        return imageStorage.getTempCameraUri()
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

    fun saveImageToGallery(uriString: String): Boolean {
        return imageStorage.saveImageToGallery(uriString)
    }

    fun save(onSaved: () -> Unit) {
        val currentState = _uiState.value
        if (!currentState.canSave) {
            return
        }

        viewModelScope.launch {
            val title = currentState.title.trim()
            val content = currentState.content
            val tags = currentState.tags
                .split(Regex("[,\\s]+"))
                .map { it.trim().removePrefix("#") }
                .filter { it.isNotBlank() }
                .distinct()
            val noteId = currentState.noteId

            if (noteId == null) {
                createNoteUseCase(title = title, content = content, tags = tags)
            } else {
                updateNoteUseCase(noteId = noteId, title = title, content = content, tags = tags)
            }

            onSaved()
        }
    }
}

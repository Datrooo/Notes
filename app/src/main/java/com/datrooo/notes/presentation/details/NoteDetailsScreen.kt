package com.datrooo.notes.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.datrooo.notes.R
import com.datrooo.notes.domain.model.NoteContentBlock
import com.datrooo.notes.presentation.components.NotesBackground
import com.datrooo.notes.presentation.components.formatForUi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteDetailsScreen(
    viewModel: NoteDetailsViewModel,
    onNavigateBack: () -> Unit,
    onEditNote: (Long) -> Unit,
    onNoteDeleted: (com.datrooo.notes.navigation.DeletedNotePayload) -> Unit,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val showDeleteDialog = remember { mutableStateOf(value = false) }
    var isTagsExpanded by remember { mutableStateOf(value = false) }
    var fullScreenImageUri by remember { mutableStateOf<String?>(null) }

    if (fullScreenImageUri != null) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black)
                    .clickable { 
                        fullScreenImageUri = null 
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = fullScreenImageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog.value = false
            },
            title = {
                Text(text = stringResource(R.string.delete_dialog_title))
            },
            text = {
                Text(text = stringResource(R.string.delete_dialog_desc))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog.value = false
                        viewModel.deleteNote(onNoteDeleted)
                    }
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog.value = false
                    }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotesBackground()
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    title = {
                        Text(text = stringResource(R.string.view_note_title))
                    },
                    navigationIcon = {
                        TextButton(onClick = onNavigateBack) {
                            Text(text = stringResource(R.string.back))
                        }
                    }
                )
            }
        ) { innerPadding ->
            when {
                uiState.value.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.value.note == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Text(
                                text = stringResource(R.string.note_not_found),
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    val note = uiState.value.note ?: return@Scaffold

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.93f),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = note.title.ifBlank { stringResource(R.string.no_title) },
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                    shape = MaterialTheme.shapes.large
                                ) {
                                    Text(
                                        text = stringResource(R.string.updated_at, note.updatedAt.formatForUi()),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }

                        if (note.tags.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shape = MaterialTheme.shapes.extraLarge
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isTagsExpanded) {
                                        FlowRow(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            note.tags.forEach { tag ->
                                                AssistChip(
                                                    onClick = { },
                                                    label = { Text(text = "#$tag") },
                                                    colors = AssistChipDefaults.assistChipColors(
                                                        labelColor = MaterialTheme.colorScheme.primary
                                                    ),
                                                    border = null,
                                                    shape = MaterialTheme.shapes.small
                                                )
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val displayTags = note.tags.take(3)
                                            displayTags.forEach { tag ->
                                                AssistChip(
                                                    onClick = { },
                                                    label = { Text(text = "#$tag") },
                                                    colors = AssistChipDefaults.assistChipColors(
                                                        labelColor = MaterialTheme.colorScheme.primary
                                                    ),
                                                    border = null,
                                                    shape = MaterialTheme.shapes.small
                                                )
                                            }
                                            if (note.tags.size > 3) {
                                                Text(
                                                    text = "+${note.tags.size - 3}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }

                                    if (note.tags.size > 3) {
                                        IconButton(onClick = { isTagsExpanded = !isTagsExpanded }) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            ) {
                                                Text(
                                                    text = if (isTagsExpanded) "−" else "+",
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.note_content_header),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                note.content.forEach { block ->
                                    when (block) {
                                        is NoteContentBlock.Text -> {
                                            if (block.text.isNotBlank()) {
                                                Text(
                                                    text = block.text,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            }
                                        }
                                        is NoteContentBlock.Image -> {
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { fullScreenImageUri = block.uri },
                                                shape = MaterialTheme.shapes.medium
                                            ) {
                                                AsyncImage(
                                                    model = block.uri,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    contentScale = ContentScale.FillWidth
                                                )
                                            }
                                        }
                                    }
                                }
                                if (note.content.all { (it as? NoteContentBlock.Text)?.text?.isBlank() == true } && note.content.none { it is NoteContentBlock.Image }) {
                                    Text(
                                        text = stringResource(R.string.empty_content_desc),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    onEditNote(note.id)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = stringResource(R.string.edit))
                            }
                            OutlinedButton(
                                onClick = {
                                    showDeleteDialog.value = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = stringResource(R.string.delete))
                            }
                        }
                    }
                }
            }
        }
    }
}

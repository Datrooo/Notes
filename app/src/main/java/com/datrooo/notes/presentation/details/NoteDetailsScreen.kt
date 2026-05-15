package com.datrooo.notes.presentation.details

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.datrooo.notes.R
import com.datrooo.notes.domain.model.NoteContentBlock
import com.datrooo.notes.presentation.components.NotesBackground
import com.datrooo.notes.presentation.components.formatForUi
import com.datrooo.notes.presentation.editor.AppSelectionDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteDetailsScreen(
    viewModel: NoteDetailsViewModel,
    onNavigateBack: () -> Unit,
    onEditNote: (Long) -> Unit,
    onNoteDeleted: (com.datrooo.notes.navigation.DeletedNotePayload) -> Unit,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val showDeleteDialog = remember { mutableStateOf(false) }
    var isTagsExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val errorOpeningImageMsg = stringResource(R.string.error_opening_image)

    val showAppPicker = remember { mutableStateOf(false) }
    val pendingUriToOpen = remember { mutableStateOf<android.net.Uri?>(null) }

    val openImage = { uri: android.net.Uri ->
        val preferred = viewModel.getPreferredPackage()
        if (preferred != null) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "image/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setPackage(preferred)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                viewModel.setPreferredPackage(null)
                pendingUriToOpen.value = uri
                showAppPicker.value = true
            }
        } else {
            pendingUriToOpen.value = uri
            showAppPicker.value = true
        }
    }

    if (showAppPicker.value && pendingUriToOpen.value != null) {
        AppSelectionDialog(
            viewers = viewModel.getAvailableViewers(),
            onAppSelected = { pkg, rememberChoice ->
                if (rememberChoice) {
                    viewModel.setPreferredPackage(pkg)
                }
                showAppPicker.value = false
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(pendingUriToOpen.value, "image/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setPackage(pkg)
                }
                context.startActivity(intent)
                pendingUriToOpen.value = null
            },
            onDismiss = { 
                showAppPicker.value = false
                pendingUriToOpen.value = null
            }
        )
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
                    },
                    actions = {
                        if (viewModel.getPreferredPackage() != null) {
                            IconButton(onClick = { viewModel.setPreferredPackage(null) }) {
                                Icon(
                                    imageVector = Icons.Default.SettingsBackupRestore,
                                    contentDescription = stringResource(R.string.reset_preferred_viewer),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
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
                                                    .clickable { 
                                                        try {
                                                            val shareableUri = viewModel.getShareableUri(block.uri)
                                                            openImage(shareableUri)
                                                        } catch (_: Exception) {
                                                            Toast.makeText(context, errorOpeningImageMsg, Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
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

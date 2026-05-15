package com.datrooo.notes.presentation.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.datrooo.notes.R
import com.datrooo.notes.presentation.components.NotesBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.value.content) {
        scrollState.animateScrollTo(scrollState.maxValue)
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
                        Text(
                            text = if (uiState.value.isExistingNote) {
                                stringResource(R.string.edit_note_title)
                            } else {
                                stringResource(R.string.new_note_title)
                            }
                        )
                    },
                    navigationIcon = {
                        TextButton(onClick = onNavigateBack) {
                            Text(text = stringResource(R.string.back))
                        }
                    }
                )
            }
        ) { innerPadding ->
            if (uiState.value.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.93f),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.value.title,
                            onValueChange = viewModel::onTitleChanged,
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(text = stringResource(R.string.title_label))
                            },
                            placeholder = {
                                Text(text = stringResource(R.string.title_placeholder))
                            },
                            singleLine = true,
                            isError = uiState.value.isTitleTooLong,
                            supportingText = {
                                if (uiState.value.isTitleTooLong) {
                                    Text(
                                        text = stringResource(
                                            R.string.title_too_long,
                                            NoteEditorViewModel.MAX_TITLE_LENGTH
                                        ),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                        OutlinedTextField(
                            value = uiState.value.content,
                            onValueChange = viewModel::onContentChanged,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 10,
                            label = {
                                Text(text = stringResource(R.string.content_label))
                            },
                            placeholder = {
                                Text(text = stringResource(R.string.content_placeholder))
                            }
                        )
                        OutlinedTextField(
                            value = uiState.value.tags,
                            onValueChange = viewModel::onTagsChanged,
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(text = stringResource(R.string.tags_label))
                            },
                            placeholder = {
                                Text(text = stringResource(R.string.tags_placeholder))
                            },
                            singleLine = true,
                            isError = uiState.value.hasTooLongTags,
                            supportingText = {
                                if (uiState.value.hasTooLongTags) {
                                    Text(
                                        text = stringResource(
                                            R.string.tag_too_long,
                                            NoteEditorViewModel.MAX_TAG_LENGTH
                                        ),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                        Text(
                            text = stringResource(
                                R.string.char_count_total,
                                uiState.value.title.length + uiState.value.content.length
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.save(onSaved)
                    },
                    enabled = uiState.value.canSave,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (uiState.value.isExistingNote) {
                            stringResource(R.string.save)
                        } else {
                            stringResource(R.string.create_note_button)
                        }
                    )
                }
            }
        }
    }
}

package com.datrooo.notes.presentation.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.datrooo.notes.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.datrooo.notes.presentation.components.NoteCard
import com.datrooo.notes.presentation.components.NotesBackground
import com.datrooo.notes.navigation.DeletedNotePayload
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesListViewModel,
    deletedNotePayload: DeletedNotePayload?,
    onSnackbarConsumed: () -> Unit,
    onAddNoteClick: () -> Unit,
    onNoteClick: (Long) -> Unit,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var backPressedOnce by remember { mutableStateOf(value = false) }

    val exitMessage = stringResource(R.string.exit_press_again)
    val noTitle = stringResource(R.string.no_title)
    val undoLabel = stringResource(R.string.undo)
    val deletedNoteTitle = deletedNotePayload?.title?.ifBlank { noTitle } ?: ""
    val deletedMessage = stringResource(R.string.note_deleted_msg, deletedNoteTitle)

    BackHandler(enabled = true) {
        if (backPressedOnce) {
            (context as? android.app.Activity)?.finish()
        } else {
            backPressedOnce = true
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = exitMessage,
                    duration = SnackbarDuration.Short,
                )
                delay(2000)
                backPressedOnce = false
            }
        }
    }

    LaunchedEffect(deletedNotePayload) {
        if (deletedNotePayload != null) {
            val result = snackbarHostState.showSnackbar(
                message = deletedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.restoreDeletedNote(deletedNotePayload)
            }
            onSnackbarConsumed()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotesBackground()
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    title = {
                        Column {
                            Text(text = stringResource(R.string.my_notes))
                            Text(
                                text = if (uiState.value.totalNotesCount == 0) {
                                    stringResource(R.string.notes_count_zero)
                                } else if (uiState.value.searchQuery.isBlank()) {
                                    pluralStringResource(
                                        R.plurals.notes_count_ready,
                                        uiState.value.totalNotesCount,
                                        uiState.value.totalNotesCount
                                    )
                                } else {
                                    stringResource(
                                        R.string.notes_search_results,
                                        uiState.value.notes.size,
                                        uiState.value.totalNotesCount
                                    )
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onAddNoteClick
                ) {
                    Text(text = stringResource(R.string.new_note_fab))
                }
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
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 104.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        SearchSection(
                            searchQuery = uiState.value.searchQuery,
                            onSearchQueryChanged = viewModel::onSearchQueryChanged
                        )
                    }
                    if (uiState.value.availableTags.isNotEmpty()) {
                        item {
                            TagFilterSection(
                                availableTags = uiState.value.availableTags,
                                selectedTags = uiState.value.selectedTags,
                                onTagToggle = viewModel::onTagToggle
                            )
                        }
                    }
                    if (uiState.value.notes.isEmpty()) {
                        item {
                            EmptyNotesState(
                                searchQuery = uiState.value.searchQuery,
                                hasAnyNotes = uiState.value.totalNotesCount > 0,
                                onClearSearch = {
                                    viewModel.onSearchQueryChanged("")
                                }
                            )
                        }
                    } else {
                        items(
                            items = uiState.value.notes,
                            key = { note -> note.id }
                        ) { note ->
                            NoteCard(
                                note = note,
                                onClick = {
                                    onNoteClick(note.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagFilterSection(
    availableTags: List<String>,
    selectedTags: Set<String>,
    onTagToggle: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(value = false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (!isExpanded) {
                        val unselectedTags = availableTags.filter { !selectedTags.contains(it) }
                        val tagsToShow = (selectedTags.toList() + unselectedTags.take(3)).distinct()

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(tagsToShow) { tag ->
                                FilterChip(
                                    selected = selectedTags.contains(tag),
                                    onClick = { onTagToggle(tag) },
                                    label = { Text(text = "#$tag") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            availableTags.forEach { tag ->
                                FilterChip(
                                    selected = selectedTags.contains(tag),
                                    onClick = { onTagToggle(tag) },
                                    label = { Text(text = "#$tag") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ) {
                        Text(
                            text = if (isExpanded) "−" else "+",
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

@Composable
private fun EmptyNotesState(
    searchQuery: String,
    hasAnyNotes: Boolean,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.padding(20.dp), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (hasAnyNotes && searchQuery.isNotBlank()) "?" else "0",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (hasAnyNotes && searchQuery.isNotBlank()) {
                        stringResource(R.string.nothing_found)
                    } else {
                        stringResource(R.string.no_notes_yet)
                    },
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = if (hasAnyNotes && searchQuery.isNotBlank()) {
                        stringResource(R.string.search_no_matches_desc, searchQuery)
                    } else {
                        stringResource(R.string.empty_list_desc)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                if (hasAnyNotes && searchQuery.isNotBlank()) {
                    OutlinedButton(onClick = onClearSearch) {
                        Text(text = stringResource(R.string.clear_search))
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSection(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.search_by_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text(text = stringResource(R.string.search_placeholder))
                }
            )
        }
    }
}

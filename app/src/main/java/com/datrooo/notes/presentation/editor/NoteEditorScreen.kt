package com.datrooo.notes.presentation.editor

import android.Manifest
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.datrooo.notes.R
import com.datrooo.notes.domain.model.NoteContentBlock
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
    val context = LocalContext.current

    val errorOpeningImageMsg = stringResource(R.string.error_opening_image)
    val imageSavedMsg = stringResource(R.string.image_saved_to_gallery)
    val imageSaveFailedMsg = stringResource(R.string.image_save_failed)

    var tempCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showImageSourceMenu by remember { mutableStateOf(false) }
    
    var lastFocusedBlockIndex by remember { mutableStateOf<Int?>(null) }
    var lastCursorPosition by remember { mutableStateOf(0) }

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

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.addImageBlock(it.toString(), lastFocusedBlockIndex, lastCursorPosition) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { viewModel.addImageBlock(it.toString(), lastFocusedBlockIndex, lastCursorPosition) }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = viewModel.getTempCameraUri()
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    LaunchedEffect(uiState.value.content) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotesBackground()
        Scaffold(
            modifier = Modifier.fillMaxSize().imePadding(),
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
            },
            bottomBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            IconButton(onClick = { showImageSourceMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Add Image",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            DropdownMenu(
                                expanded = showImageSourceMenu,
                                onDismissRequest = { showImageSourceMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.gallery)) },
                                    leadingIcon = { Icon(Icons.Default.AddPhotoAlternate, null) },
                                    onClick = {
                                        showImageSourceMenu = false
                                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.camera)) },
                                    leadingIcon = { Icon(Icons.Default.PhotoCamera, null) },
                                    onClick = {
                                        showImageSourceMenu = false
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                )
                            }
                        }
                    }
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

                        uiState.value.content.forEachIndexed { index, block ->
                            when (block) {
                                is NoteContentBlock.Text -> {
                                    val textFieldValue = remember(block.text) {
                                        mutableStateOf(
                                            TextFieldValue(
                                                text = block.text,
                                                selection = if (lastFocusedBlockIndex == index) {
                                                    TextRange(lastCursorPosition)
                                                } else {
                                                    TextRange.Zero
                                                }
                                            )
                                        )
                                    }
                                    androidx.compose.material3.TextField(
                                        value = textFieldValue.value,
                                        onValueChange = { 
                                            textFieldValue.value = it
                                            lastFocusedBlockIndex = index
                                            lastCursorPosition = it.selection.start
                                            viewModel.onContentBlockChanged(index, NoteContentBlock.Text(it.text)) 
                                        },
                                        modifier = Modifier.onFocusChanged {
                                            if (it.isFocused) lastFocusedBlockIndex = index 
                                        }.fillMaxWidth(),
                                        placeholder = {
                                            if (index == 0 && uiState.value.content.size == 1) {
                                                Text(text = stringResource(R.string.content_placeholder))
                                            }
                                        },
                                        colors = androidx.compose.material3.TextFieldDefaults.colors(
                                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                                        ),
                                        textStyle = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                is NoteContentBlock.Image -> {
                                    Box(modifier = Modifier.fillMaxWidth()) {
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
                                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.align(Alignment.TopEnd),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            ) {
                                                Row {
                                                    IconButton(
                                                        onClick = { 
                                                            if (viewModel.saveImageToGallery(block.uri)) {
                                                                Toast.makeText(context, imageSavedMsg, Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                Toast.makeText(context, imageSaveFailedMsg, Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Download,
                                                            contentDescription = "Save to Gallery",
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { viewModel.removeBlock(index) }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Remove Image",
                                                            tint = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

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
                                uiState.value.content.sumOf { (it as? NoteContentBlock.Text)?.text?.length ?: 0 }
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

@Composable
fun AppSelectionDialog(
    viewers: List<Pair<String, String>>,
    onAppSelected: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var rememberChoice by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_viewer_title),
                    style = MaterialTheme.typography.headlineSmall
                )

                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewers) { (pkg, label) ->
                        Surface(
                            onClick = { onAppSelected(pkg, rememberChoice) },
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = label, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { rememberChoice = !rememberChoice }
                ) {
                    Checkbox(
                        checked = rememberChoice,
                        onCheckedChange = { rememberChoice = it }
                    )
                    Text(
                        text = stringResource(R.string.remember_choice),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        }
    }
}

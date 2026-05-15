package com.datrooo.notes.presentation.editor

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
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
    var fullScreenImageUri by remember { mutableStateOf<String?>(null) }
    var tempCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showImageSourceMenu by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.addImageBlock(it.toString()) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { viewModel.addImageBlock(it.toString()) }
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
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showImageSourceMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Add Image"
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

                        uiState.value.content.forEachIndexed { index, block ->
                            when (block) {
                                is NoteContentBlock.Text -> {
                                    OutlinedTextField(
                                        value = block.text,
                                        onValueChange = { 
                                            viewModel.onContentBlockChanged(index, NoteContentBlock.Text(it)) 
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = {
                                            if (index == 0) Text(text = stringResource(R.string.content_label))
                                        },
                                        placeholder = {
                                            if (index == 0) Text(text = stringResource(R.string.content_placeholder))
                                        }
                                    )
                                }
                                is NoteContentBlock.Image -> {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { fullScreenImageUri = block.uri },
                                            shape = MaterialTheme.shapes.medium
                                        ) {
                                            AsyncImage(
                                                model = block.uri,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.removeBlock(index) },
                                            modifier = Modifier.align(Alignment.TopEnd)
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

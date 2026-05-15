package com.datrooo.notes.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.datrooo.notes.AppContainer
import com.datrooo.notes.presentation.details.NoteDetailsScreen
import com.datrooo.notes.presentation.details.NoteDetailsViewModel
import com.datrooo.notes.presentation.editor.NoteEditorScreen
import com.datrooo.notes.presentation.editor.NoteEditorViewModel
import com.datrooo.notes.presentation.list.NotesListScreen
import com.datrooo.notes.presentation.list.NotesListViewModel

@Composable
fun NotesNavHost(
    navController: androidx.navigation.NavHostController = rememberNavController(),
    appContainer: AppContainer
) {
    val repository = appContainer.notesRepository
    val imageStorage = appContainer.imageStorage

    NavHost(
        navController = navController,
        startDestination = NotesDestination.LIST_ROUTE
    ) {
        composable(route = NotesDestination.LIST_ROUTE) { backStackEntry ->
            val viewModel: NotesListViewModel = viewModel(
                factory = NotesListViewModel.factory(repository)
            )
            val deletedNotePayload = backStackEntry.savedStateHandle
                .getStateFlow<DeletedNotePayload?>(NotesDestination.DELETED_NOTE_PAYLOAD_KEY, null)
                .collectAsStateWithLifecycle()

            NotesListScreen(
                viewModel = viewModel,
                deletedNotePayload = deletedNotePayload.value,
                onSnackbarConsumed = {
                    backStackEntry.savedStateHandle[NotesDestination.DELETED_NOTE_PAYLOAD_KEY] = null
                },
                onAddNoteClick = {
                    navController.navigate(NotesDestination.editorRoute())
                },
                onNoteClick = { noteId ->
                    navController.navigate(NotesDestination.detailsRoute(noteId))
                }
            )
        }

        composable(
            route = NotesDestination.DETAILS_ROUTE,
            arguments = listOf(
                navArgument(NotesDestination.NOTE_ID_ARG) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong(NotesDestination.NOTE_ID_ARG)
                ?: NotesDestination.EMPTY_NOTE_ID

            val viewModel: NoteDetailsViewModel = viewModel(
                factory = NoteDetailsViewModel.factory(repository, noteId)
            )

            NoteDetailsScreen(
                viewModel = viewModel,
                onNavigateBack = navController::popBackStack,
                onEditNote = { currentNoteId ->
                    navController.navigate(NotesDestination.editorRoute(currentNoteId))
                },
                onNoteDeleted = { payload ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(NotesDestination.DELETED_NOTE_PAYLOAD_KEY, payload)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = NotesDestination.EDITOR_ROUTE,
            arguments = listOf(
                navArgument(NotesDestination.NOTE_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = NotesDestination.EMPTY_NOTE_ID
                }
            )
        ) { backStackEntry ->
            val rawNoteId = backStackEntry.arguments?.getLong(NotesDestination.NOTE_ID_ARG)
                ?: NotesDestination.EMPTY_NOTE_ID
            val noteId = rawNoteId.takeIf { it != NotesDestination.EMPTY_NOTE_ID }

            val viewModel: NoteEditorViewModel = viewModel(
                factory = NoteEditorViewModel.factory(repository, imageStorage, noteId)
            )

            NoteEditorScreen(
                viewModel = viewModel,
                onNavigateBack = navController::popBackStack,
                onSaved = {
                    navController.popBackStack()
                }
            )
        }
    }
}

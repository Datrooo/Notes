package com.datrooo.notes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.datrooo.notes.navigation.NotesNavHost

@androidx.compose.runtime.Composable
fun NotesApp(
    appContainer: AppContainer
) {
    val navController = rememberNavController()

    Surface(modifier = Modifier.fillMaxSize()) {
        NotesNavHost(
            navController = navController,
            appContainer = appContainer
        )
    }
}

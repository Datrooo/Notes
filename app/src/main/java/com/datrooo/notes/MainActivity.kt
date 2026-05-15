package com.datrooo.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.datrooo.notes.ui.theme.NotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (application as NotesApplication).container

        setContent {
            NotesTheme {
                NotesApp(appContainer = appContainer)
            }
        }
    }
}

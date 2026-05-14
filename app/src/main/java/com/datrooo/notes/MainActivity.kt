package com.datrooo.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.datrooo.notes.ui.theme.NotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as NotesApplication).container.notesRepository

        setContent {
            NotesTheme {
                NotesApp(repository = repository)
            }
        }
    }
}

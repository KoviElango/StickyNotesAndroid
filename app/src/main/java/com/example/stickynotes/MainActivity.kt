package com.example.stickynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to the composable function StickyNotesApp
        setContent {
            StickyNotesApp()
        }
    }
}

data class Note(
    val id: Long,
    val content: String
)

@Composable
fun StickyNotesApp() {
    var notes by remember { mutableStateOf(listOf<Note>()) }
    var newNoteText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        NotesList(notes = notes)

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = newNoteText,
                onValueChange = { newNoteText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Enter your note") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            AddNoteButton(onClick = {
                if (newNoteText.isNotEmpty()) {
                    val newNote = Note(
                        id = System.currentTimeMillis(),
                        content = newNoteText
                    )
                    notes = notes + newNote
                    newNoteText = ""
                }
            })
        }
    }
}

@Composable
fun NotesList(notes: List<Note>) {
    LazyColumn(modifier = Modifier) {
        items(notes) { note ->
            Text(
                text = note.content,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun AddNoteButton(onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .padding(15.dp)
            .width(120.dp)
            .height(40.dp)
    ) {
        Text(text = "Add Note")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    StickyNotesApp()
}
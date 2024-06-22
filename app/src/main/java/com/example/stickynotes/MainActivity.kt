package com.example.stickynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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
    var content: String
)

@Composable
fun StickyNotesApp() {
    var notes by remember { mutableStateOf(listOf<Note>()) }
    var newNoteText by remember { mutableStateOf("") }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var isDialogOpen by remember { mutableStateOf(false) }

    if (isDialogOpen && selectedNote != null) {
        EditNoteDialog(
            note = selectedNote!!,
            onDismiss = { isDialogOpen = false },
            onSave = {
                isDialogOpen = false
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        NotesList(notes = notes, onNoteClick = { note ->
            selectedNote = note
            isDialogOpen = true
        })

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
fun NotesList(notes: List<Note>, onNoteClick: (Note) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(notes) { note ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { onNoteClick(note) },
                //elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = note.content)
                }
            }
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

@Composable
fun EditNoteDialog(note: Note, onDismiss: () -> Unit, onSave: () -> Unit) {
    var content by remember { mutableStateOf(note.content) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Note") },
        text = {
            Column {
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.height(150.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                note.content = content
                onSave()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    StickyNotesApp()
}

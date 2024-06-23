package com.example.stickynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StickyNotesApp()
        }
    }
}

data class Note(
    val id: Long,
    var content: String
)

fun saveNotes(context: Context, notes: List<Note>) {
    val sharedPreferences = context.getSharedPreferences("notes_pref", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val gson = Gson()
    val json = gson.toJson(notes)
    editor.putString("notes_key", json)
    editor.apply()
}

fun loadNotes(context: Context): List<Note> {
    val sharedPreferences = context.getSharedPreferences("notes_pref", Context.MODE_PRIVATE)
    val gson = Gson()
    val json = sharedPreferences.getString("notes_key", null)
    val type = object : TypeToken<List<Note>>() {}.type
    return gson.fromJson(json, type) ?: emptyList()
}

@Composable
fun StickyNotesApp() {
    val context = LocalContext.current
    var notes by remember { mutableStateOf(loadNotes(context)) }
    var newNoteText by remember { mutableStateOf("") }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var isDialogOpen by remember { mutableStateOf(false) }

    if (isDialogOpen && selectedNote != null) {
        EditNoteDialog(
            note = selectedNote!!,
            onDismiss = { isDialogOpen = false },
            onSave = {
                saveNotes(context, notes)
                isDialogOpen = false
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        NotesList(
            notes = notes, onNoteClick = { note ->
                selectedNote = note
                isDialogOpen = true
            },
            onNoteDelete = { note ->
                notes = notes.filter { it.id != note.id }
                saveNotes(context, notes)
            },
            modifier = Modifier.weight(1f)
        )
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, // Position items with space between
            verticalAlignment = Alignment.CenterVertically // Align items vertically to the center
        ) {
            TextField(
                value = newNoteText,
                onValueChange = { newNoteText = it },
                modifier = Modifier.weight(1f), // TextField takes available space
                label = { Text("Enter your note") }
            )
            Spacer(modifier = Modifier.width(8.dp)) // Space between TextField and Button
            AddNoteButton(onClick = {
                if (newNoteText.isNotEmpty()) {
                    val newNote = Note(
                        id = System.currentTimeMillis(),
                        content = newNoteText
                    )
                    notes = notes + newNote
                    newNoteText = ""
                    saveNotes(context, notes)
                }
            })
        }
    }
}

@Composable
fun NotesList(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,onNoteDelete: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        notes.forEach {note ->
            NoteItem(note, onNoteClick, onNoteDelete)
        }
    }
}

@Composable
fun NoteItem(note: Note, onNoteClick: (Note) -> Unit, onNoteDelete: (Note) -> Unit) {
    Box(modifier = Modifier
        .offset(x = 4.dp, y = 4.dp) // Offset the Box containing the Card
    ) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onNoteClick(note) }
        ) {
            Box(modifier = Modifier
                .background(Color(0xFFFFCA47))
                .fillMaxWidth()
                .padding(16.dp)
            ) {
                Text(text = note.content)
                IconButton(
                    onClick = { onNoteDelete(note) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Note"
                    )
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
            .padding(3.dp)
            .width(60.dp).height(60.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = "Add Note",
            modifier= Modifier.size(28.dp)
        )
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

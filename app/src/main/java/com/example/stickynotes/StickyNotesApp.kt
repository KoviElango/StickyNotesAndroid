package com.example.stickynotes

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun StickyNotesApp() {
    val context = LocalContext.current
    var notes by remember { mutableStateOf(loadNotes(context)) }
    var newNoteText by remember { mutableStateOf("") }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var isDialogOpen by remember { mutableStateOf(false) }
    val soundEffects = soundEffects()

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
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        NotesList(
            notes = notes,
            onNoteClick = { note ->
                selectedNote = note
                isDialogOpen = true
            },
            onNoteDelete = { note ->
                notes = notes.filter { it.id != note.id }
                saveNotes(context, notes)
            },
            onMoveNote = { fromIndex, toIndex ->
                notes = notes.toMutableList().apply {
                    add(toIndex, removeAt(fromIndex))
                }
                saveNotes(context, notes)
            },
            modifier = Modifier.weight(1f)
        )
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newNoteText,
                onValueChange = { newNoteText = it },
                modifier = Modifier
                    .weight(2f)
                    .clip(RoundedCornerShape(60)),
                label = { Text("Enter your note") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            AddNoteButton(onClick = {
                if (newNoteText.isNotEmpty()) {
                    val newNote = Note(
                        id = System.currentTimeMillis(),
                        content = newNoteText
                    )
                    notes = notes + newNote
                    newNoteText = ""
                    saveNotes(context, notes)
                    soundEffects.playPopSound()
                }
            })
        }
    }
}

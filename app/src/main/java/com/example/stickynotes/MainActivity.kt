package com.example.stickynotes

import android.os.Bundle
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.roundToInt

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
fun soundEffects(): SoundEffectsState {
    val context = LocalContext.current
    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }

    val popSoundId = remember { soundPool.load(context, R.raw.pop_sound, 1) }

    fun playPopSound() {
        soundPool.play(popSoundId, 1f, 1f, 0, 0, 1f)
    }

    DisposableEffect(Unit) {
        onDispose {
            soundPool.release()
        }
    }
    return remember { SoundEffectsState(::playPopSound) }
}

data class SoundEffectsState(
    val playPopSound: () -> Unit
)

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
        modifier = Modifier.fillMaxSize()
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
                modifier = Modifier.weight(1f),
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

@Composable
fun NotesList(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onNoteDelete: (Note) -> Unit,
    onMoveNote: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var targetIndex by remember { mutableStateOf<Int?>(null) }

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        itemsIndexed(notes, key = { _, note -> note.id }) { index, note ->
            var isDragging by remember { mutableStateOf(false) }

            val animatedOffsetY by animateFloatAsState(
                targetValue = offsetY,
                animationSpec = tween(durationMillis = 150)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .draggable(
                        state = rememberDraggableState { delta ->
                            if (draggedIndex == index) {
                                offsetY += delta
                            }
                        },
                        orientation = Orientation.Vertical,
                        onDragStarted = {
                            draggedIndex = index
                            isDragging = true
                        },
                        onDragStopped = {
                            isDragging = false
                            targetIndex = (animatedOffsetY / with(density) { 64.dp.toPx() }).roundToInt().coerceIn(0, notes.size - 1)
                            if (draggedIndex != null && targetIndex != draggedIndex) {
                                onMoveNote(draggedIndex!!, targetIndex!!)
                            }
                            draggedIndex = null
                            offsetY = 0f
                        }
                    )
            ) {
                if (index == targetIndex) {
                    DropCursor()
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(0, if (draggedIndex == index) animatedOffsetY.roundToInt() else 0) }
                        .clickable { onNoteClick(note) }
                        .shadow(
                            elevation = if (isDragging) 8.dp else 2.dp,
                            spotColor = Color.Gray,
                            ambientColor = Color.Gray
                        ),
                ) {
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFFFCA47))
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = note.content, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onNoteDelete(note) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Note")
                        }
                    }
                }
            }

            if (index == notes.size) {
                DropCursor()
            }
        }
    }
}

@Composable
fun DropCursor() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(Color.Black)
            .padding(vertical = 8.dp)
    )
}

@Composable
fun AddNoteButton(onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .padding(3.dp)
            .width(60.dp)
            .height(60.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = "Add Note",
            modifier = Modifier.size(28.dp)
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

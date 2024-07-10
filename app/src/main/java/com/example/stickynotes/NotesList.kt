package com.example.stickynotes

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

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
                targetValue = if (draggedIndex == index) offsetY else 0f,
                animationSpec = tween(durationMillis = 100)
            )

            val spacing = if (draggedIndex != null && targetIndex != null) {
                if (index == targetIndex) {
                    with(density) { 20.dp.toPx() }
                } else {
                    with(density) { 8.dp.toPx() }
                }
            } else {
                with(density) { 8.dp.toPx() }
            }

            val elevation = if (isDragging) 8.dp else 2.dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = with(density) { spacing.toDp() })
                    .zIndex(if (isDragging) 1f else 0f)
                    .offset {
                        IntOffset(
                            0,
                            if (draggedIndex == index) animatedOffsetY.roundToInt() else 0
                        )
                    }
                    .draggable(
                        state = rememberDraggableState { delta ->
                            if (draggedIndex == index) {
                                offsetY += delta
                                targetIndex = (offsetY / with(density) { 20.dp.toPx() })
                                    .roundToInt()
                                    .coerceIn(0, notes.size - 1)
                            }
                        },
                        orientation = Orientation.Vertical,
                        onDragStarted = {
                            draggedIndex = index
                            isDragging = true
                        },
                        onDragStopped = {
                            isDragging = false
                            if (draggedIndex != null && targetIndex != draggedIndex) {
                                onMoveNote(draggedIndex!!, targetIndex!!)
                            }
                            draggedIndex = null
                            offsetY = 0f
                            targetIndex = null
                        }
                    )
            ) {
                if (index == targetIndex && draggedIndex != null) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .background(Color.White)
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { onNoteClick(note) }
                        .shadow(
                            elevation = elevation,
                            spotColor = Color.Gray,
                            ambientColor = Color.Gray
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                            .clip(RoundedCornerShape(10))
                            .background(Color.White)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(0.3.dp)
                                .clip(RoundedCornerShape(5))
                                .background(Color.Black)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = note.content,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(16.dp),
                                    color = Color.White
                                )
                                IconButton(onClick = { onNoteDelete(note) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Note",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

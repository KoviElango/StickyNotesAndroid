package com.example.stickynotes

data class Note(
    val id: Long,
    var content: String
)

data class SoundEffectsState(
    val playPopSound: () -> Unit
)


package com.example.stickynotes

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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

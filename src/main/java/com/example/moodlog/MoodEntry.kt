package com.example.moodlog

data class MoodEntry(
    val mood: String = "",
    val emoji: String = "",
    val notes: String = "",
    val date: String = "",
    val timestamp: Long = 0L
)

package com.example.moodlog

import android.graphics.Color
import java.util.*

//data class YearPixelData(
//    val date: String, // Format: "2025-07-09"
//    val mood: String = "",
//    val color: String = "#F3F4F6", // Default gray for no entry
//    val intensity: Int = 0
//)

object YearPixelsManager {

    fun getMoodColor(mood: String, intensity: Int): String {
        val baseColors = mapOf(
            "Happy" to "#10B981",    // Green
            "Sad" to "#3B82F6",      // Blue
            "Angry" to "#EF4444",    // Red
            "Calm" to "#8B5CF6",     // Purple
            "Neutral" to "#6B7280",  // Gray
            "Anxious" to "#F59E0B"   // Orange
        )

        val baseColor = baseColors[mood] ?: "#F3F4F6"
        return adjustColorIntensity(baseColor, intensity)
    }

    private fun adjustColorIntensity(hexColor: String, intensity: Int): String {
        val color = Color.parseColor(hexColor)
        val factor = (intensity / 10.0f).coerceIn(0.3f, 1.0f)

        val red = (Color.red(color) * factor).toInt()
        val green = (Color.green(color) * factor).toInt()
        val blue = (Color.blue(color) * factor).toInt()

        return String.format("#%02X%02X%02X", red, green, blue)
    }

    fun generateYearGrid(year: Int): List<YearPixelData> {
        val pixels = mutableListOf<YearPixelData>()
        val calendar = Calendar.getInstance()
        calendar.set(year, Calendar.JANUARY, 1)

        while (calendar.get(Calendar.YEAR) == year) {
            val dateString = String.format(
                "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            pixels.add(YearPixelData(dateString))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return pixels
    }
}

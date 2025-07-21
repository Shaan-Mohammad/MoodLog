package com.example.moodlog

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

object StreakManager {
    private const val PREFS = "moodlog_prefs"
    private const val KEY_LAST_DATE = "last_streak_date"
    private const val KEY_STREAK = "current_streak"
    private const val KEY_LONGEST = "longest_streak"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun updateStreak(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = dateFormat.format(Date())
        val lastDate = prefs.getString(KEY_LAST_DATE, null)
        var streak = prefs.getInt(KEY_STREAK, 0)
        var longest = prefs.getInt(KEY_LONGEST, 0)

        when {
            lastDate == null -> {
                streak = 1
            }
            lastDate == today -> {
                // Already updated today; do nothing
            }
            lastDate == getYesterday() -> {
                streak += 1
            }
            else -> {
                streak = 1
            }
        }
        if (streak > longest) longest = streak

        prefs.edit()
            .putString(KEY_LAST_DATE, today)
            .putInt(KEY_STREAK, streak)
            .putInt(KEY_LONGEST, longest)
            .apply()

        return streak
    }

    fun getLongest(context: Context): Int {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_LONGEST, 0)
    }

    private fun getYesterday(): String {
        val cal = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        return dateFormat.format(cal.time)
    }
}

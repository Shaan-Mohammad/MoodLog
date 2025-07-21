package com.example.moodlog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        NotificationHelper.showNotification(context)
        val prefs = context.getSharedPreferences("moodlog_prefs", Context.MODE_PRIVATE)
        val hour = prefs.getInt("reminder_hour", 20)
        val minute = prefs.getInt("reminder_minute", 0)
        NotificationHelper.scheduleDailyReminder(context, hour, minute)
    }
}

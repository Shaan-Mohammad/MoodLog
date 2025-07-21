package com.example.moodlog

import android.content.Context
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var btnMood: ImageButton
    private lateinit var btnHistory: ImageButton
    private lateinit var btnProfile: ImageButton
    private lateinit var btnAnalytics: ImageButton
    private lateinit var btnJournal: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MoodLog)
        setContentView(R.layout.activity_main)


        NotificationHelper.createNotificationChannel(this)
        val prefs = getSharedPreferences("moodlog_prefs", Context.MODE_PRIVATE)
        NotificationHelper.scheduleDailyReminder(
            this,
            prefs.getInt("reminder_hour", 20),
            prefs.getInt("reminder_minute", 0)
        )


        btnMood = findViewById(R.id.btnMood)
        btnHistory = findViewById(R.id.btnHistory)
        btnProfile = findViewById(R.id.btnProfile)
        btnAnalytics = findViewById(R.id.btnAnalytics)
        btnJournal = findViewById(R.id.btnJournal)


        btnMood.setOnClickListener      { switchFragment(MoodFragment(),      btnMood) }
        btnHistory.setOnClickListener   { switchFragment(HistoryFragment(),   btnHistory) }
        btnProfile.setOnClickListener   { switchFragment(ProfileFragment(),   btnProfile) }
        btnAnalytics.setOnClickListener { switchFragment(AnalyticsFragment(), btnAnalytics) }
        btnJournal.setOnClickListener   { switchFragment(JournalFragment(),   btnJournal) }


        if (savedInstanceState == null) {
            switchFragment(MoodFragment(), btnMood)
        }
    }

    private fun switchFragment(fragment: Fragment, selected: ImageButton) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        highlight(selected)
    }

    private fun highlight(selected: ImageButton) {

        listOf(btnMood, btnHistory, btnProfile, btnAnalytics, btnJournal).forEach {
            it.alpha = if (it == selected) 1.0f else 0.6f
        }
    }
}

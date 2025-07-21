package com.example.moodlog

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val auth = FirebaseAuth.getInstance()

        // Profile email
        val emailText = view.findViewById<TextView>(R.id.profileEmail)
        emailText.text = auth.currentUser?.email ?: "No email"

        // Dark mode toggle
        val darkSwitch = view.findViewById<Switch>(R.id.darkModeSwitch)
        darkSwitch.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        darkSwitch.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Reminder time
        val prefs = requireContext().getSharedPreferences("moodlog_prefs", Context.MODE_PRIVATE)
        var hour = prefs.getInt("reminder_hour", 20)
        var minute = prefs.getInt("reminder_minute", 0)
        val reminderTimeText = view.findViewById<TextView>(R.id.reminderTime)
        reminderTimeText.text = String.format("%02d:%02d", hour, minute)

        view.findViewById<Button>(R.id.setReminderBtn).setOnClickListener {
            TimePickerDialog(requireContext(), { _, h, m ->
                hour = h
                minute = m
                reminderTimeText.text = String.format("%02d:%02d", hour, minute)
                prefs.edit()
                    .putInt("reminder_hour", hour)
                    .putInt("reminder_minute", minute)
                    .apply()
                NotificationHelper.scheduleDailyReminder(requireContext(), hour, minute)
                Toast.makeText(
                    requireContext(),
                    "Reminder set for ${String.format("%02d:%02d", hour, minute)}",
                    Toast.LENGTH_SHORT
                ).show()
            }, hour, minute, true).show()
        }

        // Year in Pixels button
        view.findViewById<Button>(R.id.yearPixelsBtn).setOnClickListener {
            startActivity(Intent(requireContext(), YearPixelsActivity::class.java))
        }

        // Streaks
        val streakCount = StreakManager.updateStreak(requireContext())
        view.findViewById<TextView>(R.id.streakText).text = "🔥 Streak: $streakCount days"

        // Longest streak
        val longest = StreakManager.getLongest(requireContext())
        view.findViewById<TextView>(R.id.longestText).text = "🏆 Longest: $longest days"

        // Sign out
        view.findViewById<Button>(R.id.signOutBtn).setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        return view
    }
}

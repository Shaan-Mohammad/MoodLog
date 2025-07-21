package com.example.moodlog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class YearPixelsActivity : AppCompatActivity() {

    private lateinit var yearPixelsView: YearPixelsView
    private lateinit var yearText: TextView
    private lateinit var backButton: Button
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private val pixelDataMap = mutableMapOf<String, YearPixelData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_year_pixels)

        setupViews()
        setupClickListeners()
        loadYearData()
    }

    private fun setupViews() {
        yearPixelsView = findViewById(R.id.yearPixelsView)
        yearText = findViewById(R.id.yearText)
        backButton = findViewById(R.id.backButton)

        yearText.text = currentYear.toString()

        yearPixelsView.setOnPixelClickListener { pixel ->
            if (pixel.mood.isNotEmpty()) {
                Toast.makeText(this, "${pixel.date}: ${pixel.mood}", Toast.LENGTH_SHORT).show()
            } else {
                // Navigate to mood entry for this date
                val intent = Intent(this, MoodDetailActivity::class.java)
                intent.putExtra("selected_date", pixel.date)
                startActivity(intent)
            }
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener { finish() }

        findViewById<Button>(R.id.prevYearBtn).setOnClickListener {
            currentYear--
            yearText.text = currentYear.toString()
            loadYearData()
        }

        findViewById<Button>(R.id.nextYearBtn).setOnClickListener {
            if (currentYear < Calendar.getInstance().get(Calendar.YEAR)) {
                currentYear++
                yearText.text = currentYear.toString()
                loadYearData()
            }
        }
    }

    private fun loadYearData() {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        // Generate empty year grid
        val yearGrid = YearPixelsManager.generateYearGrid(currentYear)
        pixelDataMap.clear()
        yearGrid.forEach { pixelDataMap[it.date] = it }

        // Load mood data from Firestore
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("moods")
            .whereGreaterThanOrEqualTo("timestamp", getYearStartTimestamp(currentYear))
            .whereLessThan("timestamp", getYearStartTimestamp(currentYear + 1))
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val mood = document.getString("label") ?: ""
                    val intensity = document.getLong("intensity")?.toInt() ?: 5
                    val timestamp = document.getLong("timestamp") ?: 0

                    val date = timestampToDateString(timestamp)
                    pixelDataMap[date]?.let { pixel ->
                        val updatedPixel = pixel.copy(
                            mood = mood,
                            //intensity = intensity,
                            color = YearPixelsManager.getMoodColor(mood, intensity)
                        )
                        pixelDataMap[date] = updatedPixel
                    }
                }

                yearPixelsView.setPixelData(pixelDataMap.values.toList())
            }
    }

    private fun getYearStartTimestamp(year: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun timestampToDateString(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return String.format(
            "%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }
}

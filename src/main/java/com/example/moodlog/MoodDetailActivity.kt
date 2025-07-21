package com.example.moodlog

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.text.SimpleDateFormat
import java.util.*

// Data class for ZenQuotes API
data class ZenQuote(
    val text: String,
    val author: String
)

// Retrofit interface for ZenQuotes API
interface ZenQuoteService {
    @GET("api/random")
    fun getRandomQuote(): Call<List<ZenQuote>>
}

class MoodDetailActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var zenQuoteService: ZenQuoteService

    private lateinit var selectedEmojiImage: ImageView
    private lateinit var moodLabel: TextView
    private lateinit var dateLabel: TextView
    private lateinit var intensitySlider: Slider
    private lateinit var intensityValue: TextView
    private lateinit var quoteText: TextView
    private lateinit var quoteAuthor: TextView
    private lateinit var notesEditText: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var backButton: MaterialButton

    private var selectedMood: String = ""
    private var selectedMoodEmoji: String = ""
    private var selectedEmojiDrawable: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_detail)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize ZenQuotes API
        val retrofit = Retrofit.Builder()
            .baseUrl("https://zenquotes.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        zenQuoteService = retrofit.create(ZenQuoteService::class.java)

        // Initialize views
        selectedEmojiImage = findViewById(R.id.selectedEmojiImage)
        moodLabel = findViewById(R.id.moodLabel)
        dateLabel = findViewById(R.id.dateLabel)
        intensitySlider = findViewById(R.id.intensitySlider)
        intensityValue = findViewById(R.id.intensityValue)
        quoteText = findViewById(R.id.quoteText)
        quoteAuthor = findViewById(R.id.quoteAuthor)
        notesEditText = findViewById(R.id.notesEditText)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)

        // Get selected mood from intent
        selectedMood = intent.getStringExtra("mood") ?: "Happy"
        selectedMoodEmoji = intent.getStringExtra("emoji") ?: "😊"
        selectedEmojiDrawable = intent.getIntExtra("emoji_drawable", R.drawable.emoji_happy)

        setupMoodDisplay()
        setupIntensitySlider()
        fetchMotivationalQuote()
        setupButtons()
    }

    private fun setupMoodDisplay() {
        selectedEmojiImage.setImageResource(selectedEmojiDrawable)
        moodLabel.text = selectedMood
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        dateLabel.text = "Today, ${dateFormat.format(Date())}"
    }

    private fun setupIntensitySlider() {
        intensitySlider.addOnChangeListener { _, value, _ ->
            intensityValue.text = "${value.toInt()}/10"
        }
        intensityValue.text = "${intensitySlider.value.toInt()}/10"
    }

    private fun fetchMotivationalQuote() {
        quoteText.text = "Loading inspirational quote..."
        quoteAuthor.text = ""

        zenQuoteService.getRandomQuote().enqueue(object : Callback<List<ZenQuote>> {
            override fun onResponse(call: Call<List<ZenQuote>>, response: Response<List<ZenQuote>>) {
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val quote = response.body()!![0]
                    quoteText.text = "\"${quote.text}\""
                    quoteAuthor.text = "— ${quote.author}"
                } else {
                    showLocalFallbackQuote()
                }
            }
            override fun onFailure(call: Call<List<ZenQuote>>, t: Throwable) {
                showLocalFallbackQuote()
            }
        })
    }

    private fun showLocalFallbackQuote() {
        val fallbackQuotes = listOf(
            Pair("Every day is a new beginning. Take a deep breath and start again.", "Anonymous"),
            Pair("The only way to do great work is to love what you do.", "Steve Jobs"),
            Pair("Happiness is not something ready made. It comes from your own actions.", "Dalai Lama"),
            Pair("You are braver than you believe, stronger than you seem, and smarter than you think.", "A.A. Milne"),
            Pair("Peace comes from within. Do not seek it without.", "Buddha"),
            Pair("It's okay to feel sad. Let yourself feel it, then let it go.", "Anonymous"),
            Pair("Anger is an acid that can do more harm to the vessel in which it is stored than to anything on which it is poured.", "Mark Twain"),
            Pair("Calmness is the cradle of power.", "Josiah Gilbert Holland")
        )
        val (text, author) = fallbackQuotes.random()
        quoteText.text = "\"$text\""
        quoteAuthor.text = "— $author"
    }

    private fun setupButtons() {
        backButton.setOnClickListener { finish() }
        saveButton.setOnClickListener { saveMoodEntry() }
    }

    private fun saveMoodEntry() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }
        val notes = notesEditText.text.toString()
        val intensity = intensitySlider.value.toInt()
        val timestamp = System.currentTimeMillis()
        saveButton.isEnabled = false
        saveButton.text = "Saving..."

        val moodEntry = hashMapOf(
            "mood" to selectedMood,
            "emoji" to selectedMoodEmoji,
            "notes" to notes,
            "intensity" to intensity,
            "timestamp" to timestamp,
            "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )

        db.collection("users")
            .document(currentUser.uid)
            .collection("moods")
            .add(moodEntry)
            .addOnSuccessListener {
                Toast.makeText(this, "Mood saved successfully! 🎉", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                saveButton.isEnabled = true
                saveButton.text = "Save Mood Entry"
                Toast.makeText(this, "Error saving mood: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}

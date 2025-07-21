package com.example.moodlog

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class MoodFragment : Fragment() {

    private lateinit var moodAdapter: CustomMoodAdapter
    private val moodList = mutableListOf<CustomMood>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood, container, false)
        setupHeader(view)
        setupMoodGrid(view)
        setupCustomMoodButton(view)
        loadMoods()
        return view
    }

    private fun setupHeader(view: View) {
        val greetingText = view.findViewById<TextView>(R.id.greetingText)
        val dateText = view.findViewById<TextView>(R.id.dateText)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }
        greetingText.text = greeting
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        dateText.text = "Today, ${dateFormat.format(Date())}"
    }

    private fun setupMoodGrid(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.moodRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        moodAdapter = CustomMoodAdapter(moodList) { mood ->
            val intent = Intent(requireContext(), MoodDetailActivity::class.java)
            intent.putExtra("mood", mood.label)
            intent.putExtra("emoji", mood.emoji)
            intent.putExtra("mood_id", mood.id)
            startActivity(intent)
        }
        recyclerView.adapter = moodAdapter
    }

    private fun setupCustomMoodButton(view: View) {
        view.findViewById<Button>(R.id.addCustomMoodBtn).setOnClickListener {
            showAddCustomMoodDialog()
        }
    }

    private fun showAddCustomMoodDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_mood, null)
        val etLabel = dialogView.findViewById<EditText>(R.id.etMoodLabel)
        val etEmoji = dialogView.findViewById<EditText>(R.id.etMoodEmoji)
        val palette = dialogView.findViewById<LinearLayout>(R.id.colorPalette)

        val colors = listOf(
            "#FEF3C7", "#DBEAFE", "#FED7D7", "#D1FAE5",
            "#F3F4F6", "#FFEDD5", "#FDE68A", "#A7F3D0", "#FBBF24"
        )
        var selectedColor = colors[0]

        palette.removeAllViews()
        colors.forEach { hex ->
            val circle = View(requireContext()).apply {
                setBackgroundResource(R.drawable.color_circle_bg)
                background.setTint(Color.parseColor(hex))
                layoutParams = LinearLayout.LayoutParams(64, 64).apply {
                    setMargins(8, 0, 8, 0)
                }
                setOnClickListener {
                    selectedColor = hex
                    // Use classic loop instead of children extension
                    for (i in 0 until palette.childCount) {
                        palette.getChildAt(i).alpha = 0.5f
                    }
                    this.alpha = 1f
                }
                alpha = if (hex == selectedColor) 1f else 0.5f
            }
            palette.addView(circle)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Custom Mood")
            .setView(dialogView)
            .setPositiveButton("Add") { dlg, _ ->
                val label = etLabel.text.toString().trim()
                val emoji = etEmoji.text.toString().trim()
                val color = selectedColor

                if (label.isEmpty() || emoji.isEmpty()) {
                    Toast.makeText(requireContext(), "Enter label and emoji", Toast.LENGTH_SHORT).show()
                } else {
                    val user = FirebaseAuth.getInstance().currentUser ?: return@setPositiveButton
                    val db = Firebase.firestore
                    val docRef = db.collection("users")
                        .document(user.uid)
                        .collection("customMoods")
                        .document()
                    val customMood = CustomMood(
                        id = docRef.id,
                        label = label,
                        emoji = emoji,
                        color = color
                    )
                    docRef.set(customMood)
                        .addOnSuccessListener {
                            moodList.add(customMood)
                            moodAdapter.notifyItemInserted(moodList.size - 1)
                            Toast.makeText(requireContext(), "Custom mood added", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                dlg.dismiss()
            }
            .setNegativeButton("Cancel") { dlg, _ -> dlg.dismiss() }
            .show()
    }

    private fun loadMoods() {
        // Default moods
        val defaultMoods = listOf(
            CustomMood("happy", "Happy", "😊", "#FEF3C7"),
            CustomMood("sad", "Sad", "😢", "#DBEAFE"),
            CustomMood("angry", "Angry", "😡", "#FED7D7"),
            CustomMood("calm", "Calm", "😌", "#D1FAE5"),
            CustomMood("neutral", "Neutral", "😐", "#F3F4F6"),
            CustomMood("anxious", "Anxious", "😰", "#FFEDD5"),
//            CustomMood("excited", "Excited", "🤩", "#FDE68A"),
//            CustomMood("tired", "Tired", "🥱", "#D1D5DB"),
//            CustomMood("bored", "Bored", "😑", "#F3F4F6"),
//            CustomMood("grateful", "Grateful", "🙏", "#A7F3D0"),
//            CustomMood("motivated", "Motivated", "💪", "#FBBF24"),
//            CustomMood("overwhelmed", "Overwhelmed", "😵", "#FCA5A5"),
//            CustomMood("content", "Content", "🙂", "#BBF7D0"),
//            CustomMood("lonely", "Lonely", "😔", "#C7D2FE"),
//            CustomMood("proud", "Proud", "😌", "#FDE68A")
         )

        moodList.clear()
        moodList.addAll(defaultMoods)
        moodAdapter.notifyDataSetChanged()
        loadCustomMoods()
    }

    private fun loadCustomMoods() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("customMoods")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val customMood = document.toObject(CustomMood::class.java)
                    if (!moodList.any { it.id == customMood.id }) {
                        moodList.add(customMood)
                    }
                }
                moodAdapter.notifyDataSetChanged()
            }
    }
}

package com.example.moodlog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class CustomMood(
    val id: String = "",
    val label: String = "",
    val emoji: String = "",
    val color: String = "#F3F4F6"
)

class CustomMoodAdapter(
    private val moods: List<CustomMood>,
    private val onMoodClick: (CustomMood) -> Unit
) : RecyclerView.Adapter<CustomMoodAdapter.MoodViewHolder>() {

    inner class MoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emojiText: TextView = view.findViewById(R.id.emojiText)
        val labelText: TextView = view.findViewById(R.id.labelText)
        val container: View = view.findViewById(R.id.moodContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_selection, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moods[position]
        holder.emojiText.text = mood.emoji
        holder.labelText.text = mood.label

        holder.container.setOnClickListener {
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                    onMoodClick(mood)
                }
                .start()
        }
    }

    override fun getItemCount() = moods.size
}

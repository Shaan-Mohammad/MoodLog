package com.example.moodlog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MoodHistoryAdapter(private val moodList: List<MoodEntry>) :
    RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder>() {

    class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emoji: TextView = itemView.findViewById(R.id.itemEmoji)
        val mood: TextView = itemView.findViewById(R.id.itemMood)
        val date: TextView = itemView.findViewById(R.id.itemDate)
        val note: TextView = itemView.findViewById(R.id.itemNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_entry, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val entry = moodList[position]
        holder.emoji.text = entry.emoji
        holder.mood.text = entry.mood
        holder.date.text = entry.date
        holder.note.text = entry.notes
    }

    override fun getItemCount(): Int = moodList.size
}

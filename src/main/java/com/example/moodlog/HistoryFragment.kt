package com.example.moodlog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MoodHistoryAdapter
    private val moodList = mutableListOf<MoodEntry>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        recyclerView = view.findViewById(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MoodHistoryAdapter(moodList)
        recyclerView.adapter = adapter

        loadMoodHistory()
        return view
    }

    private fun loadMoodHistory() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .collection("moods")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener

                    moodList.clear()
                    snapshot?.documents?.forEach { document ->
                        document.toObject(MoodEntry::class.java)?.let {
                            moodList.add(it)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
        }
    }
}

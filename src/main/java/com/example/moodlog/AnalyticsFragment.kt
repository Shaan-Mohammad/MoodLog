package com.example.moodlog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsFragment : Fragment() {

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analytics, container, false)
        setupBarChart(view.findViewById(R.id.barChart))
        setupPieChart(view.findViewById(R.id.pieChart))
        return view
    }

    private fun setupBarChart(barChart: BarChart) {
        val uid = auth.currentUser?.uid ?: return
        val past = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -13) }
        db.collection("users").document(uid)
            .collection("moods")
            .whereGreaterThanOrEqualTo("timestamp", past.timeInMillis)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get().addOnSuccessListener { docs ->
                val byDay = mutableMapOf<String, MutableList<Int>>()
                val fmt = SimpleDateFormat("MM-dd", Locale.getDefault())
                docs.forEach { doc ->
                    doc.getLong("timestamp")?.let { ts ->
                        val day = fmt.format(Date(ts))
                        val intensity = doc.getLong("intensity")?.toInt() ?: 5
                        byDay.getOrPut(day) { mutableListOf() }.add(intensity)
                    }
                }
                val entries = ArrayList<BarEntry>()
                val labels = ArrayList<String>()
                byDay.entries.forEachIndexed { i, entry ->
                    entries.add(BarEntry(i.toFloat(), entry.value.average().toFloat()))
                    labels.add(entry.key)
                }
                barChart.data = BarData(
                    BarDataSet(entries, "Avg Intensity").apply {
                        colors = ColorTemplate.MATERIAL_COLORS.toList()
                        valueTextColor = Color.BLACK
                        valueTextSize = 12f
                    }
                ).apply { barWidth = 0.5f }
                barChart.xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(labels)
                    granularity = 1f
                    isGranularityEnabled = true
                }
                barChart.axisLeft.apply {
                    axisMinimum = 0f
                    axisMaximum = 10f
                }
                barChart.axisRight.isEnabled = false
                barChart.description.isEnabled = false
                barChart.setExtraOffsets(8f, 8f, 8f, 8f)
                barChart.animateY(1000)
                barChart.legend.isEnabled = false
                barChart.invalidate()
            }
    }

    private fun setupPieChart(pieChart: PieChart) {
        val uid = auth.currentUser?.uid ?: return
        val past = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -29) }
        db.collection("users").document(uid)
            .collection("moods")
            .whereGreaterThanOrEqualTo("timestamp", past.timeInMillis)
            .get().addOnSuccessListener { docs ->
                val count = mutableMapOf<String, Int>()
                docs.forEach { doc ->
                    val label = doc.getString("label") ?: "Unknown"
                    count[label] = count.getOrDefault(label, 0) + 1
                }
                val entries = count.map { PieEntry(it.value.toFloat(), it.key) }
                pieChart.data = PieData(
                    PieDataSet(entries, "").apply {
                        colors = ColorTemplate.COLORFUL_COLORS.toList()
                        valueTextColor = Color.BLACK
                        valueTextSize = 12f
                    }
                )
                pieChart.apply {
                    setUsePercentValues(true)
                    centerText = "Distribution"
                    setCenterTextSize(16f)
                    holeRadius = 50f
                    var transparentCircleAlpha = 110
                    animateY(1000)
                    legend.isEnabled = false
                    description.isEnabled = false
                }
                pieChart.invalidate()
            }
    }
}

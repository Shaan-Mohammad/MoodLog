package com.example.moodlog

import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class JournalFragment : Fragment() {

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val today get() = dateFmt.format(Date())
    private val prompts = listOf(
        "What are you grateful for today?",
        "Describe a moment that made you smile.",
        "What challenge did you overcome?",
        "How did you care for yourself today?"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_journal, container, false)

        val promptText     = view.findViewById<TextView>(R.id.promptText)
        val streakText     = view.findViewById<TextView>(R.id.journalStreakText)
        val notesEdit      = view.findViewById<TextInputEditText>(R.id.notesEditText)
        val wordCountText  = view.findViewById<TextView>(R.id.wordCountText)
        val btnSave        = view.findViewById<Button>(R.id.btnSaveJournal)
        val btnExport      = view.findViewById<Button>(R.id.btnExportJournal)
        val btnResources   = view.findViewById<TextView>(R.id.tvResourcesLink)
        val btnBack        = view.findViewById<MaterialButton>(R.id.backButton)

        // Prompt
        promptText.text = prompts.random()
        promptText.setOnClickListener { promptText.text = prompts.random() }

        // Streak
        val streak = StreakManager.updateStreak(requireContext())
        streakText.text = "🖋️ Journal Streak: $streak days"

        // Load existing entry
        db.collection("users").document(auth.currentUser!!.uid)
            .collection("journal").document(today)
            .get().addOnSuccessListener { doc ->
                if (doc.exists()) notesEdit.setText(doc.getString("content"))
            }

        // Word count
        notesEdit.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val count = s?.trim()?.split("\\s+".toRegex())?.size ?: 0
                wordCountText.text = "$count words"
            }
            override fun beforeTextChanged(a: CharSequence?, b: Int, c: Int, d: Int) {}
            override fun onTextChanged(a: CharSequence?, b: Int, c: Int, d: Int) {}
        })

        // Save
        btnSave.setOnClickListener {
            val text = notesEdit.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), "Cannot save empty entry", Toast.LENGTH_SHORT).show()
            } else {
                db.collection("users").document(auth.currentUser!!.uid)
                    .collection("journal").document(today)
                    .set(mapOf("content" to text, "timestamp" to Date()))
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Journal saved", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Export PDF
        btnExport.setOnClickListener {
            val pdf = PdfDocument()
            val info = PdfDocument.PageInfo.Builder(595,842,1).create()
            val page = pdf.startPage(info)
            val canvas = page.canvas
            val paint = Paint().apply { textSize = 14f }
            canvas.drawText(today, 20f, 40f, paint)
            var y = 80f
            notesEdit.text.toString().lines().forEach {
                canvas.drawText(it,20f,y,paint)
                y += paint.textSize + 4
            }
            pdf.finishPage(page)
            val file = File(requireContext().cacheDir, "journal_$today.pdf")
            pdf.writeTo(FileOutputStream(file)); pdf.close()
            val uri = FileProvider.getUriForFile(
                requireContext(), "${requireContext().packageName}.provider", file
            )
            startActivity(Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri)
            })
        }


        btnResources.setOnClickListener {
            val uri = Uri.parse("https://www.mentalhealth.gov/get-help")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }


        btnBack.setOnClickListener { activity?.onBackPressed() }

        return view
    }
}

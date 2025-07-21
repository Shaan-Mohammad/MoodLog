package com.example.moodlog

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MoodLog)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val emailEdit  = findViewById<TextInputEditText>(R.id.emailEdit)
        val passEdit   = findViewById<TextInputEditText>(R.id.passwordEdit)
        val rememberCb = findViewById<CheckBox>(R.id.rememberMeCheck)
        val loginBtn   = findViewById<MaterialButton>(R.id.loginBtn)
        val regBtn     = findViewById<MaterialButton>(R.id.registerBtn)
        val forgotTxt  = findViewById<TextView>(R.id.forgotPassword)
        val progress   = findViewById<CircularProgressIndicator>(R.id.progressIndicator)


        passEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) { loginBtn.performClick(); true } else false
        }

        fun toggleUi(enable: Boolean) {
            loginBtn.isEnabled = enable
            regBtn.isEnabled   = enable
            if (enable) progress.hide() else progress.show()
        }

        loginBtn.setOnClickListener {
            val email = emailEdit.text?.toString()?.trim() ?: ""
            val pass  = passEdit.text?.toString()?.trim() ?: ""
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            toggleUi(false)
            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    toggleUi(true)
                    if (task.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java)); finish()
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        regBtn.setOnClickListener {
            val email = emailEdit.text?.toString()?.trim() ?: ""
            val pass  = passEdit.text?.toString()?.trim() ?: ""
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            if (pass.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            toggleUi(false)
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    toggleUi(true)
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Account created! Please sign in.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        forgotTxt.setOnClickListener {
            val email = emailEdit.text?.toString()?.trim() ?: ""
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email first.", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            toggleUi(false)
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    toggleUi(true)
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Failed: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}

package com.dailer.app

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.security.MessageDigest

class PinSetupActivity : AppCompatActivity() {

    private lateinit var pinInput: TextInputEditText
    private lateinit var confirmInput: TextInputEditText
    private lateinit var saveBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_setup)

        pinInput = findViewById(R.id.pin_input)
        confirmInput = findViewById(R.id.pin_confirm_input)
        saveBtn = findViewById(R.id.pin_save_btn)

        saveBtn.setOnClickListener {
            val pin = pinInput.text?.toString() ?: ""
            val confirm = confirmInput.text?.toString() ?: ""
            if (pin.length < 4) {
                Toast.makeText(this, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pin != confirm) {
                Toast.makeText(this, "PINs do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hash = sha256(pin)
            val prefs = getSharedPreferences("vault_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("vault_pin_hash", hash).putInt("vault_pin_length", pin.length).apply()

            Toast.makeText(this, "PIN saved", Toast.LENGTH_SHORT).show()
            finish()
        }

        // small UX: disable save until inputs filled
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                saveBtn.isEnabled = (pinInput.text?.length ?: 0) >= 4 && (confirmInput.text?.length ?: 0) >= 4
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        pinInput.addTextChangedListener(watcher)
        confirmInput.addTextChangedListener(watcher)
    }

    private fun sha256(s: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(s.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

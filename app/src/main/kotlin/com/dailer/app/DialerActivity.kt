package com.dailer.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.security.MessageDigest

class DialerActivity : AppCompatActivity() {

    private lateinit var display: TextView
    private val input = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialer)

        display = findViewById(R.id.dial_display)

        // numeric buttons 0-9 and * #
        val buttons = listOf(
            R.id.b1 to "1", R.id.b2 to "2", R.id.b3 to "3",
            R.id.b4 to "4", R.id.b5 to "5", R.id.b6 to "6",
            R.id.b7 to "7", R.id.b8 to "8", R.id.b9 to "9",
            R.id.b_star to "*", R.id.b0 to "0", R.id.b_hash to "#"
        )

        for ((id, label) in buttons) {
            findViewById<Button>(id).setOnClickListener {
                appendDigit(label)
            }
        }

        findViewById<ImageButton>(R.id.b_back).setOnClickListener {
            if (input.isNotEmpty()) {
                input.deleteCharAt(input.length - 1)
                updateDisplay()
            }
        }

        findViewById<Button>(R.id.b_call).setOnClickListener {
            checkForPinAndAct()
        }

        // Also check PIN automatically whenever the input length matches stored PIN length
        // (optional convenience)
    }

    private fun appendDigit(d: String) {
        input.append(d)
        updateDisplay()
        // optionally auto-check if matches PIN length
        val storedHash = getStoredPinHash()
        if (!storedHash.isNullOrEmpty()) {
            val pin = getStoredPinPlainLength()
            if (pin > 0 && input.length == pin) {
                checkForPinAndAct()
            }
        }
    }

    private fun updateDisplay() {
        // show masked input
        display.text = input.toString().replace(Regex("."), "•")
    }

    private fun checkForPinAndAct() {
        val prefs = getSharedPreferences("vault_prefs", Context.MODE_PRIVATE)
        val stored = prefs.getString("vault_pin_hash", null)
        if (stored.isNullOrEmpty()) {
            // no pin set -> open setup
            Toast.makeText(this, "No PIN set. Please create a PIN.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, PinSetupActivity::class.java))
            return
        }

        val candidate = input.toString()
        if (candidate.isEmpty()) {
            Toast.makeText(this, "Enter PIN", Toast.LENGTH_SHORT).show()
            return
        }
        val candidateHash = sha256(candidate)
        if (candidateHash.equals(stored, ignoreCase = true)) {
            // Open vault
            startActivity(Intent(this, VaultActivity::class.java))
            input.clear()
            updateDisplay()
        } else {
            Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
            // optionally clear input
            input.clear()
            updateDisplay()
        }
    }

    private fun sha256(s: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(s.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun getStoredPinHash(): String? {
        val prefs = getSharedPreferences("vault_prefs", Context.MODE_PRIVATE)
        return prefs.getString("vault_pin_hash", null)
    }

    private fun getStoredPinPlainLength(): Int {
        // there is no stored plain length; we approximate length by reading a special pref if we stored it
        val prefs = getSharedPreferences("vault_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("vault_pin_length", 0)
    }
}

package com.jarvis.assistant.ui.settings

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PersonaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "JARVIC Persona & Voice Studio (Pari / Jarvis / Kabir / TechGPT X)"
            textSize = 18f
            setPadding(48, 48, 48, 48)
        })
    }
}

class VoiceCommandsGuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "JARVIC Mobile Voice Control Guide (A to Z)"
            textSize = 18f
            setPadding(48, 48, 48, 48)
        })
    }

    companion object {
        fun showMobileControlPopup(context: Context) {
            MaterialAlertDialogBuilder(context)
                .setTitle("JARVIC Voice Control Guide")
                .setMessage(
                    "• Say 'Open WhatsApp / YouTube / Facebook'\n" +
                        "• Say 'Search latest news on Google'\n" +
                        "• Tap or hold the center orb to toggle live voice conversation."
                )
                .setPositiveButton("Got it", null)
                .show()
        }
    }
}

class ApiConnectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "JARVIC API & Cloud Connection Settings"
            textSize = 18f
            setPadding(48, 48, 48, 48)
        })
    }
}

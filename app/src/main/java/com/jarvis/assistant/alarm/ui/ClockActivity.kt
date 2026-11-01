package com.jarvis.assistant.alarm.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ClockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "JARVIC Voice Alarms & Clock"
            textSize = 18f
            setPadding(48, 48, 48, 48)
        })
    }
}

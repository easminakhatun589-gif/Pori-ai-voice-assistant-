package com.jarvis.assistant.builder

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class VoiceBuilderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "JARVIC Voice Builder & Code Studio"
            textSize = 18f
            setPadding(48, 48, 48, 48)
        })
    }
}

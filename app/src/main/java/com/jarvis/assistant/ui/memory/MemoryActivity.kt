package com.jarvis.assistant.ui.memory

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MemoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "JARVIC Long-Term Memory Core"
            textSize = 18f
            setPadding(48, 48, 48, 48)
        })
    }
}

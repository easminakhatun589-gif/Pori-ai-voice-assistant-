package com.jarvis.assistant.ui.agent

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AgentDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "JARVIC Master Autonomous Agent & Multi-Task Dashboard"
            textSize = 18f
            setPadding(48, 48, 48, 48)
        })
    }
}

package com.jarvis.assistant.ui.permission

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PermissionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "JARVIC Permissions & Security Setup"
            textSize = 18f
            setPadding(48, 48, 48, 48)
        })
    }
}

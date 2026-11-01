package com.jarvis.assistant.trading.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TradingAnalyzerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "JARVIC Market Chart & S&R Scanner"
            textSize = 18f
            setPadding(48, 48, 48, 48)
        })
    }
}

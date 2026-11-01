package com.jarvis.assistant.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jarvis.assistant.JarvisApp
import com.jarvis.assistant.R
import java.util.Locale

object HapticFeedbackHelper {
    fun performControlClick(view: View?) {
        try {
            view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        } catch (_: Exception) {}
    }

    fun performCorePulse(view: View?) {
        try {
            view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        } catch (_: Exception) {}
    }
}

object HapticFeedbackManager {
    fun triggerOrbPress(view: View?) {
        HapticFeedbackHelper.performControlClick(view)
    }

    fun triggerPowerToggle(isPowerOn: Boolean, view: View?) {
        HapticFeedbackHelper.performCorePulse(view)
    }

    fun triggerSubtleTick(view: View?) {
        try {
            view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        } catch (_: Exception) {}
    }
}

object PermissionHelper {
    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun openAccessibilitySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}

object LocationWeatherHelper {

    data class CountryIdentity(
        val countryName: String,
        val countryCode: String
    )

    data class WeatherData(
        val temperature: String,
        val condition: String,
        val cityName: String,
        val countryName: String,
        val countryCode: String,
        val details: String,
        val iconResId: Int
    )

    suspend fun fetchDeviceCountryIdentity(context: Context): CountryIdentity? {
        val locale = Locale.getDefault()
        val code = locale.country.ifBlank { "BD" }
        val name = locale.displayCountry.ifBlank { "Bangladesh" }
        return CountryIdentity(countryName = name, countryCode = code)
    }

    suspend fun fetchWeather(context: Context): WeatherData? {
        if (!PermissionHelper.hasLocationPermission(context)) {
            return null
        }
        val prefs = try {
            (context.applicationContext as? JarvisApp)?.preferences ?: JarvisApp.instance.preferences
        } catch (_: Exception) {
            null
        }
        val city = prefs?.userLocation?.takeIf { it.isNotBlank() } ?: "Dhaka"
        val country = prefs?.userCountryName?.takeIf { it.isNotBlank() } ?: "Bangladesh"
        val code = prefs?.userCountryCode?.takeIf { it.isNotBlank() } ?: "BD"
        return WeatherData(
            temperature = "28°C",
            condition = "Clear Sky",
            cityName = city,
            countryName = country,
            countryCode = code,
            details = "Humidity 62% • Wind 11 km/h",
            iconResId = R.drawable.ic_angel_weather_sun
        )
    }
}

object CountryFlagHelper {
    fun normalizeCountry(name: String, code: String): Pair<String, String> {
        val cleanName = name.trim()
        val cleanCode = code.trim().uppercase(Locale.ROOT)
        if (cleanName.isBlank() && cleanCode.isBlank()) {
            return "" to ""
        }
        val resolvedName = cleanName.ifBlank {
            when (cleanCode) {
                "BD" -> "Bangladesh"
                "IN" -> "India"
                "US" -> "United States"
                else -> cleanCode
            }
        }
        val resolvedCode = cleanCode.ifBlank {
            when (cleanName.lowercase(Locale.ROOT)) {
                "bangladesh" -> "BD"
                "india" -> "IN"
                else -> cleanName.take(2).uppercase(Locale.ROOT)
            }
        }
        return resolvedName to resolvedCode
    }

    fun createCountryFlagDrawable(
        context: Context,
        countryCodeOrName: String,
        widthDp: Int,
        heightDp: Int
    ): Drawable {
        val density = context.resources.displayMetrics.density
        val w = (widthDp * density).toInt().coerceAtLeast(16)
        val h = (heightDp * density).toInt().coerceAtLeast(12)
        val code = countryCodeOrName.trim().uppercase(Locale.ROOT)

        return object : Drawable() {
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            override fun getIntrinsicWidth(): Int = w
            override fun getIntrinsicHeight(): Int = h

            override fun draw(canvas: Canvas) {
                val b = bounds
                val rect = RectF(b.left.toFloat(), b.top.toFloat(), b.right.toFloat(), b.bottom.toFloat())
                if (code == "BD" || code.contains("BANGLA")) {
                    paint.color = Color.parseColor("#006A4E")
                    canvas.drawRoundRect(rect, 4f, 4f, paint)
                    paint.color = Color.parseColor("#F42A41")
                    canvas.drawCircle(rect.centerX() - rect.width() * 0.05f, rect.centerY(), rect.height() * 0.3f, paint)
                } else {
                    paint.color = Color.parseColor("#2563EB")
                    canvas.drawRoundRect(rect, 4f, 4f, paint)
                }
            }

            override fun setAlpha(alpha: Int) {
                paint.alpha = alpha
            }

            override fun setColorFilter(colorFilter: ColorFilter?) {
                paint.colorFilter = colorFilter
            }

            @Deprecated("Deprecated in Java")
            override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
        }
    }
}

object UserProfileDialogHelper {
    fun showProfileDialog(
        context: Context,
        onRequestLocationPermission: () -> Unit,
        onRefreshLocation: () -> Unit,
        onOpenFullSettings: () -> Unit,
        onProfileUpdated: () -> Unit
    ) {
        val prefs = try {
            (context.applicationContext as? JarvisApp)?.preferences ?: JarvisApp.instance.preferences
        } catch (_: Exception) {
            null
        }
        val input = EditText(context).apply {
            hint = "Your Name"
            setText(prefs?.userName.orEmpty())
        }
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
            addView(input)
        }
        MaterialAlertDialogBuilder(context)
            .setTitle("User Profile & Location")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                prefs?.userName = input.text?.toString()?.trim().orEmpty()
                onProfileUpdated()
            }
            .setNeutralButton("Settings") { _, _ ->
                onOpenFullSettings()
            }
            .setNegativeButton("Refresh Location") { _, _ ->
                if (!PermissionHelper.hasLocationPermission(context)) {
                    onRequestLocationPermission()
                } else {
                    onRefreshLocation()
                }
            }
            .show()
    }
}

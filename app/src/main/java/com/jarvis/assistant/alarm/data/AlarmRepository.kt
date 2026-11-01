package com.jarvis.assistant.alarm.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlarmRepository private constructor() {

    data class AlarmItem(
        val id: Long = 0L,
        val hour: Int = 7,
        val minute: Int = 0,
        val label: String = "",
        val enabled: Boolean = true,
        val nextTriggerEpochMillis: Long = 0L
    )

    data class VoiceActionNotice(
        val actionType: String,
        val summaryText: String
    )

    private val _allAlarms = MutableStateFlow<List<AlarmItem>>(emptyList())
    val allAlarms: StateFlow<List<AlarmItem>> = _allAlarms.asStateFlow()

    private val _lastVoiceAction = MutableStateFlow<VoiceActionNotice?>(null)
    val lastVoiceAction: StateFlow<VoiceActionNotice?> = _lastVoiceAction.asStateFlow()

    suspend fun undoLastDelete() {
        // Restores last deleted alarm if any
    }

    fun clearVoiceActionNotice() {
        _lastVoiceAction.value = null
    }

    companion object {
        @Volatile
        private var INSTANCE: AlarmRepository? = null

        fun getInstance(context: Context): AlarmRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AlarmRepository().also { INSTANCE = it }
            }
        }
    }
}

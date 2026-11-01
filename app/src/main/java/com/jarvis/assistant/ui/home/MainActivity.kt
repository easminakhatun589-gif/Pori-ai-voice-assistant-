package com.jarvis.assistant.ui.home

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import com.jarvis.assistant.background.JarvisConversationService
import com.jarvis.assistant.data.model.AudioFrequencyState
import com.jarvis.assistant.data.model.ConversationState
import com.jarvis.assistant.data.model.LiveErrorState
import com.jarvis.assistant.security.configureAppCheck
import com.jarvis.assistant.vision.ScreenCaptureManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {

    private val _conversationState = MutableStateFlow(ConversationState.IDLE)
    val conversationState: StateFlow<ConversationState> = _conversationState.asStateFlow()

    private val _isSessionOn = MutableStateFlow(false)
    val isSessionOn: StateFlow<Boolean> = _isSessionOn.asStateFlow()

    private val _isMicMuted = MutableStateFlow(false)
    val isMicMuted: StateFlow<Boolean> = _isMicMuted.asStateFlow()

    private val _liveUserTranscript = MutableStateFlow("")
    val liveUserTranscript: StateFlow<String> = _liveUserTranscript.asStateFlow()

    private val _liveAssistantResponse = MutableStateFlow("")
    val liveAssistantResponse: StateFlow<String> = _liveAssistantResponse.asStateFlow()

    private val _audioFrequency = MutableStateFlow(AudioFrequencyState())
    val audioFrequency: StateFlow<AudioFrequencyState> = _audioFrequency.asStateFlow()

    private val _liveError = MutableStateFlow<LiveErrorState?>(null)
    val liveError: StateFlow<LiveErrorState?> = _liveError.asStateFlow()

    private val _connectionStatus = MutableStateFlow("ONLINE")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    fun clearLiveError() {
        _liveError.value = null
    }

    fun toggleMicOrOrb() {
        if (!_isSessionOn.value) {
            _isSessionOn.value = true
            _isMicMuted.value = false
            _conversationState.value = ConversationState.LISTENING
            JarvisConversationService.setSessionActive(true)
        } else {
            val nextMute = !_isMicMuted.value
            _isMicMuted.value = nextMute
            _conversationState.value = if (nextMute) ConversationState.IDLE else ConversationState.LISTENING
        }
    }

    fun togglePowerSession() {
        val nextOn = !_isSessionOn.value
        _isSessionOn.value = nextOn
        _isMicMuted.value = false
        _conversationState.value = if (nextOn) ConversationState.LISTENING else ConversationState.IDLE
        JarvisConversationService.setSessionActive(nextOn)
    }
}

class MainActivity : AppCompatActivity() {

    val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val container = FrameLayout(this).apply {
            id = android.R.id.content.xor(0x1001)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        setContentView(container)

        ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(container.id, HomeFragment())
                .commit()
        }
    }

    fun selectTab(tab: Int) {
        // Tab navigation handler
    }

    fun handleMicOrOrbClick() {
        viewModel.toggleMicOrOrb()
    }

    fun handleHoldMicOrOrb() {
        viewModel.togglePowerSession()
    }

    fun toggleScreenShare() {
        ScreenCaptureManager.getInstance().toggleSharing()
    }

    companion object {
        const val TAB_HOME = 0
        const val TAB_CHAT = 1
        const val TAB_SETTINGS = 2
    }
}

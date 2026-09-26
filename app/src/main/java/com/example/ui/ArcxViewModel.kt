package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ArcxApplication
import com.example.ai.AIModelManager
import com.example.avatar.AvatarState
import com.example.commands.CommandDispatcher
import com.example.commands.CommandExecutionResult
import com.example.core.CoreState
import com.example.data.MemoryEntity
import com.example.data.MessageEntity
import com.example.overlay.ArcxOverlayService
import com.example.voice.SpeechManager
import com.example.voice.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ArcxScreen {
    HUD,
    CHAT,
    TOOLS,
    MEMORY,
    SETTINGS
}

class ArcxViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ArcxApplication
    val repository = app.repository
    private val commandDispatcher = CommandDispatcher(application)
    private val aiModelManager = AIModelManager(repository)

    val ttsManager = TtsManager(application, viewModelScope)

    // Current navigation tab
    private val _currentScreen = MutableStateFlow(ArcxScreen.HUD)
    val currentScreen: StateFlow<ArcxScreen> = _currentScreen.asStateFlow()

    // Core & Avatar states
    private val _coreState = MutableStateFlow(CoreState.IDLE)
    val coreState: StateFlow<CoreState> = _coreState.asStateFlow()

    private val _avatarState = MutableStateFlow(AvatarState.IDLE)
    val avatarState: StateFlow<AvatarState> = _avatarState.asStateFlow()

    private val _statusText = MutableStateFlow("READY")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private val _latestResponse = MutableStateFlow("ARC-X ONLINE. How may I assist you, Executive?")
    val latestResponse: StateFlow<String> = _latestResponse.asStateFlow()

    private val _errorNotice = MutableStateFlow<String?>(null)
    val errorNotice: StateFlow<String?> = _errorNotice.asStateFlow()

    private val _isOverlayRunning = MutableStateFlow(false)
    val isOverlayRunning: StateFlow<Boolean> = _isOverlayRunning.asStateFlow()

    // Database flows
    val messages = repository.messagesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val memories = repository.memoriesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Speech manager
    val speechManager = SpeechManager(
        context = application,
        onResult = { text ->
            _liveTranscript.value = text
            processUserDirective(text)
        },
        onError = { err ->
            _coreState.value = CoreState.ERROR
            _avatarState.value = AvatarState.ALERT
            _statusText.value = "ERROR"
            _errorNotice.value = err
            viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                resetToIdle()
            }
        }
    )

    // Reactive audio level for Core & Avatar
    val audioLevel: StateFlow<Float> = combine(
        speechManager.audioLevel,
        ttsManager.speechAmplitude,
        ttsManager.isSpeaking,
        speechManager.isListening
    ) { micLevel, ttsAmp, isSpeaking, isListening ->
        when {
            isListening -> micLevel
            isSpeaking -> ttsAmp
            else -> 0f
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    init {
        // Observe TTS state to return to IDLE when done
        viewModelScope.launch {
            ttsManager.isSpeaking.collect { speaking ->
                if (speaking) {
                    _coreState.value = CoreState.SPEAKING
                    _avatarState.value = AvatarState.SPEAKING
                    _statusText.value = "SPEAKING..."
                } else if (_coreState.value == CoreState.SPEAKING) {
                    resetToIdle()
                }
            }
        }

        // Check if messages is empty on first boot, add initial greeting
        viewModelScope.launch {
            val existing = repository.messagesFlow.first()
            if (existing.isEmpty()) {
                repository.saveMessage("ARCX", "ARC-X Initialized. All core systems operational. Tap the core or speak to begin.")
            }
        }
    }

    fun navigateTo(screen: ArcxScreen) {
        _currentScreen.value = screen
    }

    fun onCoreClicked() {
        triggerHaptic()
        if (ttsManager.isSpeaking.value) {
            ttsManager.stop()
            resetToIdle()
            return
        }

        if (speechManager.isListening.value) {
            speechManager.stopListening()
            resetToIdle()
            return
        }

        startListening()
    }

    fun startListening() {
        ttsManager.stop()
        _coreState.value = CoreState.LISTENING
        _avatarState.value = AvatarState.LISTENING
        _statusText.value = "LISTENING..."
        _liveTranscript.value = "Listening to voice directive..."
        _errorNotice.value = null
        speechManager.startListening()
    }

    fun stopListening() {
        speechManager.stopListening()
        resetToIdle()
    }

    fun stopSpeaking() {
        ttsManager.stop()
        resetToIdle()
    }

    fun processUserDirective(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank()) return

        speechManager.stopListening()
        ttsManager.stop()

        _coreState.value = CoreState.THINKING
        _avatarState.value = AvatarState.THINKING
        _statusText.value = "THINKING..."
        _liveTranscript.value = trimmed

        viewModelScope.launch {
            // Save user message to database
            repository.saveMessage("USER", trimmed)

            // 1. Evaluate device commands / quick integrations
            val commandResult = commandDispatcher.evaluate(trimmed)
            val (responseText, origin) = when (commandResult) {
                is CommandExecutionResult.Executed -> {
                    Pair(commandResult.responseText, "Executive Command System")
                }
                is CommandExecutionResult.Error -> {
                    Pair(commandResult.errorText, "System Hardware Interface")
                }
                is CommandExecutionResult.NotACommand -> {
                    // Check if user is asking to store a memory (e.g. "Remember that my code is 1234")
                    if (trimmed.startsWith("remember ", ignoreCase = true) || trimmed.startsWith("note that ", ignoreCase = true)) {
                        val memoryContent = trimmed.replace(Regex("^(remember|note that)\\s+(that\\s+)?", RegexOption.IGNORE_CASE), "")
                        repository.saveMemory(
                            key = memoryContent.take(40),
                            value = memoryContent,
                            category = "USER_NOTE"
                        )
                        Pair("Directive confirmed: Information successfully committed to the ARC-X Memory Bank.", "Neural Memory Bank")
                    } else {
                        // Delegate to AI Layer
                        val historyList = repository.getRecentMessages(8)
                        val memoryList = repository.memoriesFlow.first()
                        aiModelManager.processRequest(trimmed, historyList, memoryList)
                    }
                }
            }

            // Save AI message to database
            repository.saveMessage("ARCX", responseText)
            _latestResponse.value = responseText

            // Voice response
            if (repository.autoSpeak.value) {
                _coreState.value = CoreState.SPEAKING
                _avatarState.value = AvatarState.SPEAKING
                _statusText.value = "SPEAKING..."
                ttsManager.speak(
                    text = responseText,
                    pitch = repository.voicePitch.value,
                    speed = repository.voiceSpeed.value
                )
            } else {
                resetToIdle()
            }
        }
    }

    private fun resetToIdle() {
        _coreState.value = CoreState.IDLE
        _avatarState.value = AvatarState.IDLE
        _statusText.value = "READY"
    }

    fun dismissError() {
        _errorNotice.value = null
    }

    fun addMemory(key: String, value: String, category: String = "GENERAL") {
        viewModelScope.launch {
            repository.saveMemory(key, value, category)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun clearConversation() {
        viewModelScope.launch {
            repository.clearMessages()
            repository.saveMessage("ARCX", "Conversation cleared. ARC-X standing by.")
            _latestResponse.value = "Conversation buffer cleared."
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearMemories()
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            repository.saveMessage("ARCX", "System purge complete. ARC-X re-initialized.")
            _latestResponse.value = "All local data has been purged."
            resetToIdle()
        }
    }

    fun toggleOverlay(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            _errorNotice.value = "Overlay permission requested. Enable 'Display over other apps' to use Floating Core."
            return
        }

        val serviceIntent = Intent(context, ArcxOverlayService::class.java)
        if (_isOverlayRunning.value) {
            context.stopService(serviceIntent)
            _isOverlayRunning.value = false
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            _isOverlayRunning.value = true
        }
    }

    fun completeOnboarding() {
        repository.setOnboardingCompleted(true)
    }

    private fun triggerHaptic() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                v?.vibrate(40)
            }
        } catch (e: Exception) {
            // Ignore haptic failure
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.stopListening()
        ttsManager.destroy()
    }
}

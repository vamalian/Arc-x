package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ArcxRepository(
    context: Context,
    private val database: ArcxDatabase
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("arcx_prefs", Context.MODE_PRIVATE)

    // Message database flows
    val messagesFlow: Flow<List<MessageEntity>> = database.messageDao().getAllMessages()
    val memoriesFlow: Flow<List<MemoryEntity>> = database.memoryDao().getAllMemories()

    suspend fun getRecentMessages(limit: Int = 10): List<MessageEntity> {
        return database.messageDao().getRecentMessages(limit)
    }

    suspend fun saveMessage(sender: String, content: String): Long {
        return database.messageDao().insertMessage(
            MessageEntity(
                sender = sender,
                content = content,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteMessage(id: Long) {
        database.messageDao().deleteMessage(id)
    }

    suspend fun clearMessages() {
        database.messageDao().clearAllMessages()
    }

    // Memories
    fun searchMemories(query: String): Flow<List<MemoryEntity>> {
        return database.memoryDao().searchMemories(query)
    }

    suspend fun saveMemory(key: String, value: String, category: String = "GENERAL"): Long {
        return database.memoryDao().insertMemory(
            MemoryEntity(
                key = key.trim(),
                value = value.trim(),
                category = category,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteMemory(id: Long) {
        database.memoryDao().deleteMemory(id)
    }

    suspend fun clearMemories() {
        database.memoryDao().clearAllMemories()
    }

    // Preference settings
    private val _customApiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _selectedProvider = MutableStateFlow(prefs.getString("ai_provider", "GEMINI") ?: "GEMINI")
    val selectedProvider: StateFlow<String> = _selectedProvider.asStateFlow()

    private val _voicePitch = MutableStateFlow(prefs.getFloat("voice_pitch", 1.0f))
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()

    private val _voiceSpeed = MutableStateFlow(prefs.getFloat("voice_speed", 1.0f))
    val voiceSpeed: StateFlow<Float> = _voiceSpeed.asStateFlow()

    private val _autoSpeak = MutableStateFlow(prefs.getBoolean("auto_speak", true))
    val autoSpeak: StateFlow<Boolean> = _autoSpeak.asStateFlow()

    private val _avatarEnabled = MutableStateFlow(prefs.getBoolean("avatar_enabled", true))
    val avatarEnabled: StateFlow<Boolean> = _avatarEnabled.asStateFlow()

    private val _avatarIntensity = MutableStateFlow(prefs.getFloat("avatar_intensity", 0.85f))
    val avatarIntensity: StateFlow<Float> = _avatarIntensity.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_completed", false))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    fun setCustomApiKey(key: String) {
        prefs.edit().putString("custom_api_key", key).apply()
        _customApiKey.value = key
    }

    fun setSelectedProvider(provider: String) {
        prefs.edit().putString("ai_provider", provider).apply()
        _selectedProvider.value = provider
    }

    fun setVoicePitch(pitch: Float) {
        prefs.edit().putFloat("voice_pitch", pitch).apply()
        _voicePitch.value = pitch
    }

    fun setVoiceSpeed(speed: Float) {
        prefs.edit().putFloat("voice_speed", speed).apply()
        _voiceSpeed.value = speed
    }

    fun setAutoSpeak(auto: Boolean) {
        prefs.edit().putBoolean("auto_speak", auto).apply()
        _autoSpeak.value = auto
    }

    fun setAvatarEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("avatar_enabled", enabled).apply()
        _avatarEnabled.value = enabled
    }

    fun setAvatarIntensity(intensity: Float) {
        prefs.edit().putFloat("avatar_intensity", intensity).apply()
        _avatarIntensity.value = intensity
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
        _onboardingCompleted.value = completed
    }

    suspend fun clearAllData() {
        clearMessages()
        clearMemories()
        prefs.edit().clear().apply()
        _customApiKey.value = ""
        _selectedProvider.value = "GEMINI"
        _voicePitch.value = 1.0f
        _voiceSpeed.value = 1.0f
        _autoSpeak.value = true
        _avatarEnabled.value = true
        _avatarIntensity.value = 0.85f
        _onboardingCompleted.value = false
    }
}

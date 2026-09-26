package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class TtsManager(
    private val context: Context,
    private val scope: CoroutineScope
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechAmplitude = MutableStateFlow(0f)
    val speechAmplitude: StateFlow<Float> = _speechAmplitude.asStateFlow()

    private var amplitudeJob: Job? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            isInitialized = true
            setupProgressListener()
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
                startAmplitudeSimulation()
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                stopAmplitudeSimulation()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                stopAmplitudeSimulation()
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
                stopAmplitudeSimulation()
            }
        })
    }

    private fun startAmplitudeSimulation() {
        amplitudeJob?.cancel()
        amplitudeJob = scope.launch(Dispatchers.Default) {
            var step = 0
            while (isActive && _isSpeaking.value) {
                // Organic rhythmic speech pulse simulation
                val wave = (Math.sin(step * 0.35) * 0.5 + 0.5).toFloat()
                val jitter = (Math.random() * 0.4).toFloat()
                _speechAmplitude.value = (wave * 0.7f + jitter * 0.3f).coerceIn(0.1f, 1.0f)
                step++
                delay(60)
            }
            _speechAmplitude.value = 0f
        }
    }

    private fun stopAmplitudeSimulation() {
        amplitudeJob?.cancel()
        _speechAmplitude.value = 0f
    }

    fun speak(text: String, pitch: Float = 1.0f, speed: Float = 1.0f) {
        if (!isInitialized) return
        stop()

        tts?.setPitch(pitch)
        tts?.setSpeechRate(speed)

        // Strip markdown asterisks or special formatting for cleaner speech
        val cleanSpeech = text
            .replace(Regex("\\*\\*|\\*|__|_|`"), "")
            .replace(Regex("\\[ARC-X Notice:[^]]+]"), "")
            .trim()

        val params = android.os.Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "arcx_speech_${System.currentTimeMillis()}")
        tts?.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, params, "arcx_speech")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        stopAmplitudeSimulation()
    }

    fun destroy() {
        stop()
        tts?.shutdown()
        tts = null
    }
}

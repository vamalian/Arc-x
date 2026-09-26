package com.example.ai

import com.example.data.ArcxRepository
import com.example.data.MemoryEntity
import com.example.data.MessageEntity
import kotlinx.coroutines.flow.first

class AIModelManager(
    private val repository: ArcxRepository
) {
    private val offlineProvider = OfflineExecutiveProvider()
    private val geminiProvider = GeminiProvider(
        getApiKey = { repository.customApiKey.value }
    )

    fun getAvailableProviders(): List<AIProvider> = listOf(
        geminiProvider,
        offlineProvider
    )

    suspend fun processRequest(
        prompt: String,
        history: List<MessageEntity>,
        memories: List<MemoryEntity>
    ): Pair<String, String> {
        val selected = repository.selectedProvider.value
        val primaryProvider = if (selected == "OFFLINE") offlineProvider else geminiProvider

        val result = primaryProvider.generateResponse(prompt, history, memories)
        return if (result.isSuccess) {
            Pair(result.getOrThrow(), primaryProvider.displayName)
        } else {
            // Fallback to offline executive engine with notification
            val fallback = offlineProvider.generateResponse(prompt, history, memories)
            val fallbackText = fallback.getOrDefault("System offline response: Directive acknowledged.")
            val combined = if (primaryProvider.id == "GEMINI") {
                "$fallbackText\n\n[ARC-X Notice: Cloud Link bypassed. ${result.exceptionOrNull()?.localizedMessage ?: "Switched to offline neural core."}]"
            } else {
                fallbackText
            }
            Pair(combined, "Offline Fallback Core")
        }
    }
}

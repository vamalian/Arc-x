package com.example.ai

import com.example.data.MemoryEntity
import com.example.data.MessageEntity

interface AIProvider {
    val id: String
    val displayName: String
    suspend fun generateResponse(
        prompt: String,
        history: List<MessageEntity>,
        memories: List<MemoryEntity>
    ): Result<String>
}

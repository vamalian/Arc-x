package com.example.ai

import com.example.BuildConfig
import com.example.data.MemoryEntity
import com.example.data.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiProvider(
    private val getApiKey: () -> String
) : AIProvider {

    override val id: String = "GEMINI"
    override val displayName: String = "Gemini 3.5 Flash (Cloud Intelligence)"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun generateResponse(
        prompt: String,
        history: List<MessageEntity>,
        memories: List<MemoryEntity>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val key = getApiKey().ifBlank { BuildConfig.GEMINI_API_KEY }
            if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(
                    IllegalStateException("Gemini API key is not configured. Please supply your API key in ARC-X Command Center Settings.")
                )
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$key"

            val systemPromptBuilder = StringBuilder()
            systemPromptBuilder.append(
                "You are ARC-X (Advanced Reactive Cognitive eXecutive), a cutting-edge futuristic Android AI assistant. " +
                "Your persona is calm, highly articulate, intelligent, sharp, and helpful. " +
                "Keep spoken-style responses conversational, concise, and structured. " +
                "You are an original AI entity, not JARVIS or Marvel-affiliated. " +
                "If asked to perform Android device actions (e.g. open an app, search the web, toggle flashlight, check battery), " +
                "confirm clearly that ARC-X is handling the directive.\n"
            )

            if (memories.isNotEmpty()) {
                systemPromptBuilder.append("\nKnown user memories and context:\n")
                memories.take(15).forEach { mem ->
                    systemPromptBuilder.append("- [${mem.category}] ${mem.key}: ${mem.value}\n")
                }
            }

            val requestJson = JSONObject()

            // System instruction
            val sysInstructionObj = JSONObject()
            val sysPartsArr = JSONArray().put(JSONObject().put("text", systemPromptBuilder.toString()))
            sysInstructionObj.put("parts", sysPartsArr)
            requestJson.put("system_instruction", sysInstructionObj)

            // Contents array
            val contentsArr = JSONArray()
            // Recent history (last 8 turns)
            val recentTurns = history.takeLast(8)
            for (msg in recentTurns) {
                val role = if (msg.sender == "USER") "user" else "model"
                val contentObj = JSONObject()
                contentObj.put("role", role)
                val parts = JSONArray().put(JSONObject().put("text", msg.content))
                contentObj.put("parts", parts)
                contentsArr.put(contentObj)
            }

            // Current prompt
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            currentTurn.put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            contentsArr.put(currentTurn)

            requestJson.put("contents", contentsArr)

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("maxOutputTokens", 1200)
            requestJson.put("generationConfig", genConfig)

            val requestBody = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errorObj = JSONObject(responseBody).optJSONObject("error")
                    errorObj?.optString("message") ?: "HTTP ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                return@withContext Result.failure(Exception("AI Link Error: $errorMsg"))
            }

            val responseObj = JSONObject(responseBody)
            val candidates = responseObj.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("No output generated from ARC-X core."))
            }

            val firstCand = candidates.getJSONObject(0)
            val content = firstCand.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.getJSONObject(0)?.optString("text")

            if (text.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Empty cognitive stream returned."))
            }

            Result.success(text.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

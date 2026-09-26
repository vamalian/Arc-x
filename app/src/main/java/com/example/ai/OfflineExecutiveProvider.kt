package com.example.ai

import com.example.data.MemoryEntity
import com.example.data.MessageEntity
import kotlinx.coroutines.delay

class OfflineExecutiveProvider : AIProvider {
    override val id: String = "OFFLINE"
    override val displayName: String = "ARC-X Neural Core (Offline Engine)"

    override suspend fun generateResponse(
        prompt: String,
        history: List<MessageEntity>,
        memories: List<MemoryEntity>
    ): Result<String> {
        delay(350) // Simulate cognitive processing latency
        val clean = prompt.trim().lowercase()

        val response = when {
            clean.contains("who are you") || clean.contains("what are you") -> {
                "I am ARC-X: Advanced Reactive Cognitive eXecutive. I am your futuristic personal AI companion, equipped with neural dialog capabilities, voice intelligence, and local executive controls."
            }

            clean.contains("status") || clean.contains("diagnostic") || clean.contains("system check") -> {
                "ARC-X Core diagnostic report:\n• Cognitive Reactor: Operational at 99.4% efficiency\n• Speech Synthesis: Active\n• Neural Memory Bank: Synchronized\n• Ambient Telemetry: Nominal\nAll systems running within safe parameters."
            }

            clean.contains("what can you do") || clean.contains("help") || clean.contains("capabilities") -> {
                "Directive capabilities available:\n" +
                "1. Spoken dialog & voice reasoning\n" +
                "2. System integrations: Open apps, web queries, clipboard analysis, flashlight\n" +
                "3. Memory Bank: Retain important notes, tasks, and personal directives\n" +
                "4. Cloud Intelligence: Switch to Gemini 3.5 in Settings for deep reasoning\n" +
                "5. Floating HUD overlay: Quick access over other applications."
            }

            clean.startsWith("calculate") || clean.contains("+") || clean.contains("-") || clean.contains("*") || clean.contains("/") -> {
                tryCalculate(prompt)
            }

            clean.contains("quantum") -> {
                "Quantum computing leverages superposition and quantum entanglement to process complex parallel problem spaces exponentially faster than classical binary architectures."
            }

            clean.contains("artificial intelligence") || clean.contains("ai") && clean.length < 30 -> {
                "Artificial intelligence in the ARC-X architecture combines modular predictive networks with reactive sensory inputs to execute real-time companion support."
            }

            clean.contains("time") || clean.contains("date") -> {
                val now = java.text.SimpleDateFormat("EEEE, MMMM d, yyyy - HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                "Current temporal coordinate: $now."
            }

            clean.contains("weather") -> {
                "To get live atmospheric telemetry for your coordinates, ask 'Search weather' or check the Tools matrix."
            }

            clean.contains("hello") || clean.contains("hi") || clean.contains("hey") -> {
                "Greetings. ARC-X neural link established. Ready for your directive."
            }

            clean.contains("thank") -> {
                "Acknowledged. Standing by for further directives."
            }

            clean.contains("sleep") || clean.contains("shut down") || clean.contains("standby") -> {
                "Entering low-power standby mode. Tap the ARC-X Core when you require my assistance."
            }

            memories.isNotEmpty() && (clean.contains("remember") || clean.contains("my")) -> {
                val matched = memories.firstOrNull { mem -> clean.contains(mem.key.lowercase()) }
                if (matched != null) {
                    "Recalling from memory bank: ${matched.key} is recorded as '${matched.value}'."
                } else {
                    "Directive received: \"$prompt\". Synthesizing executive plan."
                }
            }

            else -> {
                "Processing directive: \"$prompt\". As ARC-X, I have evaluated your input. For deep reasoning on this subject, ensure your Gemini Cloud API key is connected in Settings, or use the Command matrix."
            }
        }

        return Result.success(response)
    }

    private fun tryCalculate(input: String): String {
        return try {
            val expr = input.replace("calculate", "", ignoreCase = true).trim()
            val sanitized = expr.replace("x", "*").replace("×", "*").replace("÷", "/")
            val parts = sanitized.split(Regex("(?<=[-+*/])|(?=[-+*/])")).map { it.trim() }.filter { it.isNotEmpty() }
            if (parts.size >= 3) {
                val a = parts[0].toDouble()
                val op = parts[1]
                val b = parts[2].toDouble()
                val res = when (op) {
                    "+" -> a + b
                    "-" -> a - b
                    "*" -> a * b
                    "/" -> if (b != 0.0) a / b else Double.NaN
                    else -> null
                }
                if (res != null) {
                    return "Executive calculation: $expr = ${if (res % 1 == 0.0) res.toLong().toString() else res.toString()}"
                }
            }
            "Calculating: $expr. Evaluated via neural processor."
        } catch (e: Exception) {
            "Calculation analysis completed for: $input."
        }
    }
}

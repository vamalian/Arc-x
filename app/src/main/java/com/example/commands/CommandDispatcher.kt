package com.example.commands

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.Settings
import java.util.Locale

sealed class CommandExecutionResult {
    data class Executed(val responseText: String) : CommandExecutionResult()
    object NotACommand : CommandExecutionResult()
    data class Error(val errorText: String) : CommandExecutionResult()
}

class CommandDispatcher(private val context: Context) {

    private val clipboard: ClipboardManager? =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    private val cameraManager: CameraManager? =
        context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    private var isTorchOn = false

    fun evaluate(input: String): CommandExecutionResult {
        val lower = input.trim().lowercase(Locale.ROOT)

        // 1. App Launchers
        if (lower.startsWith("open ") || lower.startsWith("launch ") || lower.startsWith("start ")) {
            val target = lower.removePrefix("open ")
                .removePrefix("launch ")
                .removePrefix("start ")
                .trim()
            return handleAppLaunch(target)
        }

        // 2. Web Search
        if (lower.startsWith("search ") || lower.startsWith("web search ") || lower.startsWith("google ")) {
            val query = lower.removePrefix("search ")
                .removePrefix("web search ")
                .removePrefix("google ")
                .replace(Regex("^for "), "")
                .trim()
            return handleWebSearch(query)
        }

        // 3. Clipboard
        if (lower.contains("clipboard")) {
            if (lower.contains("read") || lower.contains("what") || lower.contains("check")) {
                return handleReadClipboard()
            }
        }

        // 4. Flashlight / Torch
        if (lower.contains("flashlight") || lower.contains("torch")) {
            val enable = !lower.contains("off") && !lower.contains("disable")
            return handleFlashlight(enable)
        }

        // 5. Battery & Telemetry
        if (lower.contains("battery") || lower.contains("charge") || lower.contains("power level")) {
            return handleBatteryQuery()
        }

        if (lower.contains("system status") || lower.contains("telemetry") || lower.contains("hardware status") || lower.contains("ram")) {
            return handleSystemTelemetry()
        }

        // 6. Settings
        if (lower.contains("device settings") || lower == "settings") {
            return handleOpenSettings()
        }

        // 7. Alarms & Timers
        if (lower.startsWith("set timer") || lower.startsWith("set alarm")) {
            return handleSetAlarm(lower)
        }

        return CommandExecutionResult.NotACommand
    }

    private fun handleAppLaunch(target: String): CommandExecutionResult {
        val packageName = when {
            target.contains("youtube") -> "com.google.android.youtube"
            target.contains("chrome") || target.contains("browser") -> "com.android.chrome"
            target.contains("map") -> "com.google.android.apps.maps"
            target.contains("setting") -> {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return CommandExecutionResult.Executed("Executive Directive: Launching Android System Settings.")
            }
            target.contains("camera") -> {
                val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                return tryLaunchIntent(intent, "Camera receptor")
            }
            target.contains("calculator") -> "com.google.android.calculator"
            target.contains("calendar") -> "com.google.android.calendar"
            target.contains("clock") || target.contains("alarm") -> "com.google.android.deskclock"
            else -> null
        }

        if (packageName != null) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return CommandExecutionResult.Executed("Executive Directive: Initializing application subsystem for $target.")
            }
        }

        // Fallback to web search or store query for app
        return try {
            val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(android.app.SearchManager.QUERY, target)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(searchIntent)
            CommandExecutionResult.Executed("Application not installed locally. Routing directive for '$target' to Web Search matrix.")
        } catch (e: Exception) {
            CommandExecutionResult.Error("Unable to execute application launch for: $target.")
        }
    }

    private fun handleWebSearch(query: String): CommandExecutionResult {
        return try {
            val encodedQuery = Uri.encode(query)
            val uri = Uri.parse("https://www.google.com/search?q=$encodedQuery")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CommandExecutionResult.Executed("Executive directive confirmed: Executing quantum web query for \"$query\". Results dispatched to browser interface.")
        } catch (e: Exception) {
            CommandExecutionResult.Error("Search matrix connection failed: ${e.message}")
        }
    }

    private fun handleReadClipboard(): CommandExecutionResult {
        return try {
            val clip = clipboard?.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString()
                if (!text.isNullOrBlank()) {
                    CommandExecutionResult.Executed("Clipboard buffer retrieved:\n\"$text\"")
                } else {
                    CommandExecutionResult.Executed("Clipboard telemetry indicates buffer is currently empty.")
                }
            } else {
                CommandExecutionResult.Executed("No data located in the device clipboard register.")
            }
        } catch (e: Exception) {
            CommandExecutionResult.Error("Clipboard security barrier: ${e.message}")
        }
    }

    fun copyToClipboard(text: String, label: String = "ARC-X Output") {
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
    }

    private fun handleFlashlight(enable: Boolean): CommandExecutionResult {
        return try {
            if (cameraManager == null) {
                return CommandExecutionResult.Error("Optical illumination module unavailable on this hardware.")
            }
            val cameraId = cameraManager.cameraIdList.firstOrNull()
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, enable)
                isTorchOn = enable
                val state = if (enable) "engaged" else "disengaged"
                CommandExecutionResult.Executed("Illumination emitter $state.")
            } else {
                CommandExecutionResult.Error("No primary camera module located for illumination control.")
            }
        } catch (e: Exception) {
            CommandExecutionResult.Error("Flashlight driver encountered an error: ${e.message}")
        }
    }

    private fun handleBatteryQuery(): CommandExecutionResult {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            val isCharging = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == BatteryManager.BATTERY_STATUS_CHARGING
            } else false

            val statusText = if (isCharging) "Power Source Connected (Charging)" else "Discharging (Battery Cell)"
            CommandExecutionResult.Executed("Battery power telemetry: $level% capacity. Status: $statusText.")
        } catch (e: Exception) {
            CommandExecutionResult.Error("Battery diagnostic sensor error: ${e.message}")
        }
    }

    private fun handleSystemTelemetry(): CommandExecutionResult {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)

            val totalRamGb = memInfo.totalMem / (1024 * 1024 * 1024.0)
            val availRamGb = memInfo.availMem / (1024 * 1024 * 1024.0)

            val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNet = connManager?.activeNetwork
            val caps = connManager?.getNetworkCapabilities(activeNet)
            val netType = when {
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi High Speed"
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular 4G/5G"
                else -> "Offline / Local Only"
            }

            val telemetry = "ARC-X System Telemetry:\n" +
                    "• Device: ${Build.MANUFACTURER.uppercase()} ${Build.MODEL}\n" +
                    "• Android API: ${Build.VERSION.SDK_INT} (OS ${Build.VERSION.RELEASE})\n" +
                    "• Memory: %.1f GB available / %.1f GB total\n".format(availRamGb, totalRamGb) +
                    "• Data Link: $netType\n" +
                    "• Processor Architecture: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "ARM64"}"

            CommandExecutionResult.Executed(telemetry)
        } catch (e: Exception) {
            CommandExecutionResult.Error("Telemetry diagnostic scan failed: ${e.message}")
        }
    }

    private fun handleOpenSettings(): CommandExecutionResult {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            CommandExecutionResult.Executed("Navigating to Android System Preferences.")
        } catch (e: Exception) {
            CommandExecutionResult.Error("Unable to access Settings provider.")
        }
    }

    private fun handleSetAlarm(lower: String): CommandExecutionResult {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, "ARC-X Directive")
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CommandExecutionResult.Executed("Dispatched chronometer directive to System Clock subsystem.")
        } catch (e: Exception) {
            CommandExecutionResult.Error("Alarm subsystem could not be engaged: ${e.message}")
        }
    }

    private fun tryLaunchIntent(intent: Intent, label: String): CommandExecutionResult {
        return try {
            context.startActivity(intent)
            CommandExecutionResult.Executed("Engaging $label.")
        } catch (e: Exception) {
            CommandExecutionResult.Error("Target $label is not available.")
        }
    }
}

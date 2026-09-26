package com.example.ui

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcxAlertRed
import com.example.ui.theme.ArcxCyan
import com.example.ui.theme.ArcxDeepSurface
import com.example.ui.theme.ArcxHudBorder
import com.example.ui.theme.ArcxSurface
import com.example.ui.theme.ArcxTextMuted
import com.example.ui.theme.ArcxTextPrimary
import com.example.ui.theme.ArcxTextSecondary
import com.example.ui.theme.ArcxVoid
import com.example.ui.theme.ArcxWarningAmber

@Composable
fun SettingsScreen(
    viewModel: ArcxViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = viewModel.repository

    val customKey by repository.customApiKey.collectAsState()
    val selectedProvider by repository.selectedProvider.collectAsState()
    val voicePitch by repository.voicePitch.collectAsState()
    val voiceSpeed by repository.voiceSpeed.collectAsState()
    val autoSpeak by repository.autoSpeak.collectAsState()
    val avatarEnabled by repository.avatarEnabled.collectAsState()
    val avatarIntensity by repository.avatarIntensity.collectAsState()
    val isOverlayRunning by viewModel.isOverlayRunning.collectAsState()

    var apiKeyInput by remember(customKey) { mutableStateOf(customKey) }
    var showKey by remember { mutableStateOf(false) }
    var showWipeDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    if (showWipeDialog) {
        AlertDialog(
            onDismissRequest = { showWipeDialog = false },
            title = { Text("Complete System Purge?", color = ArcxTextPrimary) },
            text = { Text("This will permanently clear conversation history, stored memories, custom keys, and reset all ARC-X preferences to factory default.", color = ArcxTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showWipeDialog = false
                    }
                ) {
                    Text("Purge Everything", color = ArcxAlertRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeDialog = false }) {
                    Text("Cancel", color = ArcxTextPrimary)
                }
            },
            containerColor = ArcxDeepSurface
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ArcxVoid)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "COMMAND CENTER & CONFIGURATION",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = ArcxCyan
        )
        Text(
            text = "Neural Engines, Hologram & Security Controls",
            fontSize = 11.sp,
            color = ArcxTextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 1. AI Intelligence Section
        SettingsCard(title = "AI INTELLIGENCE SUITE", icon = Icons.Default.Psychology) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Provider Selection
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedProvider == "GEMINI",
                        onClick = { repository.setSelectedProvider("GEMINI") },
                        colors = RadioButtonDefaults.colors(selectedColor = ArcxCyan)
                    )
                    Column {
                        Text("Gemini 3.5 Flash (Cloud)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ArcxTextPrimary)
                        Text("High-speed quantum cloud reasoning via Google Generative AI", fontSize = 11.sp, color = ArcxTextSecondary)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedProvider == "OFFLINE",
                        onClick = { repository.setSelectedProvider("OFFLINE") },
                        colors = RadioButtonDefaults.colors(selectedColor = ArcxCyan)
                    )
                    Column {
                        Text("ARC-X Neural Core (Offline Engine)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ArcxTextPrimary)
                        Text("Zero-latency on-device executive logic & commands", fontSize = 11.sp, color = ArcxTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Custom API Key field
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = {
                        apiKeyInput = it
                        repository.setCustomApiKey(it)
                    },
                    label = { Text("Custom Gemini API Key (Optional)", color = ArcxTextSecondary, fontSize = 12.sp) },
                    placeholder = { Text("Configured via Secrets panel or enter here", fontSize = 11.sp, color = ArcxTextMuted) },
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle visibility",
                                tint = ArcxTextSecondary
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcxCyan,
                        unfocusedBorderColor = ArcxHudBorder,
                        focusedTextColor = ArcxTextPrimary,
                        unfocusedTextColor = ArcxTextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Voice & Audio Section
        SettingsCard(title = "VOICE & SPEECH SYNTHESIS", icon = Icons.Default.RecordVoiceOver) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Auto-speak toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Auto-Speak Responses", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ArcxTextPrimary)
                        Text("Read cognitive output aloud via Android Text-to-Speech", fontSize = 11.sp, color = ArcxTextSecondary)
                    }
                    Switch(
                        checked = autoSpeak,
                        onCheckedChange = { repository.setAutoSpeak(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ArcxCyan, checkedTrackColor = ArcxCyan.copy(alpha = 0.4f))
                    )
                }

                // Speech Speed Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Speech Rate", fontSize = 12.sp, color = ArcxTextSecondary)
                        Text(String.format("%.1fx", voiceSpeed), fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = ArcxCyan)
                    }
                    Slider(
                        value = voiceSpeed,
                        onValueChange = { repository.setVoiceSpeed(it) },
                        valueRange = 0.6f..1.8f,
                        colors = SliderDefaults.colors(thumbColor = ArcxCyan, activeTrackColor = ArcxCyan)
                    )
                }

                // Speech Pitch Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Vocal Pitch", fontSize = 12.sp, color = ArcxTextSecondary)
                        Text(String.format("%.1fx", voicePitch), fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = ArcxCyan)
                    }
                    Slider(
                        value = voicePitch,
                        onValueChange = { repository.setVoicePitch(it) },
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = ArcxCyan, activeTrackColor = ArcxCyan)
                    )
                }

                // Test voice button
                Button(
                    onClick = {
                        viewModel.ttsManager.speak(
                            "ARC-X voice receptor and synthesis matrix active. Standing by for directives.",
                            pitch = voicePitch,
                            speed = voiceSpeed
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ArcxSurface)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ArcxCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Vocal Synthesizer", color = ArcxTextPrimary, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. 3D Avatar & Hologram Section
        SettingsCard(title = "3D MALE AI AVATAR & HOLOGRAM", icon = Icons.Default.Person) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Enable 3D Male AI Companion", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ArcxTextPrimary)
                        Text("Renders digital assistant with breathing & vocal waves", fontSize = 11.sp, color = ArcxTextSecondary)
                    }
                    Switch(
                        checked = avatarEnabled,
                        onCheckedChange = { repository.setAvatarEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ArcxCyan, checkedTrackColor = ArcxCyan.copy(alpha = 0.4f))
                    )
                }

                if (avatarEnabled) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Avatar Background Visibility", fontSize = 12.sp, color = ArcxTextSecondary)
                            Text("${(avatarIntensity * 100).toInt()}%", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = ArcxCyan)
                        }
                        Slider(
                            value = avatarIntensity,
                            onValueChange = { repository.setAvatarIntensity(it) },
                            valueRange = 0.2f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = ArcxCyan, activeTrackColor = ArcxCyan)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Floating Overlay Section
        SettingsCard(title = "FLOATING ARC-X OVERLAY", icon = Icons.Default.Layers) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Floating ARC-X Core Widget", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ArcxTextPrimary)
                        Text("Display a draggable mini core above other Android apps", fontSize = 11.sp, color = ArcxTextSecondary)
                    }
                    Button(
                        onClick = { viewModel.toggleOverlay(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOverlayRunning) ArcxAlertRed.copy(alpha = 0.3f) else ArcxCyan.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("toggle_overlay_button")
                    ) {
                        Text(
                            text = if (isOverlayRunning) "Disable" else "Enable",
                            color = if (isOverlayRunning) ArcxAlertRed else ArcxCyan,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Privacy & Data Control
        SettingsCard(title = "PRIVACY & LOCAL DATA VAULT", icon = Icons.Default.PrivacyTip) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "All voice synthesis and memories reside securely on your device. ARC-X never shares personal information without your explicit command.",
                    fontSize = 11.sp,
                    color = ArcxTextSecondary,
                    lineHeight = 16.sp
                )

                Button(
                    onClick = { showWipeDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wipe_all_data_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ArcxAlertRed.copy(alpha = 0.15f))
                ) {
                    Text("Purge All Local Data & Reset ARC-X", color = ArcxAlertRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ArcxHudBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = ArcxDeepSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = ArcxCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = ArcxCyan
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

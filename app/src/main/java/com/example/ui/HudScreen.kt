package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.avatar.AvatarView
import com.example.core.ArcxCoreView
import com.example.core.CoreState
import com.example.ui.theme.ArcxAlertRed
import com.example.ui.theme.ArcxCyan
import com.example.ui.theme.ArcxCyanGlow
import com.example.ui.theme.ArcxDeepSurface
import com.example.ui.theme.ArcxGlassBackground
import com.example.ui.theme.ArcxHudBorder
import com.example.ui.theme.ArcxNeonGreen
import com.example.ui.theme.ArcxTextMuted
import com.example.ui.theme.ArcxTextPrimary
import com.example.ui.theme.ArcxTextSecondary
import com.example.ui.theme.ArcxVoid
import com.example.ui.theme.ArcxWarningAmber

@Composable
fun HudScreen(
    viewModel: ArcxViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coreState by viewModel.coreState.collectAsState()
    val avatarState by viewModel.avatarState.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val liveTranscript by viewModel.liveTranscript.collectAsState()
    val latestResponse by viewModel.latestResponse.collectAsState()
    val errorNotice by viewModel.errorNotice.collectAsState()

    val avatarEnabled by viewModel.repository.avatarEnabled.collectAsState()
    val avatarIntensity by viewModel.repository.avatarIntensity.collectAsState()
    val autoSpeak by viewModel.repository.autoSpeak.collectAsState()

    var textInput by remember { mutableStateOf("") }

    // Audio Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    val requestMicAndListen = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            viewModel.onCoreClicked()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(ArcxVoid)) {
        // 1. 3D Male AI Avatar Background
        if (avatarEnabled) {
            AvatarView(
                state = avatarState,
                audioAmplitude = audioLevel,
                intensity = avatarIntensity,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. HUD Scanline & Corner Accents Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD Status Bar
            TopHudBar(
                statusText = statusText,
                coreState = coreState,
                autoSpeak = autoSpeak,
                onToggleAutoSpeak = { viewModel.repository.setAutoSpeak(!autoSpeak) }
            )

            // Center: ARC-X Core & Dynamic Readout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                ArcxCoreView(
                    state = coreState,
                    audioLevel = audioLevel,
                    onCoreClick = requestMicAndListen,
                    sizeDp = 240.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Core Dynamic Status Text
                Text(
                    text = statusText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    color = when (coreState) {
                        CoreState.IDLE -> ArcxCyan
                        CoreState.LISTENING -> ArcxCyanGlow
                        CoreState.THINKING -> ArcxWarningAmber
                        CoreState.SPEAKING -> ArcxCyan
                        CoreState.ERROR -> ArcxAlertRed
                        CoreState.OFFLINE -> ArcxTextMuted
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Live Transcript / Response Card (Glassmorphic)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, ArcxHudBorder, RoundedCornerShape(12.dp)),
                    color = ArcxGlassBackground
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (coreState == CoreState.LISTENING) {
                                if (liveTranscript.isNotBlank()) "Receptor Input: $liveTranscript" else "Speak your directive..."
                            } else {
                                latestResponse
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = ArcxTextPrimary,
                                lineHeight = 20.sp,
                                textAlign = TextAlign.Center
                            ),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Error Notice Banner
                AnimatedVisibility(
                    visible = errorNotice != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    errorNotice?.let { error ->
                        Surface(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth(0.92f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, ArcxAlertRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            color = ArcxDeepSurface
                        ) {
                            Text(
                                text = error,
                                color = ArcxAlertRed,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Bottom Command Prompt & Quick Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Quick actions row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (coreState == CoreState.SPEAKING) {
                        IconButton(
                            onClick = { viewModel.stopSpeaking() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(ArcxAlertRed.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, ArcxAlertRed, CircleShape)
                                .testTag("stop_speech_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop Speech",
                                tint = ArcxAlertRed
                            )
                        }
                    } else {
                        IconButton(
                            onClick = requestMicAndListen,
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    if (coreState == CoreState.LISTENING) ArcxCyanGlow.copy(alpha = 0.25f)
                                    else ArcxCyan.copy(alpha = 0.15f),
                                    CircleShape
                                )
                                .border(1.5.dp, ArcxCyan, CircleShape)
                                .testTag("mic_trigger_button")
                        ) {
                            Icon(
                                imageVector = if (coreState == CoreState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Activate Voice Receptor",
                                tint = ArcxCyan
                            )
                        }
                    }
                }

                // Text Prompt Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text("Enter command or directive...", color = ArcxTextMuted, fontSize = 14.sp)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("prompt_text_field"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArcxCyan,
                            unfocusedBorderColor = ArcxHudBorder,
                            focusedTextColor = ArcxTextPrimary,
                            unfocusedTextColor = ArcxTextPrimary,
                            cursorColor = ArcxCyan
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (textInput.isNotBlank()) {
                                    val prompt = textInput
                                    textInput = ""
                                    viewModel.processUserDirective(prompt)
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                val prompt = textInput
                                textInput = ""
                                viewModel.processUserDirective(prompt)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(ArcxCyan.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, ArcxCyan, RoundedCornerShape(12.dp))
                            .testTag("send_prompt_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Prompt",
                            tint = ArcxCyan
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopHudBar(
    statusText: String,
    coreState: CoreState,
    autoSpeak: Boolean,
    onToggleAutoSpeak: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brand Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (coreState == CoreState.OFFLINE) ArcxTextMuted else ArcxNeonGreen,
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "ARC-X",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp,
                color = ArcxCyan
            )
            Text(
                text = " // NEURAL EXECUTIVE",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = ArcxTextSecondary
            )
        }

        // Voice TTS status toggle
        IconButton(
            onClick = onToggleAutoSpeak,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Toggle Spoken Voice",
                tint = if (autoSpeak) ArcxCyan else ArcxTextMuted
            )
        }
    }
}

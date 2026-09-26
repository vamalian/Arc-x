package com.example.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MessageEntity
import com.example.ui.theme.ArcxAlertRed
import com.example.ui.theme.ArcxCyan
import com.example.ui.theme.ArcxDeepSurface
import com.example.ui.theme.ArcxHudBorder
import com.example.ui.theme.ArcxSurface
import com.example.ui.theme.ArcxSurfaceLight
import com.example.ui.theme.ArcxTextMuted
import com.example.ui.theme.ArcxTextPrimary
import com.example.ui.theme.ArcxTextSecondary
import com.example.ui.theme.ArcxVoid
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: ArcxViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()
    var input by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Purge Conversation?", color = ArcxTextPrimary) },
            text = { Text("This will erase all active cognitive messages from local storage.", color = ArcxTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearConversation()
                        showClearDialog = false
                    }
                ) {
                    Text("Purge", color = ArcxAlertRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
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
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CONVERSATION STREAM",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = ArcxCyan
                )
                Text(
                    text = "${messages.size} Messages Synchronized",
                    fontSize = 11.sp,
                    color = ArcxTextSecondary
                )
            }

            IconButton(
                onClick = { showClearDialog = true },
                modifier = Modifier.testTag("clear_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Conversation",
                    tint = ArcxTextMuted
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    onSpeak = { text ->
                        viewModel.ttsManager.speak(
                            text = text,
                            pitch = viewModel.repository.voicePitch.value,
                            speed = viewModel.repository.voiceSpeed.value
                        )
                    },
                    onDelete = { viewModel.deleteMessage(message.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Command ARC-X...", color = ArcxTextMuted, fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArcxCyan,
                    unfocusedBorderColor = ArcxHudBorder,
                    focusedTextColor = ArcxTextPrimary,
                    unfocusedTextColor = ArcxTextPrimary,
                    cursorColor = ArcxCyan
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (input.isNotBlank()) {
                            val text = input
                            input = ""
                            viewModel.processUserDirective(text)
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (input.isNotBlank()) {
                        val text = input
                        input = ""
                        viewModel.processUserDirective(text)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(ArcxCyan.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .border(1.dp, ArcxCyan, RoundedCornerShape(12.dp))
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Message",
                    tint = ArcxCyan
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: MessageEntity,
    onSpeak: (String) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val isUser = message.sender == "USER"
    val timeStr = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.82f else 0.92f)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = if (isUser) ArcxCyan.copy(alpha = 0.5f) else ArcxHudBorder,
                    shape = RoundedCornerShape(12.dp)
                ),
            color = if (isUser) ArcxSurface else ArcxDeepSurface
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header row: sender + timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "EXECUTIVE" else "ARC-X NEURAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isUser) ArcxCyan else ArcxCyan
                    )
                    Text(
                        text = timeStr,
                        fontSize = 10.sp,
                        color = ArcxTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Content text
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ArcxTextPrimary,
                        lineHeight = 20.sp
                    )
                )

                // ARC-X Action Toolbar
                if (!isUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        // TTS read
                        IconButton(
                            onClick = { onSpeak(message.content) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speak aloud",
                                tint = ArcxTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Copy
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                                val clip = android.content.ClipData.newPlainText("ARC-X Output", message.content)
                                clipboard?.setPrimaryClip(clip)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = ArcxTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Delete
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete message",
                                tint = ArcxTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

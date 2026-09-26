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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcxCyan
import com.example.ui.theme.ArcxDeepSurface
import com.example.ui.theme.ArcxHudBorder
import com.example.ui.theme.ArcxSurface
import com.example.ui.theme.ArcxSurfaceLight
import com.example.ui.theme.ArcxTextMuted
import com.example.ui.theme.ArcxTextPrimary
import com.example.ui.theme.ArcxTextSecondary
import com.example.ui.theme.ArcxVoid

@Composable
fun ToolsScreen(
    viewModel: ArcxViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ArcxVoid)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "COMMAND MATRIX & TOOLS",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            color = ArcxCyan
        )
        Text(
            text = "Modular Subsystems & Android Integrations",
            fontSize = 12.sp,
            color = ArcxTextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Web Search Integration
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ArcxHudBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = ArcxDeepSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = ArcxCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quantum Web Query Matrix", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ArcxTextPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Query search engine...", fontSize = 13.sp, color = ArcxTextMuted) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tool_search_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArcxCyan,
                            unfocusedBorderColor = ArcxHudBorder,
                            focusedTextColor = ArcxTextPrimary,
                            unfocusedTextColor = ArcxTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                viewModel.processUserDirective("search $searchQuery")
                                searchQuery = ""
                            }
                        },
                        modifier = Modifier
                            .background(ArcxCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, ArcxCyan, RoundedCornerShape(8.dp))
                            .testTag("tool_search_execute")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Execute Search", tint = ArcxCyan)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Device Subsystem Launches
        Text(
            text = "APPLICATION LAUNCH MATRIX",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = ArcxCyan,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToolButton(
                icon = Icons.Default.VideoLibrary,
                label = "YouTube",
                modifier = Modifier.weight(1f),
                onClick = { viewModel.processUserDirective("open YouTube") }
            )
            ToolButton(
                icon = Icons.Default.Language,
                label = "Browser",
                modifier = Modifier.weight(1f),
                onClick = { viewModel.processUserDirective("open Chrome") }
            )
            ToolButton(
                icon = Icons.Default.Map,
                label = "Maps",
                modifier = Modifier.weight(1f),
                onClick = { viewModel.processUserDirective("open Maps") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToolButton(
                icon = Icons.Default.CameraAlt,
                label = "Camera",
                modifier = Modifier.weight(1f),
                onClick = { viewModel.processUserDirective("open Camera") }
            )
            ToolButton(
                icon = Icons.Default.Calculate,
                label = "Calculator",
                modifier = Modifier.weight(1f),
                onClick = { viewModel.processUserDirective("open Calculator") }
            )
            ToolButton(
                icon = Icons.Default.Alarm,
                label = "Clock / Alarm",
                modifier = Modifier.weight(1f),
                onClick = { viewModel.processUserDirective("set alarm") }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hardware Controls & Diagnostics
        Text(
            text = "HARDWARE SENSORS & CONTROLS",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = ArcxCyan,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToolButton(
                icon = Icons.Default.FlashlightOn,
                label = "Flashlight Toggle",
                modifier = Modifier.weight(1f),
                onClick = { viewModel.processUserDirective("flashlight") }
            )
            ToolButton(
                icon = Icons.Default.ContentPaste,
                label = "Read Clipboard",
                modifier = Modifier.weight(1f),
                onClick = { viewModel.processUserDirective("read clipboard") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToolButton(
                icon = Icons.Default.Security,
                label = "Run Diagnostic",
                modifier = Modifier.weight(1f),
                onClick = { viewModel.processUserDirective("system status") }
            )
            ToolButton(
                icon = Icons.Default.Settings,
                label = "Android Settings",
                modifier = Modifier.weight(1f),
                onClick = { viewModel.processUserDirective("open settings") }
            )
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .border(1.dp, ArcxHudBorder, RoundedCornerShape(10.dp)),
        color = ArcxSurface,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = ArcxCyan, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, color = ArcxTextPrimary, maxLines = 1)
        }
    }
}

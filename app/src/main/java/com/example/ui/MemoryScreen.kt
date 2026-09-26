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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MemoryEntity
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
fun MemoryScreen(
    viewModel: ArcxViewModel,
    modifier: Modifier = Modifier
) {
    val memories by viewModel.memories.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showPurgeDialog by remember { mutableStateOf(false) }

    val filtered = remember(memories, searchQuery) {
        if (searchQuery.isBlank()) memories
        else memories.filter {
            it.key.contains(searchQuery, ignoreCase = true) ||
            it.value.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    if (showAddDialog) {
        var keyInput by remember { mutableStateOf("") }
        var valueInput by remember { mutableStateOf("") }
        var categoryInput by remember { mutableStateOf("GENERAL") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Record New Memory", color = ArcxTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("Identifier / Title", color = ArcxTextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArcxCyan,
                            focusedTextColor = ArcxTextPrimary,
                            unfocusedTextColor = ArcxTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = valueInput,
                        onValueChange = { valueInput = it },
                        label = { Text("Memory Content / Fact", color = ArcxTextSecondary) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArcxCyan,
                            focusedTextColor = ArcxTextPrimary,
                            unfocusedTextColor = ArcxTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (keyInput.isNotBlank() && valueInput.isNotBlank()) {
                            viewModel.addMemory(keyInput, valueInput, categoryInput)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArcxCyan)
                ) {
                    Text("Store Memory", color = ArcxDeepSurface)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = ArcxTextPrimary)
                }
            },
            containerColor = ArcxDeepSurface
        )
    }

    if (showPurgeDialog) {
        AlertDialog(
            onDismissRequest = { showPurgeDialog = false },
            title = { Text("Purge All Memories?", color = ArcxTextPrimary) },
            text = { Text("All contextual facts and user records stored in ARC-X will be deleted.", color = ArcxTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllMemories()
                        showPurgeDialog = false
                    }
                ) {
                    Text("Purge", color = ArcxAlertRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurgeDialog = false }) {
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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NEURAL MEMORY BANK",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = ArcxCyan
                )
                Text(
                    text = "${memories.size} Directives & Facts Recorded",
                    fontSize = 11.sp,
                    color = ArcxTextSecondary
                )
            }

            Row {
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_memory_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Memory", tint = ArcxCyan)
                }

                if (memories.isNotEmpty()) {
                    IconButton(
                        onClick = { showPurgeDialog = true }
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Purge Memories", tint = ArcxTextMuted)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search recorded memories...", color = ArcxTextMuted, fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ArcxCyan) },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ArcxCyan,
                unfocusedBorderColor = ArcxHudBorder,
                focusedTextColor = ArcxTextPrimary,
                unfocusedTextColor = ArcxTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Memories list
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isBlank()) "Memory Bank is clear.\nSpeak or enter 'Remember that...' to commit facts." else "No records matching query.",
                    color = ArcxTextMuted,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { memory ->
                    MemoryItemCard(
                        memory = memory,
                        onDelete = { viewModel.deleteMemory(memory.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoryItemCard(
    memory: MemoryEntity,
    onDelete: () -> Unit
) {
    val dateStr = remember(memory.timestamp) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(memory.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ArcxHudBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = ArcxDeepSurface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = ArcxCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = memory.key,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ArcxTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = ArcxSurfaceLight,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = memory.category,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ArcxCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = memory.value,
                    fontSize = 13.sp,
                    color = ArcxTextSecondary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = ArcxTextMuted
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete memory",
                    tint = ArcxTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

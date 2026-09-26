package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "USER" or "ARCX"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSpoken: Boolean = false
)

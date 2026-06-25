package com.example.data

import java.util.UUID

enum class ChatChannel {
    GLOBAL,
    TEAM
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderName: String,
    val senderAvatar: String = "avatar_neon_cyber",
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val channel: ChatChannel,
    val isSystem: Boolean = false
)

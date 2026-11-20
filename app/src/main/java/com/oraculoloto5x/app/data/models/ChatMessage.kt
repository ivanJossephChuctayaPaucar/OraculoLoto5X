package com.oraculoloto5x.app.data.models

data class ChatMessage(
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val sender: String, // "user" o "oracle"
    val text: String,
    val relatedBalls: String = "" // Ej. "05-06-19-23-37-41"
) {
    val isFromUser: Boolean
        get() = sender == "user"
}

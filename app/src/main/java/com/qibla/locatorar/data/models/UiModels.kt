package com.qibla.locatorar.data.models

data class PrayerUiState(
    val cached: PrayerResponse? = null,
    val isLoading: Boolean = false,
    val message: String = "Allow location to load prayer times.",
    val locationText: String = ""
)

data class ChatMessage(
    val text: String,
    val fromUser: Boolean
)

enum class AppTab(val title: String) {
    Prayer("Prayer"),
    Qibla("Qibla"),
    Zakat("Zakat"),
    Chat("Chat")
}


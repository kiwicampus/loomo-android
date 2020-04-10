package com.kiwicampus.loomo.models.freedom_message_history

data class Message(
    val account: String,
    val `data`: Any,
    val device: String,
    val expiration_secs: Int,
    val id: String,
    val platform: String,
    val topic: String,
    val type: String,
    val utc_time: Double,
    val utc_time_api_received: Double
)
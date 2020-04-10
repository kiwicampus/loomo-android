package com.kiwicampus.loomo.models.freedom_command

data class FreedomCommand(
    val account: String,
    val age: Double,
    val device: String,
    val expiration_secs: Int,
    val message: Message,
    val platform: String,
    val repeat: Repeat,
    val topic: String,
    val type: String,
    val uid: String,
    val utc_time: Double
)
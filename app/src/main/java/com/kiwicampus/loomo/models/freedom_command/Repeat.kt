package com.kiwicampus.loomo.models.freedom_command

data class Repeat(
    val cancel_on_next_message: Boolean,
    val length: Double,
    val rate: Int
)
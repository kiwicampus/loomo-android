package com.kiwicampus.loomo.models.freedom_message_history

data class FreedomMessagesReceived(
    val is_paginated: Boolean,
    val messages: List<Message>,
    val pagination_direction: String,
    val requested_num_messages: Int,
    val requested_utc_end: Double,
    val requested_utc_start: Double,
    val returned_num_messages: Int,
    val returned_utc_end: Double,
    val returned_utc_start: Double
)
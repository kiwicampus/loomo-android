package com.kiwicampus.loomo.models

data class CloudMessageResponse(
    val account: String,
    val action: String,
    val device: String,
    val execution_time: Double,
    val max_message_age: Double,
    val min_message_age: Double,
    val num_data: String,
    val num_video_frames: Int,
    val status: String
)
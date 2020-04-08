package com.kiwicampus.loomo.models

import com.google.gson.annotations.SerializedName
import com.soywiz.klock.DateTime

data class FreedomMessage(
    val platform: String = "custom",
    @SerializedName("utc_time") val utcTime: Long = DateTime.nowUnixLong() / 1000,
    @SerializedName("expiration_secs") val expirationSecs: Int=60,
    val topic: String,
    val type: String,
    val data: Any
)
package com.kiwicampus.loomo.models

import com.google.gson.annotations.SerializedName

data class CloudMessage(
    val platform: String = "custom",
    @SerializedName("utc_time") val utcTime: Long,
    val topic: String,
    val type: String,
    @SerializedName("expiration_secs") val expirationSecs: Int,
    val data: LocationData // Object to string using Gson
)
package com.kiwicampus.loomo.models.ros_objects

import com.google.gson.annotations.SerializedName

data class Image(
    val height: Int? = null,
    val width: Int? = null,
    val encoding: String? = null,
    val step: Int? = null,
    @SerializedName("is_bigendian") val isBigEndian: Int = 0, // bool false
//    @Suppress("ArrayInDataClass") val data: ByteArray
    val data: List<Int>

)
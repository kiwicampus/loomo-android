package com.kiwicampus.loomo.models.ros_objects

data class Image(
    val height: Int? = null,
    val width: Int? = null,
    val encoding: String?= null,
    val step: Int?= null,
    @Suppress("ArrayInDataClass") val data: ByteArray
)
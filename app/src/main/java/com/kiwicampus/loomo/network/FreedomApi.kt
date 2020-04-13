package com.kiwicampus.loomo.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.kiwicampus.loomo.models.FreedomMessage
import com.kiwicampus.loomo.models.FreedomResponse
import com.kiwicampus.loomo.models.freedom_command.FreedomCommand
import com.kiwicampus.loomo.models.freedom_message_history.FreedomMessagesReceived
import com.kiwicampus.loomo.utils.Constants
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT

val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl(Constants.FREEDOM_BASE_URL)
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
//    .client(OkHttpClient.Builder().addInterceptor(run {
//        val httpLoggingInterceptor = HttpLoggingInterceptor()
//        httpLoggingInterceptor.apply { level = HttpLoggingInterceptor.Level.BODY }
//    }).build())
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface FreedomProvider {
    @PUT("accounts/${Constants.FREEDOM_ACCOUNT}/devices/${Constants.FREEDOM_DEVICE_DEV}/data")
    fun sendMessageToFreedomAsync(
        @Header("mc_token") token: String,
        @Header("mc_secret") secret: String,
        @Body message: List<FreedomMessage>
    ): Deferred<FreedomResponse>

    // Retrieve messages sent from the device
    @GET("accounts/${Constants.FREEDOM_ACCOUNT}/devices/${Constants.FREEDOM_DEVICE_DEV}/data?utc_start=-2m&pagination=true")
    fun getDeviceMessagesHistoryAsync(
        @Header("mc_token") token: String,
        @Header("mc_secret") secret: String
    ): Deferred<FreedomMessagesReceived>

    @GET("accounts/${Constants.FREEDOM_ACCOUNT}/devices/${Constants.FREEDOM_DEVICE_DEV}/commands")
    fun getCommandsAsync(
        @Header("mc_token") token: String,
        @Header("mc_secret") secret: String
    ): Deferred<List<FreedomCommand>>
}

object FreedomApi {
    val retrofitService: FreedomProvider by lazy { retrofit.create(FreedomProvider::class.java) }
}


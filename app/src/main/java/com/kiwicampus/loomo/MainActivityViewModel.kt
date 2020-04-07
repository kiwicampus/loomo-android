package com.kiwicampus.loomo

import androidx.lifecycle.ViewModel
import com.kiwicampus.loomo.models.CloudMessage
import com.kiwicampus.loomo.models.LocationData
import com.kiwicampus.loomo.network.FreedomApi
import com.kiwicampus.loomo.utils.Constants
import com.soywiz.klock.DateTime
import kotlinx.coroutines.*
import timber.log.Timber
import java.text.DateFormat

class MainActivityViewModel : ViewModel() {
    private val jobViewModel: Job = Job()
    private val uiScope: CoroutineScope = CoroutineScope(Dispatchers.Main + jobViewModel)

    init {
        uiScope.launch {
            sendMessage()
        }
    }

    private suspend fun sendMessage() {
        withContext(Dispatchers.Main) {
            val response = FreedomApi.retrofitService.sendMessageToCloudAsync(
                Constants.FREEDOM_TOKEN,
                Constants.FREEDOM_SECRET,
                listOf(
                    CloudMessage(
                        utcTime = DateTime.nowUnixLong()/1000,
                        topic = "/location",
                        expirationSecs = 60,
                        type = "sensor_msgs/NavSatFix",
                        data = LocationData(latitude = 37.778454, longitude = -122.389171)
                    )
                )
            )

            try {
                Timber.d(response.await().toString())
            } catch (e: Exception) {
                Timber.e(e)
            }

        }
    }
}
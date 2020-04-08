package com.kiwicampus.loomo.viewmodels

import androidx.lifecycle.ViewModel
import com.kiwicampus.loomo.models.FreedomMessage
import com.kiwicampus.loomo.models.ros_objects.Image
import com.kiwicampus.loomo.models.ros_objects.NavSatFix
import com.kiwicampus.loomo.network.FreedomApi
import com.kiwicampus.loomo.utils.Constants
import kotlinx.coroutines.*
import timber.log.Timber

class MainActivityViewModel : ViewModel() {
    private val jobViewModel: Job = Job()
    private val uiScope: CoroutineScope = CoroutineScope(Dispatchers.Main + jobViewModel)

    fun updateLocation(latitude: Double, longitude: Double) {
        uiScope.launch {
            sendFreedomMessage(
                "/location",
                "sensor_msgs/NavSatFix",
                NavSatFix(
                    latitude = latitude,
                    longitude = longitude
                )
            )
        }
    }

    fun updateVideoImage(bytesImage: ByteArray) {
        uiScope.launch {
            sendFreedomMessage(
                "/video",
                "sensor_msgs/Image",
                Image(data = bytesImage)
            )
        }
    }

    private suspend fun sendFreedomMessage(topic: String, type: String, data: Any) {
        withContext(Dispatchers.Main) {
            val response = FreedomApi.retrofitService.sendMessageToFreedomAsync(
                Constants.FREEDOM_TOKEN,
                Constants.FREEDOM_SECRET,
                listOf(
                    FreedomMessage(
                        topic = topic,
                        type = type,
                        data = data
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
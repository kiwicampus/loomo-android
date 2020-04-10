package com.kiwicampus.loomo.viewmodels

import android.os.Handler
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

    private val handler = Handler()
    private var runnableCommands: Runnable? = null

    init {
        observeDeviceCommands()
    }

    private fun observeDeviceCommands() {
        if (runnableCommands == null) {
            runnableCommands = object : Runnable {
                override fun run() {
                    uiScope.launch {
                        getDeviceCommands()
                    }
                    handler.postDelayed(this, 10000)
                }
            }
            runnableCommands?.run()
        }
    }

    private suspend fun getDeviceCommands() {
        withContext(Dispatchers.Main) {
            val response = FreedomApi.retrofitService.getCommandsAsync(
                Constants.FREEDOM_TOKEN,
                Constants.FREEDOM_SECRET
            )
            try {
                val commands = response.await()
                if (commands.isNotEmpty()){
                    Timber.d("Vϴ: ${commands[commands.size - 1].message.angular} , V: ${commands[commands.size - 1].message.linear}")
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

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

    fun updateVideoImage(bytesImage: ByteArray, height: Int, width: Int, step: Int) {
        val image = Image(
            data = bytesImage, step = step, height = height, width = width, encoding = "rgb16"
        )
        Timber.d(image.toString())
        uiScope.launch { sendFreedomMessage("/video", "sensor_msgs/Image", image) }
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
                response.await()
//                Timber.d(response.await().toString())
            } catch (e: Exception) {
                Timber.e(e)
            }

        }
    }

    private suspend fun getDeviceSentMessagesHistory() {
        withContext(Dispatchers.Main) {
            val response = FreedomApi.retrofitService.getDeviceMessagesHistoryAsync(
                Constants.FREEDOM_TOKEN,
                Constants.FREEDOM_SECRET
            )
            try {
                val messagesResponse = response.await()

            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }


    fun startRunnables() = runnableCommands?.let { runnableCommands?.run() }

    fun stopRunnables() = runnableCommands?.let { handler.removeCallbacks(it) }

    override fun onCleared() {
        super.onCleared()
        jobViewModel.cancel()
        stopRunnables()
    }

}
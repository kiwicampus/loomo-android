package com.kiwicampus.loomo.viewmodels

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kiwicampus.loomo.models.FreedomMessage
import com.kiwicampus.loomo.models.freedom_command.FreedomCommand
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
    private var runnableDefaultLocation: Runnable? = null

    init {
        observeDeviceCommands()
        sendDemoLocationPeriodically()
    }

    private var commandsHistory = mutableListOf<Double>()

    private val _currentCommand = MutableLiveData<FreedomCommand>()
    val currentCommand: LiveData<FreedomCommand>
        get() = _currentCommand

    private fun observeDeviceCommands() {
        if (runnableCommands == null) {
            runnableCommands = object : Runnable {
                override fun run() {
                    uiScope.launch {
                        getDeviceCommands()
                    }
                    handler.postDelayed(this, 100)
                }
            }
            runnableCommands?.run()
        }
    }

    private fun sendDemoLocationPeriodically() {
        if (runnableDefaultLocation == null) {
            runnableDefaultLocation = object : Runnable {
                override fun run() {
                    uiScope.launch {
                        updateLocation(4.144230, -73.634529)
                    }
                    handler.postDelayed(this, 1000)
                }
            }
            runnableDefaultLocation?.run()
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
                commands.forEach { command ->
                    if (!commandsHistory.contains(command.utc_time)) {
                        _currentCommand.value = command
                        commandsHistory.add(command.utc_time)
                    }
                }
                if (commandsHistory.size > 100) {
                    commandsHistory = commandsHistory.subList(90, commandsHistory.size)
                }
            } catch (e: Exception) {
                Timber.d("Exception getting device commands ... Stopping runnables")
                stopRunnables()
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
            data = bytesImage, step = step, height = height, width = width, encoding = "bgr8"
        )
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
            } catch (e: Exception) {
                Timber.d("Exception sending freedom message ... Stopping runnables")
                stopRunnables()
                Timber.e(e)
            }

        }
    }

//    private fun startRunnables() {
//        runnableDefaultLocation?.run()
//        runnableCommands?.run()
//    }

    private fun stopRunnables() {
        runnableDefaultLocation?.let { handler.removeCallbacks(it) }
        runnableCommands?.let { handler.removeCallbacks(it) }
    }

    override fun onCleared() {
        super.onCleared()
        jobViewModel.cancel()
        stopRunnables()
    }

}
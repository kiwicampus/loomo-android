package com.kiwicampus.loomo.views

import android.view.Surface
import com.opentok.android.BaseVideoCapturer
import com.segway.robot.sdk.vision.Vision
import com.segway.robot.sdk.vision.stream.StreamType
import timber.log.Timber

class LoomoVideoCapturer(private val loomoVision: Vision) : BaseVideoCapturer() {
    private val FPS = 15

    private var mCapturerHasStarted = false
    private var capturerIsPaused = false
    private lateinit var mCapturerSettings: CaptureSettings
    private val mWidth = 640
    private val mHeight = 480


    override fun init() {
        mCapturerHasStarted = false
        capturerIsPaused = false

        mCapturerSettings = CaptureSettings()
        mCapturerSettings.height = mHeight
        mCapturerSettings.width = mWidth
        mCapturerSettings.format = ARGB
        mCapturerSettings.fps = FPS
        mCapturerSettings.expectedDelay = 0
    }

    private fun initLoomoVision() {
        loomoVision.startListenFrame(StreamType.COLOR) { streamType, frame ->
//            frame.info.
            // send frame.byteBuffer
            Timber.d("Stream Type: $streamType Resolution: ${frame.info.resolution} Pixel Format: ${frame.info.pixelFormat}")
            try {
                provideBufferFrame(
                    frame.byteBuffer,
                    ARGB,
                    mWidth,
                    mHeight,
                    Surface.ROTATION_0,
                    false
                )
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }


    override fun startCapture(): Int {
        mCapturerHasStarted = true
        initLoomoVision()
        return 0
    }

    override fun isCaptureStarted(): Boolean {
        return mCapturerHasStarted
    }

    override fun onResume() {
        capturerIsPaused = false
    }

    override fun onPause() {
        capturerIsPaused = true
        loomoVision.stopListenFrame(StreamType.COLOR)
    }

    override fun stopCapture(): Int {
        mCapturerHasStarted = false
        return 0
    }

    override fun destroy() {
    }

    override fun getCaptureSettings(): CaptureSettings {
        return mCapturerSettings
    }

}
package com.kiwicampus.loomo.views

import android.view.Surface
import com.opentok.android.BaseVideoCapturer
import com.segway.robot.sdk.vision.Vision
import com.segway.robot.sdk.vision.stream.StreamType
import timber.log.Timber

class LoomoVideoCapturer(private val loomoVision: Vision) : BaseVideoCapturer() {

    private var mCapturerHasStarted = false
    private var capturerIsPaused = false
    private lateinit var mCapturerSettings: CaptureSettings
    private val mWidth = 640 // TODO TEST 320 x 240
    private val mHeight = 480


    override fun init() {
        mCapturerHasStarted = false
        capturerIsPaused = false

        mCapturerSettings = CaptureSettings()
        mCapturerSettings.height = mHeight
        mCapturerSettings.width = mWidth
        mCapturerSettings.format = BaseVideoCapturer.ARGB // TODO test bytes formats
        // Test YUY2 15, 30 ,60
        // https://www.intel.com/content/dam/support/us/en/documents/emerging-technologies/intel-realsense-technology/ZR300-Product-Datasheet-Public.pdf
        mCapturerSettings.fps = 15 // TODO test from Intel RealSense ZR300 docs
        mCapturerSettings.expectedDelay = 0
    }

    private fun initLoomoVision() {
        loomoVision.startListenFrame(StreamType.COLOR) { streamType, frame ->
            Timber.d("Stream Type: $streamType Resolution: ${frame.info.resolution} Pixel Format: ${frame.info.pixelFormat}")
            try {
                // TODO test first to bitmap before providing buffer
//                val bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
//                val stream = ByteArrayOutputStream()
//                bitmap.copyPixelsFromBuffer(frame.byteBuffer)
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
//                val byteArray = stream.toByteArray()
//                provideByteArrayFrame(byteArray, ARGB, mWidth, mHeight, Surface.ROTATION_0, false)
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
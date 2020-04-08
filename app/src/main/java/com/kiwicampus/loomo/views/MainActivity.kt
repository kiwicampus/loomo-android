package com.kiwicampus.loomo.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.kiwicampus.loomo.R
import com.kiwicampus.loomo.databinding.ActivityMainBinding
import com.kiwicampus.loomo.viewmodels.MainActivityViewModel
import com.segway.robot.algo.Pose2D
import com.segway.robot.algo.minicontroller.CheckPoint
import com.segway.robot.algo.minicontroller.CheckPointStateListener
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.locomotion.sbv.Base
import com.segway.robot.sdk.vision.Vision
import com.segway.robot.sdk.vision.stream.StreamType
import timber.log.Timber
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var loomoBase: Base
    private lateinit var loomoVision: Vision
    private val visionBitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        initLoomo()
        setupViewModel()
        setupLoomoService()
        setClickListeners()
        setupPermissions()
//        val bitmap = (binding.ivTestImage.drawable as BitmapDrawable).bitmap
    }

    private fun initLoomo() {
        loomoBase = Base.getInstance()
        loomoVision = Vision.getInstance()
    }

    private fun setupViewModel() {
        @Suppress("DEPRECATION")
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
    }

    @SuppressLint("MissingPermission")
    private fun setupLocationListener() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000,
            0.1f,
            object : LocationListener {
                override fun onLocationChanged(location: Location?) {
                    Timber.d("Location lat: ${location?.latitude} lon: ${location?.longitude}")
                    location?.let {
                        viewModel.updateLocation(it.latitude, it.longitude)
                    }
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                }

                override fun onProviderEnabled(provider: String?) {
                }

                override fun onProviderDisabled(provider: String?) {
                }

            })
    }

    private fun setupLoomoService() {
        val loomoBindService = object : ServiceBinder.BindStateListener {
            override fun onUnbind(reason: String?) {
                loomoBase.setOnCheckPointArrivedListener(object : CheckPointStateListener {
                    override fun onCheckPointMiss(
                        checkPoint: CheckPoint?,
                        realPose: Pose2D?,
                        isLast: Boolean,
                        reason: Int
                    ) {
                        Timber.d("**Missed** Real pose: ${realPose.toString()} -- Checkpoint ${checkPoint.toString()}")
                    }

                    override fun onCheckPointArrived(
                        checkPoint: CheckPoint?,
                        realPose: Pose2D?,
                        isLast: Boolean
                    ) {
                        Timber.d("Real pose: ${realPose.toString()} -- Checkpoint ${checkPoint.toString()}")
                    }
                })
            }

            override fun onBind() {
            }
        }
        loomoBase.bindService(this, loomoBindService)
        loomoVision.bindService(this, loomoBindService)
        val infos = loomoVision.activatedStreamInfo

        loomoVision.startListenFrame(StreamType.DEPTH) { streamType, frame ->
            // send frame.byteBuffer
            Timber.d("Stream Type: $streamType Resolution: ${frame.info.resolution} Pixel Format: ${frame.info.pixelFormat}")
            visionBitmap.copyPixelsFromBuffer(frame.byteBuffer)
            viewModel.updateVideoImage(visionBitmapToBytes())
        }
    }

    private fun visionBitmapToBytes(): ByteArray {
        val stream = ByteArrayOutputStream()
        visionBitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        return stream.toByteArray()
    }

    private fun cleanLoomoPose() {
        // clean loomo position
        loomoBase.cleanOriginalPoint()
        // get actual loomo position
        val pose = loomoBase.getOdometryPose(-1)
        // set initial position to loomo
        loomoBase.setOriginalPoint(pose)
    }

    private fun initLoomoNavigation() {
        loomoBase.controlMode = Base.CONTROL_MODE_NAVIGATION
        cleanLoomoPose()
    }

    private fun setClickListeners() {
        binding.btnTest.setOnClickListener {
            initLoomoNavigation()
            // works like a coordinate system
            // starts in 0, 0
            // x coordinate --> vertically
            // y coordinate --> horizontally
            // 1f, 0f means 1 meter front 0 horizontally
            loomoBase.addCheckPoint(1f, 0f)
            // 1f, 1f means 1 meter front (already taken) 1 horizontally,
            loomoBase.addCheckPoint(1f, 1f, (Math.PI / 2).toFloat())
        }
        binding.btnTest2.setOnClickListener {
            initLoomoNavigation()
            loomoBase.addCheckPoint(1f, 0f, (2 * Math.PI).toFloat())
        }
        binding.btnTest3.setOnClickListener {
            initLoomoNavigation()
            loomoBase.addCheckPoint(1f, 0f)
            loomoBase.addCheckPoint(1f, 0.5f, (-Math.PI).toFloat())
            loomoBase.addCheckPoint(2f, 0.5f, (2 * Math.PI).toFloat())
        }
        binding.btnTest4.setOnClickListener {
            initLoomoNavigation()
            loomoBase.addCheckPoint(1f, 0f)
            loomoBase.addCheckPoint(1f, 1f)
            loomoBase.addCheckPoint(0f, 1f)
            loomoBase.addCheckPoint(0f, 0f)
        }
        binding.btnTest5.setOnClickListener {
            initLoomoNavigation()
            loomoBase.addCheckPoint(0f, 0f, (Math.PI / 2).toFloat())
        }
        binding.btnTest6.setOnClickListener {
            initLoomoNavigation()
            loomoBase.addCheckPoint(0f, 0f, (-Math.PI / 2).toFloat())
        }
        binding.btnTest7.setOnClickListener {
            initLoomoNavigation()
            loomoBase.addCheckPoint(0f, 0f, (Math.PI / 4).toFloat())
            loomoBase.addCheckPoint(0f, 0f, (Math.PI / 2).toFloat())
            loomoBase.addCheckPoint(0f, 0f, ((3 * Math.PI) / 4).toFloat())
            loomoBase.addCheckPoint(0f, 0f, (Math.PI).toFloat())
            loomoBase.addCheckPoint(0f, 0f, ((5 * Math.PI) / 4).toFloat())
            loomoBase.addCheckPoint(0f, 0f, ((3 * Math.PI) / 2).toFloat())
            loomoBase.addCheckPoint(0f, 0f, ((7 * Math.PI) / 4).toFloat())
            loomoBase.addCheckPoint(0f, 0f, (2 * Math.PI).toFloat())
        }
        binding.btnTest8.setOnClickListener {
            loomoBase.controlMode = Base.CONTROL_MODE_RAW
            cleanLoomoPose()
            Timber.d("Initial velocities Linear ${loomoBase.linearVelocity} with a limit ${loomoBase.linearVelocityLimit}")
            Timber.d("Angular velocitiy ${loomoBase.angularVelocity} with a limit ${loomoBase.angularVelocityLimit}")
            loomoBase.setLinearVelocity(3f)
            loomoBase.addCheckPoint(1f, 0f, (Math.PI).toFloat())
            loomoBase.setAngularVelocity(3f)
            loomoBase.addCheckPoint(1f, 1f, (Math.PI).toFloat())
            Timber.d("🔚Final velocities Linear ${loomoBase.linearVelocity} ")
            Timber.d("Final Angular velocity ${loomoBase.angularVelocity}")
        }
    }


    private fun setupPermissions() {
        Dexter.withContext(this).withPermissions(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                report?.let {
                    if (report.areAllPermissionsGranted()) setupLocationListener()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
            }

        }).withErrorListener {
            Toast.makeText(this@MainActivity, it.name, Toast.LENGTH_SHORT).show()
            Timber.e(it.name)
        }.check()
    }
}

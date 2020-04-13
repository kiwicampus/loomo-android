package com.kiwicampus.loomo.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.kiwicampus.loomo.R
import com.kiwicampus.loomo.databinding.ActivityMainBinding
import com.kiwicampus.loomo.viewmodels.MainActivityViewModel
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.locomotion.sbv.Base
import com.segway.robot.sdk.vision.Vision
import timber.log.Timber
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var loomoBase: Base
    private lateinit var loomoVision: Vision

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        initLoomo()
        setupViewModel()
//        setupPermissions()

        binding.btnTestVision.setOnClickListener {
            loomoBase.controlMode = Base.CONTROL_MODE_RAW
        }
    }

    private fun initLoomo() {
        loomoBase = Base.getInstance()
        loomoBase.bindService(this, object : ServiceBinder.BindStateListener {
            override fun onUnbind(reason: String?) {

            }

            override fun onBind() {

            }
        })
    }

    private fun setupViewModel() {
        @Suppress("DEPRECATION")
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        viewModel.currentCommand.observe(this, Observer {
            @Suppress("UNNECESSARY_SAFE_CALL")
            val linearVelocity = it?.message?.linear?.x ?: 0.0f
            val angularVelocity = it?.message?.angular?.z ?: 0.0f
            binding.tvLinear.text = "$linearVelocity"
            binding.tvAngular.text = "$angularVelocity"
            Timber.d("V: $linearVelocity ϴ: $angularVelocity")

            loomoBase.setLinearVelocity(linearVelocity)
            loomoBase.setAngularVelocity(angularVelocity)
        })
    }

    private fun initLoomoVision() {
        val bitmap = (binding.ivTest.drawable as BitmapDrawable).bitmap
//        Timber.d("${bitmap.config} ${bitmap.copy(Bitmap.Config.RGB, true)}")
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        Timber.d("Height:${bitmap.height} Width:${bitmap.width} Byte count:${bitmap.byteCount} Row bytes:${bitmap.rowBytes}")
        val byteArray = stream.toByteArray()
        Timber.d("Byte array size: ${byteArray.size}")
//        Timber.d("${BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)}")
//        viewModel.updateVideoImage(byteArray, bitmap.height, bitmap.width, bitmap.rowBytes)
//        val infos = loomoVision.activatedStreamInfo
//        Toast.makeText(this, "Loomo vision initiated", Toast.LENGTH_SHORT).show()
//        Timber.d("$infos")
//        loomoVision.startListenFrame(StreamType.COLOR) { streamType, frame ->
//            // send frame.byteBuffer
//            Timber.d("Stream Type: $streamType Resolution: ${frame.info.resolution} Pixel Format: ${frame.info.pixelFormat}")
//            try {
//                val bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)
//                val stream = ByteArrayOutputStream()
//                bitmap.copyPixelsFromBuffer(frame.byteBuffer)
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
//                val byteArray = stream.toByteArray()
//                Timber.d("Byte array length ${byteArray.size}")
//                viewModel.updateVideoImage(byteArray)
//            } catch (e: Exception) {
//                Timber.e(e)
//            }
//        }
    }

    private fun setupPermissions() {
        Dexter.withContext(this).withPermissions(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                Timber.d("Asking for permissions $report")
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


    @SuppressLint("MissingPermission")
    private fun setupLocationListener() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000,
            0.1f,
            object : LocationListener {
                override fun onLocationChanged(location: Location?) {
//                    Timber.d("Location lat: ${location?.latitude} lon: ${location?.longitude}")
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


//    private fun cleanLoomoPose() {
//        // clean loomo position
//        loomoBase.cleanOriginalPoint()
//        // get actual loomo position
//        val pose = loomoBase.getOdometryPose(-1)
//        // set initial position to loomo
//        loomoBase.setOriginalPoint(pose)
//    }
//
//    private fun initLoomoNavigation() {
//        loomoBase.controlMode = Base.CONTROL_MODE_NAVIGATION
//        cleanLoomoPose()
//    }

//    private fun setClickListeners() {
//        binding.btnTest.setOnClickListener {
//            initLoomoNavigation()
//            // works like a coordinate system
//            // starts in 0, 0
//            // x coordinate --> vertically
//            // y coordinate --> horizontally
//            // 1f, 0f means 1 meter front 0 horizontally
//            loomoBase.addCheckPoint(1f, 0f)
//            // 1f, 1f means 1 meter front (already taken) 1 horizontally,
//            loomoBase.addCheckPoint(1f, 1f, (Math.PI / 2).toFloat())
//        }
//        binding.btnTest2.setOnClickListener {
//            initLoomoNavigation()
//            loomoBase.addCheckPoint(1f, 0f, (2 * Math.PI).toFloat())
//        }
//        binding.btnTest3.setOnClickListener {
//            initLoomoNavigation()
//            loomoBase.addCheckPoint(1f, 0f)
//            loomoBase.addCheckPoint(1f, 0.5f, (-Math.PI).toFloat())
//            loomoBase.addCheckPoint(2f, 0.5f, (2 * Math.PI).toFloat())
//        }
//        binding.btnTest4.setOnClickListener {
//            initLoomoNavigation()
//            loomoBase.addCheckPoint(1f, 0f)
//            loomoBase.addCheckPoint(1f, 1f)
//            loomoBase.addCheckPoint(0f, 1f)
//            loomoBase.addCheckPoint(0f, 0f)
//        }
//        binding.btnTest5.setOnClickListener {
//            initLoomoNavigation()
//            loomoBase.addCheckPoint(0f, 0f, (Math.PI / 2).toFloat())
//        }
//        binding.btnTest6.setOnClickListener {
//            initLoomoNavigation()
//            loomoBase.addCheckPoint(0f, 0f, (-Math.PI / 2).toFloat())
//        }
//        binding.btnTest7.setOnClickListener {
//            initLoomoNavigation()
//            loomoBase.addCheckPoint(0f, 0f, (Math.PI / 4).toFloat())
//            loomoBase.addCheckPoint(0f, 0f, (Math.PI / 2).toFloat())
//            loomoBase.addCheckPoint(0f, 0f, ((3 * Math.PI) / 4).toFloat())
//            loomoBase.addCheckPoint(0f, 0f, (Math.PI).toFloat())
//            loomoBase.addCheckPoint(0f, 0f, ((5 * Math.PI) / 4).toFloat())
//            loomoBase.addCheckPoint(0f, 0f, ((3 * Math.PI) / 2).toFloat())
//            loomoBase.addCheckPoint(0f, 0f, ((7 * Math.PI) / 4).toFloat())
//            loomoBase.addCheckPoint(0f, 0f, (2 * Math.PI).toFloat())
//        }
//        binding.btnTest8.setOnClickListener {
//            loomoBase.controlMode = Base.CONTROL_MODE_RAW
//            cleanLoomoPose()
//            Timber.d("Initial velocities Linear ${loomoBase.linearVelocity} with a limit ${loomoBase.linearVelocityLimit}")
//            Timber.d("Angular velocitiy ${loomoBase.angularVelocity} with a limit ${loomoBase.angularVelocityLimit}")
//            loomoBase.setLinearVelocity(3f)
//            loomoBase.addCheckPoint(1f, 0f, (Math.PI).toFloat())
//            loomoBase.setAngularVelocity(3f)
//            loomoBase.addCheckPoint(1f, 1f, (Math.PI).toFloat())
//            Timber.d("🔚Final velocities Linear ${loomoBase.linearVelocity} ")
//            Timber.d("Final Angular velocity ${loomoBase.angularVelocity}")
//        }
//
//    }

}

package com.kiwicampus.loomo.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.opengl.GLSurfaceView
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
import com.kiwicampus.loomo.utils.Constants.Companion.RC_VIDEO_APP_PERM
import com.kiwicampus.loomo.utils.Constants.Companion.TOKBOX_API_KEY
import com.kiwicampus.loomo.utils.Constants.Companion.TOKBOX_SESSION_ID
import com.kiwicampus.loomo.utils.Constants.Companion.TOKBOX_TOKEN
import com.kiwicampus.loomo.viewmodels.MainActivityViewModel
import com.opentok.android.*
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.locomotion.head.Head
import com.segway.robot.sdk.locomotion.sbv.Base
import com.segway.robot.sdk.vision.Vision
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

class MainActivity : AppCompatActivity(), Session.SessionListener, PublisherKit.PublisherListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel

    private lateinit var loomoBase: Base
    private lateinit var loomoVision: Vision
    private lateinit var loomoHead: Head

    private lateinit var tokboxSession: Session
    private lateinit var tokboxPublisher: Publisher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        initLoomo()
        setupViewModel()
        setupPermissions()
        binding.btnStartPublishing.setOnClickListener {
            setupLoomoHead()
            Timber.d("Stream Info init ${loomoVision.activatedStreamInfo}") // call required
            tokboxPublisher.capturer = LoomoVideoCapturer(loomoVision)
            binding.publisherContainer.addView(tokboxPublisher.view)
            if (tokboxPublisher.view is GLSurfaceView) {
                (tokboxPublisher.view as GLSurfaceView).setZOrderOnTop(true)
            }
            tokboxSession.publish(tokboxPublisher)
        }
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

    private fun setupPermissions() {
        setupLocationPermission()
        setupTokBoxPermissions()
    }

    private fun initTokbox() {
        tokboxSession = Session.Builder(this, TOKBOX_API_KEY, TOKBOX_SESSION_ID).build()
        tokboxSession.setSessionListener(this)
        tokboxPublisher = Publisher.Builder(this).build()
        tokboxPublisher.setPublisherListener(this)
        tokboxSession.connect(TOKBOX_TOKEN)
    }

    private fun setupLoomoHead(){
        loomoHead.mode = Head.MODE_ORIENTATION_LOCK
        loomoHead.setYawAngularVelocity(0f) // horizontal
        loomoHead.setPitchAngularVelocity(0f) // vertical TODO test maybe some -15
    }

    override fun onConnected(p0: Session?) {
//        binding.publisherContainer.addView(tokboxPublisher.view)
//        if (tokboxPublisher.view is GLSurfaceView) {
//            (tokboxPublisher.view as GLSurfaceView).setZOrderOnTop(true)
//        }
//        tokboxSession.publish(tokboxPublisher)
    }

//    private fun initLoomoVision() {
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
//    }

    private fun initLoomo() {
        loomoBase = Base.getInstance()
        loomoBase.bindService(this, object : ServiceBinder.BindStateListener {
            override fun onUnbind(reason: String?) {

            }

            override fun onBind() {

            }
        })
        loomoVision = Vision.getInstance()
        loomoVision.bindService(this, object : ServiceBinder.BindStateListener {
            override fun onUnbind(reason: String?) {

            }

            override fun onBind() {

            }
        })
        loomoHead = Head.getInstance()
        loomoHead.bindService(this, object : ServiceBinder.BindStateListener {
            override fun onUnbind(reason: String?) {

            }

            override fun onBind() {

            }
        })
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private fun setupTokBoxPermissions() {
        val perms = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        if (EasyPermissions.hasPermissions(this, *perms)) {
            initTokbox()
        } else {
            val message = "This app needs access to your camera and mic to make video transmission"
            EasyPermissions.requestPermissions(this, message, RC_VIDEO_APP_PERM, *perms)
        }
    }


    private fun setupLocationPermission() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
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

    override fun onStreamDropped(p0: Session?, p1: Stream?) {

    }

    override fun onStreamReceived(p0: Session?, p1: Stream?) {
    }


    override fun onDisconnected(p0: Session?) {
    }

    override fun onError(p0: Session?, p1: OpentokError?) {
    }

    override fun onStreamCreated(p0: PublisherKit?, p1: Stream?) {
    }

    override fun onStreamDestroyed(p0: PublisherKit?, p1: Stream?) {
    }

    override fun onError(p0: PublisherKit?, p1: OpentokError?) {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


}

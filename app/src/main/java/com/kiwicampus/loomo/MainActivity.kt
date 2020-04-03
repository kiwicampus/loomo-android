package com.kiwicampus.loomo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.kiwicampus.loomo.databinding.ActivityMainBinding
import com.segway.robot.algo.Pose2D
import com.segway.robot.algo.minicontroller.CheckPoint
import com.segway.robot.algo.minicontroller.CheckPointStateListener
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.locomotion.sbv.Base
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var loomoBase: Base

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        loomoBase = Base.getInstance()
        setupLoomoService()
        setClickListeners()
    }

    private fun setupLoomoService() {
        loomoBase.bindService(this, object : ServiceBinder.BindStateListener {
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
        })
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
        binding.btnTest7.setOnClickListener {
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
}

package com.kiwicampus.loomo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.kiwicampus.loomo.databinding.ActivityMainBinding
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.locomotion.sbv.Base

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

    private fun initLoomoNavigation(){
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
    }
}

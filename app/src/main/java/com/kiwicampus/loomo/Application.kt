package com.kiwicampus.loomo

import android.app.Application
import timber.log.Timber

@Suppress("unused")
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
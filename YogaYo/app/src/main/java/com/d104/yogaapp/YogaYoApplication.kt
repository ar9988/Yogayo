package com.d104.yogaapp

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class YogaYoApplication : Application() {
    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()


        Timber.plant(Timber.DebugTree())
    }


    companion object {

        var instance: YogaYoApplication? = null

        fun myContext(): Context {
            return instance!!.applicationContext
        }
    }
}
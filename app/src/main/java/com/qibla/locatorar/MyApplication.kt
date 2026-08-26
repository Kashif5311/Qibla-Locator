package com.qibla.locatorar

import android.app.Application
import com.qibla.locatorar.utils.PreferenceHelper

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PreferenceHelper.init(this)
    }
}
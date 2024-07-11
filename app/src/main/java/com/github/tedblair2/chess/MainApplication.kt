package com.github.tedblair2.chess

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication:Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
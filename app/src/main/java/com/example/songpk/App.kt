package com.example.songpk

import android.app.Application
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
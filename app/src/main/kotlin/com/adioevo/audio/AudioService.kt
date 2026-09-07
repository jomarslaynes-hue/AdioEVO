package com.adioevo.audio

import android.app.Service
import android.content.Intent
import android.os.IBinder
import timber.log.Timber

class AudioService : Service() {

    override fun onCreate() {
        super.onCreate()
        Timber.d("AudioService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("AudioService started")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("AudioService destroyed")
    }
}

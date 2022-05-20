package com.example.record.common

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class ApplicationClass : Application() {

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(Constants.CHANNEL_ID, "Now Playing", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "This is Record Playing"
            val notificationmanager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationmanager.createNotificationChannel(notificationChannel)
        }
    }
}
package com.example.record.services

import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.example.record.R
import com.example.record.common.Constants

class MusicService() : Service() {
    private var myBinder:MyBinder = MyBinder()
    var mediaPlayer:MediaPlayer? = null
    private lateinit var mediaSession:MediaSessionCompat

    override fun onBind(p0: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "record")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService() : MusicService{
            return this@MusicService
        }
    }

    fun showNotification(){
        val notifcation = NotificationCompat.Builder(baseContext, Constants.CHANNEL_ID)
            .setContentTitle("Music Name")
            .setContentText("Duration")
            .setSmallIcon(R.drawable.record_img)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.record_img))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .build()
        startForeground(13, notifcation)
    }
}
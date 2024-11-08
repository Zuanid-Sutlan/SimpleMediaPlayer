package com.example.mediaplayer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ForegroundService : Service() {

    companion object {
        const val ACTION_START = "com.example.mediaplayer.action.START"
        const val ACTION_PLAY_PAUSE = "com.example.mediaplayer.action.PLAY_PAUSE"
        const val ACTION_FORWARD = "com.example.mediaplayer.action.FORWARD"
        const val ACTION_REWIND = "com.example.mediaplayer.action.REWIND"
        const val NOTIFICATION_ID = 1
    }

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private var isPlaying = false

    override fun onCreate() {
        super.onCreate()
//        createNotificationChannel() // Ensure this is called
        mediaSession = MediaSessionCompat(this, "MediaSessionTag").apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            setCallback(mediaSessionCallback)
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> togglePlayPause()
            ACTION_FORWARD -> skipForward()
            ACTION_REWIND -> skipBackward()
            else -> {
                startForeground(NOTIFICATION_ID, createNotification().build())  // Ensure this is called
                playAudio()
            }
        }
        return START_NOT_STICKY
    }

    private fun playAudio() {
        mediaPlayer = MediaPlayer.create(this, R.raw.sample_audio).apply {
            setOnCompletionListener {
                stopSelf()
            }
            start()
            this@ForegroundService.isPlaying = true
        }
        updateNotification()
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.start()
        }
        isPlaying = !isPlaying
        updateNotification()
    }

    private fun skipForward() {
        mediaPlayer.seekTo(mediaPlayer.currentPosition + 5000) // Skip forward 5 seconds
    }

    private fun skipBackward() {
        mediaPlayer.seekTo(mediaPlayer.currentPosition - 5000) // Skip backward 5 seconds
    }

    private fun updateNotification() {
        val notification = createNotification()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            Log.i("foregroundService", "updateNotification: Permission is not Granted")
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification.build())
    }

    private fun createNotification(): NotificationCompat.Builder {
        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action(
                R.drawable.ic_pause, "Pause",
                NotificationHelper.getActionIntent(this, ACTION_PLAY_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_play, "Play",
                NotificationHelper.getActionIntent(this, ACTION_PLAY_PAUSE)
            )
        }

        return NotificationHelper.createNotification(
            context = this,
            mediaSession = mediaSession,
            playPauseAction = playPauseAction,
            forwardAction = NotificationCompat.Action(
                R.drawable.ic_forward, "Forward",
                NotificationHelper.getActionIntent(this, ACTION_FORWARD)
            ),
            rewindAction = NotificationCompat.Action(
                R.drawable.ic_rewind, "Rewind",
                NotificationHelper.getActionIntent(this, ACTION_REWIND)
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        mediaSession.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            togglePlayPause()
        }

        override fun onPause() {
            togglePlayPause()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "media_player_channel"
            val channelName = "Media Player Notifications"
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}

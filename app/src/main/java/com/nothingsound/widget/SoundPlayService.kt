package com.nothingsound.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * A tiny headless service: start it with a sound id, it plays that sound
 * through MediaPlayer, and stops itself as soon as playback finishes.
 */
class SoundPlayService : Service() {

    companion object {
        const val EXTRA_SOUND_ID = "extra_sound_id"
        const val ACTION_SOUND_STOPPED = "com.nothingsound.widget.ACTION_SOUND_STOPPED"
        private const val CHANNEL_ID = "sound_playback_channel"
        private const val NOTIFICATION_ID = 1
    }

    private var player: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val soundId = intent?.getStringExtra(EXTRA_SOUND_ID)
        val action = intent?.action

        if (action == "STOP") {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        if (soundId != null) {
            // Start as foreground service to prevent system from killing it on Nothing Phone
            startForeground(NOTIFICATION_ID, createNotification())
            playSound(soundId)
        } else {
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Playing Sound")
            .setSmallIcon(R.drawable.ic_dot_wave)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sound Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used for playing sounds from the widget"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun playSound(soundId: String) {
        player?.release()

        val item = SoundRepository.findById(this, soundId)
        val mp = MediaPlayer()
        mp.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )

        try {
            when {
                item?.filePath != null -> mp.setDataSource(this, Uri.fromFile(java.io.File(item.filePath)))
                item?.resName != null -> {
                    val resId = rawResourceId(item.resName)
                    if (resId == 0) {
                        finishPlayback()
                        return
                    }
                    val afd = resources.openRawResourceFd(resId)
                    mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                }
                else -> {
                    finishPlayback()
                    return
                }
            }

            mp.setOnCompletionListener {
                finishPlayback()
            }
            mp.setOnErrorListener { _, _, _ ->
                finishPlayback()
                true
            }
            mp.prepare()
            mp.start()
            player = mp
        } catch (e: Exception) {
            finishPlayback()
        }
    }

    private fun finishPlayback() {
        player?.release()
        player = null
        notifyStopped()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun notifyStopped() {
        sendBroadcast(Intent(ACTION_SOUND_STOPPED).setPackage(packageName))
    }

    private fun rawResourceId(resName: String): Int = when (resName) {
        "faah" -> R.raw.faah
        "bruh" -> R.raw.bruh
        "vine_boom" -> R.raw.vine_boom
        "airhorn" -> R.raw.airhorn
        else -> 0
    }

    override fun onDestroy() {
        player?.release()
        player = null
        notifyStopped()
        super.onDestroy()
    }
}

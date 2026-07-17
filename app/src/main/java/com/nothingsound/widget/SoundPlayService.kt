package com.nothingsound.widget

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder

/**
 * A tiny headless service: start it with a sound id, it plays that sound
 * through MediaPlayer, and stops itself as soon as playback finishes.
 * This is what widget taps trigger via PendingIntent.getService(), so
 * tapping the widget never opens any UI.
 */
class SoundPlayService : Service() {

    companion object {
        const val EXTRA_SOUND_ID = "extra_sound_id"
        const val ACTION_SOUND_STOPPED = "com.nothingsound.widget.ACTION_SOUND_STOPPED"
    }

    private var player: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val soundId = intent?.getStringExtra(EXTRA_SOUND_ID)
        val action = intent?.action

        if (action == "STOP") {
            stopSelf()
            return START_NOT_STICKY
        }

        if (soundId != null) {
            playSound(soundId)
        } else {
            stopSelf()
        }
        return START_NOT_STICKY
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
                    val resId = resources.getIdentifier(item.resName, "raw", packageName)
                    if (resId == 0) {
                        notifyStopped()
                        stopSelf()
                        return
                    }
                    val afd = resources.openRawResourceFd(resId)
                    mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                }
                else -> {
                    notifyStopped()
                    stopSelf()
                    return
                }
            }

            mp.setOnCompletionListener {
                it.release()
                notifyStopped()
                stopSelf()
            }
            mp.setOnErrorListener { p, _, _ ->
                p.release()
                notifyStopped()
                stopSelf()
                true
            }
            mp.prepare()
            mp.start()
            player = mp
        } catch (e: Exception) {
            mp.release()
            notifyStopped()
            stopSelf()
        }
    }

    private fun notifyStopped() {
        sendBroadcast(Intent(ACTION_SOUND_STOPPED))
    }

    override fun onDestroy() {
        player?.release()
        player = null
        notifyStopped()
        super.onDestroy()
    }
}

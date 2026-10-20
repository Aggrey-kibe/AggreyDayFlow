package com.aggreykibe.dayflow.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.aggreykibe.dayflow.R
import com.aggreykibe.dayflow.domain.SoundIds

/**
 * Plays one alarm sound at a time, on the alarm audio stream. Used by the ringing service (looping)
 * and by the sound preview in the editor and settings (plays once). Always call [stop] when finished.
 */
class SoundPlayer(context: Context) {
    private val context = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var ramp: Runnable? = null

    /** Starts [soundId]. With [gradual], volume climbs from quiet to full over about 15 seconds. */
    fun start(soundId: String, loop: Boolean, gradual: Boolean, onFinished: (() -> Unit)? = null) {
        stop()
        val mp = MediaPlayer()
        try {
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            val raw = rawResourceFor(soundId)
            if (raw != null) {
                context.resources.openRawResourceFd(raw).use { fd ->
                    mp.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                }
            } else {
                mp.setDataSource(context, systemAlarmUri())
            }
            mp.isLooping = loop
            if (!loop) mp.setOnCompletionListener { onFinished?.invoke() }
            mp.prepare()
            if (gradual) {
                mp.setVolume(START_VOLUME, START_VOLUME)
                beginRamp(mp)
            }
            mp.start()
            player = mp
        } catch (e: Exception) {
            mp.release()
            player = null
            onFinished?.invoke()
        }
    }

    fun stop() {
        ramp?.let { handler.removeCallbacks(it) }
        ramp = null
        val mp = player ?: return
        player = null
        try { mp.stop() } catch (e: IllegalStateException) { /* was not playing */ }
        mp.release()
    }

    private fun beginRamp(mp: MediaPlayer) {
        var step = 0
        val task = object : Runnable {
            override fun run() {
                step++
                val volume = (START_VOLUME + (1f - START_VOLUME) * step / RAMP_STEPS).coerceAtMost(1f)
                try { mp.setVolume(volume, volume) } catch (e: IllegalStateException) { return }
                if (step < RAMP_STEPS) handler.postDelayed(this, RAMP_INTERVAL_MS)
            }
        }
        ramp = task
        handler.postDelayed(task, RAMP_INTERVAL_MS)
    }

    private fun rawResourceFor(soundId: String): Int? = when (soundId) {
        SoundIds.CHIME -> R.raw.chime
        SoundIds.BELLS -> R.raw.bells
        SoundIds.BEEP -> R.raw.beep
        SoundIds.RISING -> R.raw.rising
        else -> null // SoundIds.SYSTEM: the phone's own alarm sound
    }

    private fun systemAlarmUri(): Uri =
        RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    private companion object {
        const val START_VOLUME = 0.15f
        const val RAMP_STEPS = 15
        const val RAMP_INTERVAL_MS = 1000L
    }
}

package com.aggreykibe.dayflow.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.aggreykibe.dayflow.R
import com.aggreykibe.dayflow.data.AlarmLog
import com.aggreykibe.dayflow.domain.AlarmStage
import com.aggreykibe.dayflow.domain.SoundIds

/**
 * Plays one alarm sound at a time, on the alarm audio stream. Used by the ringing service (looping)
 * and by the sound preview in the editor and settings (plays once). Always call [stop] when finished.
 */
class SoundPlayer(context: Context) {
    private val context = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val audioManager = this.context.getSystemService(AudioManager::class.java)
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
    private var player: MediaPlayer? = null
    private var ramp: Runnable? = null
    private var focusRequest: AudioFocusRequest? = null

    /**
     * Starts [soundId]. With [gradual], volume climbs from quiet to full over about 15 seconds.
     * If the chosen sound cannot be played (a missing file, storage still locked after a restart), a bundled
     * sound is used instead, so an alarm is never silent for that reason.
     */
    fun start(soundId: String, loop: Boolean, gradual: Boolean, onFinished: (() -> Unit)? = null): Boolean {
        stop()
        if (tryStart(soundId, loop, gradual, onFinished)) return true
        if (soundId != SoundIds.DEFAULT && tryStart(SoundIds.DEFAULT, loop, gradual, onFinished)) {
            AlarmLog.record(context, AlarmStage.SOUND_FALLBACK, "\"$soundId\" failed, played the default sound instead")
            return true
        }
        onFinished?.invoke()
        return false
    }

    private fun tryStart(soundId: String, loop: Boolean, gradual: Boolean, onFinished: (() -> Unit)?): Boolean {
        val mp = MediaPlayer()
        try {
            mp.setAudioAttributes(attributes)
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
            requestFocus()
            if (gradual) {
                mp.setVolume(START_VOLUME, START_VOLUME)
                beginRamp(mp)
            }
            mp.start()
            player = mp
            return true
        } catch (e: Exception) {
            mp.release()
            AlarmLog.record(context, AlarmStage.SOUND_FAILED, "$soundId: ${e.javaClass.simpleName} ${e.message}")
            return false
        }
    }

    /** Asks other apps (music, video) to pause. The alarm plays even if Android refuses: it must ring. */
    private fun requestFocus() {
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            .setAudioAttributes(attributes)
            .setWillPauseWhenDucked(false)
            .setOnAudioFocusChangeListener { }
            .build()
        audioManager.requestAudioFocus(request)
        focusRequest = request
    }

    fun stop() {
        ramp?.let { handler.removeCallbacks(it) }
        ramp = null
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        focusRequest = null
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

    private fun rawResourceFor(soundId: String): Int? = SoundResources.rawFor(soundId) // null = the phone's own sound

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

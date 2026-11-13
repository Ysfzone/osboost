package com.osboost.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object SoundEffects {

    private const val SAMPLE_RATE = 22050

    fun playLaunchWhoosh(context: Context) {
        if (isMuted(context)) return
        CoroutineScope(Dispatchers.Default).launch {
            playSynthesizedSweep(
                durationMs = 260,
                startFreq = 180.0,
                endFreq = 720.0,
                volume = 0.14f
            )
        }
    }

    fun playCompleteChime(context: Context) {
        if (isMuted(context)) return
        CoroutineScope(Dispatchers.Default).launch {
            playSynthesizedChime(
                freq1 = 660.0,
                freq2 = 990.0,
                durationMs = 280,
                volume = 0.16f
            )
        }
    }

    private fun isMuted(context: Context): Boolean {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return true
        val mediaVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        return mediaVolume <= 0 || am.ringerMode == AudioManager.RINGER_MODE_SILENT
    }

    private fun playSynthesizedSweep(
        durationMs: Int,
        startFreq: Double,
        endFreq: Double,
        volume: Float
    ) {
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val freq = startFreq + (endFreq - startFreq) * t * t
            val envelope = sin(PI * t)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val sample = sin(phase) * envelope * volume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        playPcmBuffer(buffer)
    }

    private fun playSynthesizedChime(
        freq1: Double,
        freq2: Double,
        durationMs: Int,
        volume: Float
    ) {
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        val half = numSamples / 2

        for (i in 0 until numSamples) {
            val freq = if (i < half) freq1 else freq2
            val localT = if (i < half) i.toDouble() / half else (i - half).toDouble() / half
            val envelope = exp(-3.5 * localT) * (1.0 - exp(-20.0 * localT))
            val sample = sin(2.0 * PI * freq * i / SAMPLE_RATE) * envelope * volume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        playPcmBuffer(buffer)
    }

    private fun playPcmBuffer(buffer: ShortArray) {
        runCatching {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep((buffer.size * 1000L) / SAMPLE_RATE + 40L)
            audioTrack.stop()
            audioTrack.release()
        }
    }
}


package com.multies.flagquest.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

object AudioManager {
    private const val SAMPLE_RATE = 22050
    private var musicJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    // Separate volume controls
    var musicVolume: Float = 0.3f
    var soundVolume: Float = 0.5f

    var isMusicEnabled: Boolean = true
    var isSoundEnabled: Boolean = true

    fun playSoundEffect(type: SoundType) {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val buffer = when (type) {
                    SoundType.CORRECT -> generateCorrectSfx()
                    SoundType.WRONG -> generateWrongSfx()
                    SoundType.MILESTONE -> generateMilestoneSfx()
                    SoundType.TICK -> generateTickSfx()
                }
                playBuffer(buffer)
            } catch (e: Exception) {
                Log.e("AudioManager", "Error playing sound effect", e)
            }
        }
    }

    fun startBackgroundMusic() {
        if (!isMusicEnabled) {
            stopBackgroundMusic()
            return
        }
        if (musicJob != null && musicJob?.isActive == true) return

        musicJob = scope.launch {
            val melody = listOf(261.63, 293.66, 329.63, 392.00, 440.00) // Pentatonic C Major (C4, D4, E4, G4, A4)
            var index = 0
            while (isActive) {
                if (isMusicEnabled) {
                    val freq = melody[index]
                    val durationSeconds = 1.2
                    val buffer = generateTone(freq, durationSeconds, musicVolume * 0.12f, fadeInOut = true)
                    playBuffer(buffer)
                    index = (index + 1) % melody.size
                }
                delay(3500) // Delay between notes for peaceful spacing
            }
        }
    }

    fun stopBackgroundMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    private fun playBuffer(buffer: ShortArray) {
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
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
            
            // Release resources when done
            scope.launch {
                val durationMs = (buffer.size.toFloat() / SAMPLE_RATE * 1000).toLong()
                delay(durationMs + 200)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {}
            }
        } catch (e: Exception) {
            Log.e("AudioManager", "Failed to play static buffer", e)
        }
    }

    private fun generateTone(freq: Double, durationSeconds: Double, volume: Float, fadeInOut: Boolean = false): ShortArray {
        val numSamples = (durationSeconds * SAMPLE_RATE).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sample = sin(2.0 * Math.PI * freq * t)
            
            if (fadeInOut) {
                val fadePercentage = 0.2
                val fadeSamples = (numSamples * fadePercentage).toInt()
                val scale = when {
                    i < fadeSamples -> i.toDouble() / fadeSamples
                    i > numSamples - fadeSamples -> (numSamples - i).toDouble() / fadeSamples
                    else -> 1.0
                }
                sample *= scale
            }
            samples[i] = (sample * 32767.0 * volume).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun generateCorrectSfx(): ShortArray {
        val part1 = generateTone(523.25, 0.10, soundVolume) // C5
        val part2 = generateTone(659.25, 0.10, soundVolume) // E5
        val part3 = generateTone(783.99, 0.20, soundVolume) // G5
        return part1 + part2 + part3
    }

    private fun generateWrongSfx(): ShortArray {
        val numSamples = (0.35 * SAMPLE_RATE).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val currentFreq = 160.0 - (160.0 - 110.0) * (i.toDouble() / numSamples)
            val sine = sin(2.0 * Math.PI * currentFreq * t)
            val valSample = if (sine > 0) 0.4 else -0.4
            val decay = 1.0 - (i.toDouble() / numSamples)
            samples[i] = (valSample * 32767.0 * soundVolume * 0.12f * decay).toInt().toShort()
        }
        return samples
    }

    private fun generateMilestoneSfx(): ShortArray {
        val part1 = generateTone(523.25, 0.12, soundVolume) // C5
        val part2 = generateTone(783.99, 0.12, soundVolume) // G5
        val part3 = generateTone(1046.50, 0.35, soundVolume) // C6
        return part1 + part2 + part3
    }

    private fun generateTickSfx(): ShortArray {
        return generateTone(1200.0, 0.015, soundVolume * 0.4f, fadeInOut = true)
    }
}

enum class SoundType {
    CORRECT, WRONG, MILESTONE, TICK
}

operator fun ShortArray.plus(other: ShortArray): ShortArray {
    val result = ShortArray(this.size + other.size)
    System.arraycopy(this, 0, result, 0, this.size)
    System.arraycopy(other, 0, result, this.size, other.size)
    return result
}

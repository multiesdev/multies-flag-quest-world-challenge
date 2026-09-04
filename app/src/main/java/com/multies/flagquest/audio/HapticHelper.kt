package com.multies.flagquest.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticHelper {
    var isVibrationEnabled: Boolean = true

    fun triggerCorrect(context: Context) {
        if (!isVibrationEnabled) return
        vibrate(context, milliseconds = 40, amplitude = VibrationEffect.EFFECT_CLICK)
    }

    fun triggerWrong(context: Context) {
        if (!isVibrationEnabled) return
        vibrate(context, milliseconds = 150, amplitude = VibrationEffect.EFFECT_DOUBLE_CLICK)
    }

    fun triggerMilestone(context: Context) {
        if (!isVibrationEnabled) return
        // Triumphant double buzz
        vibratePattern(context, longArrayOf(0, 80, 50, 150))
    }

    fun triggerTick(context: Context) {
        if (!isVibrationEnabled) return
        vibrate(context, milliseconds = 8, amplitude = VibrationEffect.EFFECT_TICK)
    }

    private fun vibrate(context: Context, milliseconds: Long, amplitude: Int) {
        try {
            val appContext = context.applicationContext
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let {
                if (it.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            it.vibrate(VibrationEffect.createPredefined(amplitude))
                        } catch (e: Exception) {
                            it.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate(milliseconds)
                    }
                }
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    private fun vibratePattern(context: Context, pattern: LongArray) {
        try {
            val appContext = context.applicationContext
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let {
                if (it.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        it.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate(pattern, -1)
                    }
                }
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }
}

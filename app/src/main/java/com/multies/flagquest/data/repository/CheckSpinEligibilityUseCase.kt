package com.multies.flagquest.data.repository

import com.multies.flagquest.data.local.entity.DailySpinEntity
import java.util.TimeZone

sealed class SpinEligibility {
    object Eligible : SpinEligibility()
    data class AlreadyClaimedToday(val remainingTimeMs: Long) : SpinEligibility()
    object SuspiciousClock : SpinEligibility()
}

class CheckSpinEligibilityUseCase(private val timeProvider: TimeProvider) {
    
    fun checkEligibility(
        state: DailySpinEntity?,
        timeZone: TimeZone = TimeZone.getDefault()
    ): SpinEligibility {
        if (state == null) {
            return SpinEligibility.Eligible
        }
        
        val now = timeProvider.currentTimeMillis()
        val todayStr = timeProvider.getLocalCalendarDay(now, timeZone)
        
        // 1. Clock moved backward suspiciously
        if (now < state.lastSpinTimestamp) {
            return SpinEligibility.SuspiciousClock
        }
        
        // 2. Already spun today
        if (todayStr == state.lastSpinDate) {
            // Calculate remaining time until next local calendar day (midnight)
            val calendar = java.util.Calendar.getInstance(timeZone)
            calendar.timeInMillis = now
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
            
            val midnightMs = calendar.timeInMillis
            val remainingMs = (midnightMs - now).coerceAtLeast(0L)
            
            return SpinEligibility.AlreadyClaimedToday(remainingMs)
        }
        
        return SpinEligibility.Eligible
    }
}

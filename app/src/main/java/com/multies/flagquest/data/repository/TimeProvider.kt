package com.multies.flagquest.data.repository

import android.os.SystemClock
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

interface TimeProvider {
    fun currentTimeMillis(): Long
    fun elapsedRealtime(): Long
    fun getLocalCalendarDay(timestamp: Long, timeZone: TimeZone = TimeZone.getDefault()): String
    fun getLocalCalendarDayCode(timestamp: Long, timeZone: TimeZone = TimeZone.getDefault()): Int
}

class DefaultTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
    override fun elapsedRealtime(): Long = SystemClock.elapsedRealtime()
    
    override fun getLocalCalendarDay(timestamp: Long, timeZone: TimeZone): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = timeZone
        return sdf.format(Date(timestamp))
    }
    
    override fun getLocalCalendarDayCode(timestamp: Long, timeZone: TimeZone): Int {
        val cal = Calendar.getInstance(timeZone)
        cal.timeInMillis = timestamp
        return cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
    }
}

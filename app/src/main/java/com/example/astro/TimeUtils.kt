package com.example.astro

import com.example.model.DstRule
import kotlin.math.floor

object TimeUtils {

    fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    fun daysInMonth(year: Int, month: Int): Int = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 30
    }

    /**
     * Day of week: 0 = Sunday, 1 = Monday, ..., 6 = Saturday (Sakamoto's algorithm)
     */
    fun dayOfWeek(year: Int, month: Int, day: Int): Int {
        val t = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
        var y = year
        if (month < 3) y -= 1
        return ((y + y / 4 - y / 100 + y / 400 + t[month - 1] + day) % 7 + 7) % 7
    }

    /**
     * Nth weekday of a month (e.g., 2nd Sunday of March). weekday: 0=Sun..6=Sat
     */
    private fun nthWeekdayOfMonth(year: Int, month: Int, weekday: Int, nth: Int): Int {
        val firstDow = dayOfWeek(year, month, 1)
        val delta = (weekday - firstDow + 7) % 7
        return 1 + delta + (nth - 1) * 7
    }

    private fun lastWeekdayOfMonth(year: Int, month: Int, weekday: Int): Int {
        val lastDay = daysInMonth(year, month)
        val lastDow = dayOfWeek(year, month, lastDay)
        val delta = (lastDow - weekday + 7) % 7
        return lastDay - delta
    }

    /**
     * Modern DST rules per README:
     * - US modern DST from 1976+ (1976-1986: last Sun Apr -> last Sun Oct;
     *   1987-2006: 1st Sun Apr -> last Sun Oct; 2007+: 2nd Sun Mar -> 1st Sun Nov)
     * - UK modern BST from 1981+ (last Sun Mar -> last Sun Oct)
     */
    fun effectiveUtcOffset(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        baseOffset: Double,
        dstRule: DstRule
    ): Double {
        return when (dstRule) {
            DstRule.NONE -> baseOffset
            DstRule.US_MODERN -> {
                if (year < 1976) return baseOffset
                val (startMonth, startDay, endMonth, endDay) = when {
                    year >= 2007 -> listOf(
                        3, nthWeekdayOfMonth(year, 3, 0, 2),
                        11, nthWeekdayOfMonth(year, 11, 0, 1)
                    )
                    year >= 1987 -> listOf(
                        4, nthWeekdayOfMonth(year, 4, 0, 1),
                        10, lastWeekdayOfMonth(year, 10, 0)
                    )
                    else -> listOf(
                        4, lastWeekdayOfMonth(year, 4, 0),
                        10, lastWeekdayOfMonth(year, 10, 0)
                    )
                }
                val inDst = isAfterOrAt(month, day, hour, startMonth, startDay, 2) &&
                    isBefore(month, day, hour, endMonth, endDay, 2)
                if (inDst) baseOffset + 1.0 else baseOffset
            }
            DstRule.UK_MODERN -> {
                if (year < 1981) return baseOffset
                val startDay = lastWeekdayOfMonth(year, 3, 0)
                val endDay = lastWeekdayOfMonth(year, 10, 0)
                val inDst = isAfterOrAt(month, day, hour, 3, startDay, 1) &&
                    isBefore(month, day, hour, 10, endDay, 1)
                if (inDst) baseOffset + 1.0 else baseOffset
            }
        }
    }

    private fun isAfterOrAt(m: Int, d: Int, h: Int, targetM: Int, targetD: Int, targetH: Int): Boolean {
        if (m != targetM) return m > targetM
        if (d != targetD) return d > targetD
        return h >= targetH
    }

    private fun isBefore(m: Int, d: Int, h: Int, targetM: Int, targetD: Int, targetH: Int): Boolean {
        if (m != targetM) return m < targetM
        if (d != targetD) return d < targetD
        return h < targetH
    }

    /**
     * Converts local civil date-time and UTC offset to UT Julian Day.
     */
    fun toJulianDay(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Double = 0.0,
        utcOffsetHours: Double
    ): Double {
        val localDayFrac = (hour + minute / 60.0 + second / 3600.0) / 24.0
        val utDay = day + localDayFrac - utcOffsetHours / 24.0
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = if (y > 1582 || (y == 1582 && (m > 10 || (m == 10 && day >= 15)))) {
            2.0 - a + floor(a / 4.0)
        } else {
            0.0
        }
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + utDay + b - 1524.5
    }

    /**
     * Converts a Julian Day (plus timezone offset) back to local civil date YYYY-MM-DD.
     */
    fun jdToLocalDateString(jd: Double, utcOffsetHours: Double = 0.0): String {
        val jdLocal = jd + utcOffsetHours / 24.0 + 0.5
        val z = floor(jdLocal).toInt()
        val a = if (z < 2299161) z else {
            val alpha = floor((z - 1867216.25) / 36524.25).toInt()
            z + 1 + alpha - alpha / 4
        }
        val b = a + 1524
        val c = floor((b - 122.1) / 365.25).toInt()
        val d = floor(365.25 * c).toInt()
        val e = floor((b - d) / 30.6001).toInt()
        val day = b - d - floor(30.6001 * e).toInt()
        val month = if (e < 14) e - 1 else e - 13
        val year = if (month > 2) c - 4716 else c - 4715
        return "%04d-%02d-%02d".format(year, month, day)
    }

    fun formatHoursToHhMm(hoursInput: Double): String {
        val wrapped = ((hoursInput % 24.0) + 24.0) % 24.0
        val totalMinutes = kotlin.math.round(wrapped * 60.0).toInt() % (24 * 60)
        val hh = totalMinutes / 60
        val mm = totalMinutes % 60
        return "%02d:%02d".format(hh, mm)
    }

    fun formatDms(degInSign: Double): String {
        val totalSec = kotlin.math.round(degInSign * 3600.0).toInt().coerceIn(0, 30 * 3600 - 1)
        val d = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return "%02d°%02d'%02d\"".format(d, m, s)
    }
}

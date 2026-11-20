package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object DateTimeUtils {

    private val shortDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dayOfWeekShortFormat = SimpleDateFormat("EEE", Locale.getDefault())

    fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun endOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    fun combineDateAndTime(dateMillis: Long, hourOfDay: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getHourAndMinute(millis: Long): Pair<Int, Int> {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE)
    }

    fun addDays(millis: Long, days: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            add(Calendar.DAY_OF_YEAR, days)
        }
        return cal.timeInMillis
    }

    fun addMonths(millis: Long, months: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            add(Calendar.MONTH, months)
        }
        return cal.timeInMillis
    }

    fun startOfWeekMonday(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = startOfDay(millis)
            firstDayOfWeek = Calendar.MONDAY
            val dayOfWeek = get(Calendar.DAY_OF_WEEK)
            val diff = if (dayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - dayOfWeek
            add(Calendar.DAY_OF_YEAR, diff)
        }
        return cal.timeInMillis
    }

    fun getWeekDaysFrom(weekStartMillis: Long): List<Long> =
        (0..6).map { offset -> addDays(weekStartMillis, offset) }

    /**
     * Returns 42 cells (6 weeks x 7 days starting Monday) for a month calendar grid.
     * Each entry is Pair(dayStartMillis, isInTargetMonth).
     */
    fun getMonthGrid(anchorMillis: Long): List<Pair<Long, Boolean>> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = anchorMillis
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val targetMonth = cal.get(Calendar.MONTH)
        val firstCellMillis = startOfWeekMonday(cal.timeInMillis)
        return (0 until 42).map { index ->
            val cellMillis = addDays(firstCellMillis, index)
            val cellCal = Calendar.getInstance().apply { timeInMillis = cellMillis }
            cellMillis to (cellCal.get(Calendar.MONTH) == targetMonth)
        }
    }

    fun isSameDay(aMillis: Long, bMillis: Long): Boolean {
        val calA = Calendar.getInstance().apply { timeInMillis = aMillis }
        val calB = Calendar.getInstance().apply { timeInMillis = bMillis }
        return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
            calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
    }

    fun dayOfMonth(millis: Long): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return cal.get(Calendar.DAY_OF_MONTH)
    }

    fun formatShortDate(millis: Long): String = shortDateFormat.format(Date(millis))

    fun formatFullDate(millis: Long): String = fullDateFormat.format(Date(millis))

    fun formatMonthYear(millis: Long): String = monthYearFormat.format(Date(millis))

    fun formatTime(millis: Long): String = timeFormat.format(Date(millis))

    fun formatDayOfWeekShort(millis: Long): String = dayOfWeekShortFormat.format(Date(millis))

    fun formatDateTime(millis: Long): String = "${formatShortDate(millis)} · ${formatTime(millis)}"

    fun formatRelativeDue(dueMillis: Long, isCompleted: Boolean, nowMillis: Long = System.currentTimeMillis()): String {
        if (isCompleted) return "Completed"
        val diffMillis = dueMillis - nowMillis
        val diffMinutes = diffMillis / 60_000L
        val diffHours = diffMinutes / 60L

        if (diffMillis < 0) {
            val overdueHours = abs(diffHours)
            val overdueDays = overdueHours / 24L
            return when {
                overdueDays >= 1 -> "Overdue by ${overdueDays}d"
                overdueHours >= 1 -> "Overdue by ${overdueHours}h"
                else -> "Overdue"
            }
        }

        val todayStart = startOfDay(nowMillis)
        val dueStart = startOfDay(dueMillis)
        val dayDiff = ((dueStart - todayStart) / (24L * 60L * 60L * 1000L)).toInt()

        return when {
            diffMinutes < 60 -> "Due in ${diffMinutes.coerceAtLeast(1)}m"
            diffHours < 12 -> "Due in ${diffHours}h"
            dayDiff == 0 -> "Today at ${formatTime(dueMillis)}"
            dayDiff == 1 -> "Tomorrow at ${formatTime(dueMillis)}"
            dayDiff in 2..6 -> "In $dayDiff days (${formatDayOfWeekShort(dueMillis)})"
            else -> "Due ${formatShortDate(dueMillis)}"
        }
    }

    /**
     * Converts UTC millis returned by Material3 DatePicker (which uses UTC midnight)
     * into local-timezone midnight on the same calendar date.
     */
    fun utcPickerMillisToLocalDay(utcMillis: Long): Long {
        val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = utcMillis
        }
        val localCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
            set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return localCal.timeInMillis
    }

    fun localMillisToUtcPickerMillis(localMillis: Long): Long {
        val localCal = Calendar.getInstance().apply { timeInMillis = localMillis }
        val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, localCal.get(Calendar.YEAR))
            set(Calendar.MONTH, localCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, localCal.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return utcCal.timeInMillis
    }
}

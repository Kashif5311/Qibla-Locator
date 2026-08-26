package com.qibla.locatorar.utils

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * DateTimeUtils
 * This class contains a bunch of function that can manipulate
 * Date object or Date String to achieve certain operations
 * e.g : Time difference, Time Ago, Date formatting
 *
 * @author thunder413
 * @version 1.5
 */
object DateTimeUtils {
    /**
     * LOG TAG
     */
    private const val LOG_TAG = "DateTimeUtils"

    /**
     * Debug mode
     */
    private var debug = false

    /**
     * Time zone
     */
    private var timeZone = "UTC"

    /**
     * Enable / Disable
     *
     * @param state Debug state
     */
    fun setDebug(state: Boolean) {
        debug = state
    }

    /**
     * Set TimeZone
     *
     * @param zone TimeZone
     */
    fun setTimeZone(zone: String) {
        timeZone = zone
    }

    /**
     * Get Date or DateTime formatting pattern
     *
     * @param dateString Date String
     * @return Format Pattern
     */
    public fun getDatePattern(dateString: String?): String? {
        return if (isDateTime(dateString)) {
            if (dateString!!.contains("/")) DateTimeFormat.DATE_TIME_PATTERN_2 else DateTimeFormat.DATE_TIME_PATTERN_1
        } else {
            if (dateString!!.contains("/")) DateTimeFormat.DATE_PATTERN_2 else DateTimeFormat.DATE_PATTERN_1
        }
    }
    /**
     * Convert a Java Date object to String
     *
     * @param date   Date Object
     * @param locale Locale
     * @return Date Object string representation
     */
    /**
     * Convert a Java Date object to String
     *
     * @param date Date Object
     * @return Date Object string representation
     */
    @JvmOverloads
    fun formatDate(date: Date?, locale: Locale? = Locale.getDefault()): String {
        if (date == null && debug) {
            Log.e(LOG_TAG, "formatDate >> Supplied date is null")
        }
        val iso8601Format = SimpleDateFormat(DateTimeFormat.DATE_TIME_PATTERN_1, locale)
        iso8601Format.timeZone = TimeZone.getTimeZone(timeZone)
        if (debug) {
            Log.d(
                LOG_TAG,
                "formatDate >> Formatting using " + iso8601Format.timeZone.displayName + " | " + iso8601Format.timeZone.id
            )
        }
        return iso8601Format.format(date)
    }

    /**
     * Convert a date string to Java Date Object
     *
     * @param dateString Date String
     * @param locale     Locale
     * @return Java Date Object
     */
    fun formatDate(dateString: String?, locale: Locale?): Date? {
        val iso8601Format = SimpleDateFormat(getDatePattern(dateString), locale)
        iso8601Format.timeZone = TimeZone.getTimeZone(timeZone)
        var date: Date? = null
        if (dateString != null) {
            try {
                date = iso8601Format.parse(dateString.trim { it <= ' ' })
            } catch (e: ParseException) {
                if (debug) {
                    Log.e(
                        LOG_TAG,
                        "formatDate >> Fail to parse supplied date string >> $dateString"
                    )
                    e.printStackTrace()
                }
            }
        }
        return date
    }

    /**
     * Convert a date string to Java Date Object
     *
     * @param date Date String
     * @return Java Date Object
     */
    fun formatDate(date: String?): Date? {
        return formatDate(date, Locale.getDefault())
    }
    /**
     * Convert a timeStamp into a date object
     *
     * @param timeStamp TimeStamp
     * @param units     Witch unit is whether seconds or milliseconds
     * @return Date object
     * @see DateTimeUnits.SECONDS
     *
     * @see DateTimeUnits.MILLISECONDS
     */
    /**
     * Convert a timeStamp into a date considering given timeStamp in milliseconds
     *
     * @param timeStamp TimeStamp
     * @return Date object
     * @see DateTimeUnits.MILLISECONDS
     */
    @JvmOverloads
    fun formatDate(timeStamp: Long, units: DateTimeUnits = DateTimeUnits.MILLISECONDS): Date {
        return if (units == DateTimeUnits.SECONDS) Date(timeStamp * 1000L) else Date(
            timeStamp
        )
    }
    /**
     * Format date using a given pattern
     * and apply supplied locale
     *
     * @param date    Date Object
     * @param pattern Pattern
     * @param locale  Locale
     * @return Formatted date
     */
    /**
     * Format date using a given pattern
     * apply default locale
     *
     * @param date    Date Object
     * @param pattern Pattern
     * @return Formatted date
     */
    @JvmOverloads
    fun formatWithPattern(
        date: Date?,
        pattern: String?,
        locale: Locale? = Locale.ENGLISH
    ): String {
        if (date == null && debug) {
            Log.e(LOG_TAG, "FormatWithPattern >> Supplied date is null")
        }
        val iso8601Format = SimpleDateFormat(pattern, locale)
        iso8601Format.timeZone = TimeZone.getTimeZone(timeZone)
        return iso8601Format.format(date)
    }
    /**
     * Format date using a given pattern
     * and apply supplied locale
     *
     * @param date    Date String
     * @param pattern Pattern
     * @param locale  Locale
     * @return Formatted date
     */
    /**
     * Format date using a given pattern
     * apply default locale
     *
     * @param date    Date String
     * @param pattern Pattern
     * @return Formatted date
     */
    @JvmOverloads
    fun formatWithPattern(
        date: String?,
        pattern: String?,
        locale: Locale? = Locale.getDefault()
    ): String {
        return formatWithPattern(formatDate(date), pattern, locale)
    }

    /**
     * Build a pattern for given style
     *
     * @param style DateTimeStyle
     * @return Pattern
     */
    private fun getPatternForStyle(style: DateTimeStyle): String {
        val pattern: String
        pattern = if (style == DateTimeStyle.LONG) {
            "MMMM dd, yyyy"
        } else if (style == DateTimeStyle.MEDIUM) {
            "MMM dd, yyyy"
        } else if (style == DateTimeStyle.SHORT) {
            "MM/dd/yy"
        } else {
            "EEEE, MMMM dd, yyyy"
        }
        return pattern
    }
    /**
     * Get localized date string
     *
     * @param date Date string
     * @return Formatted localized date string
     */
    /**
     * Get localized date string (Using default locale)
     *
     * @param date Date string
     * @return Formatted localized date string
     */
    @JvmOverloads
    fun formatWithStyle(
        date: Date?,
        style: DateTimeStyle,
        locale: Locale? = Locale.getDefault()
    ): String {
        if (date == null && debug) {
            Log.e(LOG_TAG, "FormatWithPattern >> Supplied date is null")
        }
        return formatWithPattern(date, getPatternForStyle(style), locale)
    }
    /**
     * Get localized date string (Using default locale)
     *
     * @param date Date string
     * @return Formatted localized date string
     */
    /**
     * Get localized date string (Using default locale)
     *
     * @param date Date string
     * @return Formatted localized date string
     */
    @JvmOverloads
    fun formatWithStyle(
        date: String?,
        style: DateTimeStyle,
        locale: Locale? = Locale.getDefault()
    ): String {
        return formatWithStyle(formatDate(date), style, locale)
    }
    /**
     * Extract time from date without seconds
     *
     * @param date Date object
     * @return Time String
     * @see DateTimeFormat.TIME_PATTERN_1
     */
    /**
     * Extract time from date without seconds
     *
     * @param date Date object
     * @return Time string
     */
    @JvmOverloads
    fun formatTime(date: Date?, forceShowHours: Boolean = false): String {
        val iso8601Format = SimpleDateFormat(DateTimeFormat.TIME_PATTERN_1, Locale.getDefault())
        iso8601Format.timeZone = TimeZone.getTimeZone(timeZone)
        val time = iso8601Format.format(date)
        val hhmmss = time.split(":".toRegex()).toTypedArray()
        val hours = hhmmss[0].toInt()
        val minutes = hhmmss[1].toInt()
        val seconds = hhmmss[2].toInt()
        return ((if (hours == 0 && !forceShowHours) "" else if (hours < 10) "0$hours:" else "$hours:") +
                (if (minutes == 0) "00" else if (minutes < 10) "0$minutes" else minutes.toString()) + ":"
                + if (seconds == 0) "00" else if (seconds < 10) "0$seconds" else seconds.toString())
    }
    /**
     * Extract time from date without seconds
     *
     * @param date Date object
     * @return Time string
     */
    /**
     * Extract time from date without seconds
     *
     * @param date Date object
     * @return Time string
     */
    @JvmOverloads
    fun formatTime(date: String?, forceShowHours: Boolean = false): String {
        return formatTime(formatDate(date), forceShowHours)
    }

    /**
     * Convert millis to human readable time
     *
     * @param millis TimeStamp
     * @return Time String
     */
    fun millisToTime(millis: Long): String {
        val seconds = (TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)))
        val minutes = (TimeUnit.MILLISECONDS.toMinutes(millis)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)))
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        return ((if (hours == 0L) "" else if (hours < 10) "0$hours:" else "$hours:") +
                (if (minutes == 0L) "00" else if (minutes < 10) "0$minutes" else minutes.toString()) + ":"
                + if (seconds == 0L) "00" else if (seconds < 10) "0$seconds" else seconds.toString())
    }

    /**
     * Convert millis to human readable time
     *
     * @param time Time string
     * @return Time String
     */
    fun timeToMillis(time: String): Long {
        val hhmmss = time.split(":".toRegex()).toTypedArray()
        var hours = 0
        val minutes: Int
        val seconds: Int
        if (hhmmss.size == 3) {
            hours = hhmmss[0].toInt()
            minutes = hhmmss[1].toInt()
            seconds = hhmmss[2].toInt()
        } else {
            minutes = hhmmss[0].toInt()
            seconds = hhmmss[1].toInt()
        }
        return ((hours * 60 + minutes * 60 + seconds) * 1000).toLong()
    }

    /**
     * Tell whether or not a given string represent a date time string or a simple date
     *
     * @param dateString Date String
     * @return True if given string is a date time False otherwise
     */
    fun isDateTime(dateString: String?): Boolean {
        return dateString != null && dateString.trim { it <= ' ' }.split(" ".toRegex())
            .toTypedArray().size > 1
    }

    /**
     * Tell whether or not a given date is yesterday
     *
     * @param date Date Object
     * @return True if the date is yesterday False otherwise
     */
    fun isYesterday(date: Date?): Boolean {
        // Check if yesterday
        val c1 = Calendar.getInstance() // today
        c1.add(Calendar.DAY_OF_YEAR, -1) // yesterday
        val c2 = Calendar.getInstance()
        c2.time = date //
        return (c1[Calendar.YEAR] == c2[Calendar.YEAR]
                && c1[Calendar.DAY_OF_YEAR] == c2[Calendar.DAY_OF_YEAR])
    }

    /**
     * Tell whether or not a given date is yesterday
     *
     * @param dateString Date String
     * @return True if the date is yesterday False otherwise
     */
    fun isYesterday(dateString: String?): Boolean {
        return isYesterday(formatDate(dateString))
    }

    /**
     * Tell whether or not a given date is today date
     *
     * @param date Date object
     * @return True if date is today False otherwise
     */
    fun isToday(date: Date?): Boolean {
        return DateUtils.isToday(date!!.time)
    }

    /**
     * Tell whether or not a given date is today date
     *
     * @param dateString Date string
     * @return True if date is today False otherwise
     */
    fun isToday(dateString: String?): Boolean {
        return isToday(formatDate(dateString))
    }

    /**
     * Get Previous month from a given date
     *
     * @param date Date start
     * @return Date of the previous month
     */
    fun getPreviousMonthDate(date: Date?): Date {
        val c = Calendar.getInstance()
        c.time = date //
        c.add(Calendar.MONTH, -1)
        return c.time
    }

    /**
     * Get Previous month from a given date
     *
     * @param date Date start
     * @return Date of the previous month
     */
    fun getPreviousMonthDate(date: String?): Date {
        return getPreviousMonthDate(formatDate(date))
    }

    /**
     * Get Next month from a given date
     *
     * @param date Date start
     * @return Date of the previous month
     */
    fun getNextMonthDate(date: Date?): Date {
        val c = Calendar.getInstance()
        c.time = date //
        c.add(Calendar.MONTH, 1)
        return c.time
    }

    /**
     * Get Previous month from a given date
     *
     * @param date String Date start
     * @return Date of the previous month
     */
    fun getNextMonthDate(date: String?): Date {
        return getNextMonthDate(formatDate(date))
    }

    /**
     * Get Previous week date
     *
     * @param date         Date Object
     * @param dayOfTheWeek Day Of the week
     * @return Date
     */
    fun getPreviousWeekDate(date: Date?, dayOfTheWeek: Int): Date {
        val c = Calendar.getInstance()
        c.time = date
        c.firstDayOfWeek = dayOfTheWeek
        c[Calendar.DAY_OF_WEEK] = dayOfTheWeek
        c.add(Calendar.DATE, -7)
        return c.time
    }

    /**
     * Get Previous week date
     *
     * @param date         Date String
     * @param dayOfTheWeek Day Of the week
     * @return Date
     */
    fun getPreviousWeekDate(date: String?, dayOfTheWeek: Int): Date {
        return getPreviousWeekDate(formatDate(date), dayOfTheWeek)
    }

    /**
     * Get Next week date
     *
     * @param date         Date Object
     * @param dayOfTheWeek Day Of the week
     * @return Date
     */
    fun getNextWeekDate(date: Date?, dayOfTheWeek: Int): Date {
        val c = Calendar.getInstance()
        c.time = date
        c.firstDayOfWeek = dayOfTheWeek
        c[Calendar.DAY_OF_WEEK] = dayOfTheWeek
        c.add(Calendar.DATE, 7)
        return c.time
    }

    /**
     * Get Next week date
     *
     * @param date Date Object
     * @return Date
     */
    fun getNextWeekDate(date: Date?): Date {
        return getNextWeekDate(date, Calendar.MONDAY)
    }

    /**
     * Get Next week date
     *
     * @param date Date Object
     * @return Date
     */
    fun getNextWeekDate(date: String?): Date {
        return getNextWeekDate(formatDate(date))
    }

    /**
     * Get Next week date
     *
     * @param date         Date Object
     * @param dayOfTheWeek Day Of the week
     * @return Date
     */
    fun getNextWeekDate(date: String?, dayOfTheWeek: Int): Date {
        return getNextWeekDate(formatDate(date), dayOfTheWeek)
    }

    /**
     * Get difference between two dates
     *
     * @param nowDate  Current date
     * @param oldDate  Date to compare
     * @param dateDiff Difference Unit
     * @return Difference
     */
    fun getDateDiff(nowDate: Date?, oldDate: Date?, dateDiff: DateTimeUnits?): Int {
        val diffInMs = nowDate!!.time - oldDate!!.time
//        val days = TimeUnit.MILLISECONDS.toDays(diffInMs).toInt()
//        val hours = (TimeUnit.MILLISECONDS.toHours(diffInMs) - TimeUnit.DAYS.toHours(
//            days.toLong()
//        )).toInt()
//        val minutes = (TimeUnit.MILLISECONDS.toMinutes(diffInMs) - TimeUnit.HOURS.toMinutes(
//            TimeUnit.MILLISECONDS.toHours(diffInMs)
//        )).toInt()
//        val seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMs).toInt()

        val days = TimeUnit.MILLISECONDS.toDays(diffInMs).toInt()
        val hours = TimeUnit.MILLISECONDS.toHours(diffInMs).toInt()
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMs).toInt()
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMs).toInt()
        return when (dateDiff) {
            DateTimeUnits.DAYS -> days
            DateTimeUnits.SECONDS -> seconds
            DateTimeUnits.MINUTES -> minutes
            DateTimeUnits.HOURS -> hours
            DateTimeUnits.MILLISECONDS -> diffInMs.toInt()
            else -> diffInMs.toInt()
        }
    }

    /**
     * Get difference between two dates
     *
     * @param nowDate  Current date
     * @param oldDate  Date to compare
     * @param dateDiff Difference Unit
     * @return Difference
     */
    fun getDateDiff(nowDate: String?, oldDate: Date?, dateDiff: DateTimeUnits?): Int {
        return getDateDiff(formatDate(nowDate), oldDate, dateDiff)
    }

    /**
     * Get difference between two dates
     *
     * @param nowDate  Current date
     * @param oldDate  Date to compare
     * @param dateDiff Difference Unit
     * @return Difference
     */
    fun getDateDiff(nowDate: Date?, oldDate: String?, dateDiff: DateTimeUnits?): Int {
        return getDateDiff(nowDate, formatDate(oldDate), dateDiff)
    }

    /**
     * Get difference between two dates
     *
     * @param nowDate  Current date
     * @param oldDate  Date to compare
     * @param dateDiff Difference Unit
     * @return Difference
     */
    fun getDateDiff(nowDate: String?, oldDate: String?, dateDiff: DateTimeUnits?): Int {
        return getDateDiff(nowDate, formatDate(oldDate), dateDiff)
    }

    fun convertDateStringXFormatToYFormat(
        dateStr_: String?,
        xFormat: String?,
        yFormat: String?,
        locale: String = "en",
        timeZone: String = ""
    ): String? {
        var dateStr = dateStr_
        if (xFormat!!.endsWith("z", true) && (!dateStr!!.endsWith("z", true))) {
            dateStr += "Z"
        }
        val originalFormat: DateFormat =
            SimpleDateFormat(xFormat, Locale(locale))
        val targetFormat: DateFormat =
            SimpleDateFormat(yFormat, Locale(locale))
        if (timeZone.equals("", ignoreCase = true)) {
            targetFormat.timeZone = TimeZone.getDefault()
            originalFormat.timeZone = TimeZone.getDefault()
        } else {
            targetFormat.timeZone = TimeZone.getTimeZone(timeZone)
            originalFormat.timeZone = TimeZone.getTimeZone(timeZone)
        }
        try {
            val date = originalFormat.parse(dateStr)
            dateStr = targetFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return dateStr
    }


    fun getCurrentDateTimeFromTimeZoneWithAddDays(
        timezone: String,
        formate: String,
        daysToAdd: Int = 0
    ): String {
        val sdf =
            SimpleDateFormat(formate, Locale.US)
        sdf.timeZone = TimeZone.getTimeZone(timezone)
        val time = sdf.format(Date())
        val c = Calendar.getInstance()
        try {
            c.time = sdf.parse(time)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        c.add(Calendar.DATE, daysToAdd)
        val ttime = sdf.format(c.time)
        return ttime
    }

    fun getCurrentDateTimeAsString(formate: String): String? {
        val sdf =
            SimpleDateFormat(formate, Locale.US)
        val time = sdf.format(Date())
        return time
    }

    fun getDateTimeStringFromCalender(calendar: Calendar, format: String): String? {
        val dateFormat = SimpleDateFormat(format, Locale.US)
        return dateFormat.format(calendar.time)
    }

    fun getTimeDifference(time1: String, time2: String, formate: String): Int {
        val sdf = SimpleDateFormat(formate, Locale.ENGLISH)
        try {
            val date1: Date = sdf.parse(time1.replace("Z", "").replace("z", ""))
            val date2: Date = sdf.parse(time2.replace("Z", "").replace("z", ""))
            return date1.compareTo(date2)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return 0
    }


    fun isDateBefore(time1: String, time2: String, format: String): Boolean {
        val sdf = SimpleDateFormat(format, Locale.ENGLISH)
        try {
            val date1: Date = sdf.parse(time1)
            val date2: Date = sdf.parse(time2)
            return date1.before(date2)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }

    fun isDateAfter(time1: String, time2: String, format: String): Boolean {
        val sdf = SimpleDateFormat(format, Locale.ENGLISH)
        try {
            val date1: Date = sdf.parse(time1)
            val date2: Date = sdf.parse(time2)
            return date1.after(date2)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }

    fun isSameDate(time1: String, time2: String, format: String): Boolean {
        val sdf = SimpleDateFormat(format, Locale.ENGLISH)
        try {
            val date1: Date = sdf.parse(time1)!!
            val date2: Date = sdf.parse(time2)!!
            return date1.equals(date2)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }


    fun getTimeDifferenceInHoursWithoutNegative(
        time1: String,
        time2: String,
        formate: String,
        dateDiff: DateTimeUnits?
    ): Long {
        val sdf = SimpleDateFormat(formate, Locale.ENGLISH)
        try {
            val date1: Date = sdf.parse(time1)!!
            val date2: Date = sdf.parse(time2)!!
            var diff = date1.time - date2.time
            if (date1.time < date2.time) {
                diff = date2.time - date1.time
            }
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            when (dateDiff) {
                DateTimeUnits.DAYS -> {
                    return days
                }
                DateTimeUnits.HOURS -> {
                    return hours
                }
                DateTimeUnits.MINUTES -> {
                    return minutes
                }
                DateTimeUnits.SECONDS -> {
                    return seconds
                }
                else -> {
                    return diff
                }
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return 0
    }

    fun getTimeDifferenceInHours(
        time1: String,
        time2: String,
        formate: String,
        dateDiff: DateTimeUnits?
    ): Long {
        val sdf = SimpleDateFormat(formate, Locale.ENGLISH)
        try {
            val date1: Date = sdf.parse(time1)!!
            val date2: Date = sdf.parse(time2)!!
            var diff = date1.time - date2.time
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            when (dateDiff) {
                DateTimeUnits.DAYS -> {
                    return days
                }
                DateTimeUnits.HOURS -> {
                    return hours
                }
                DateTimeUnits.MINUTES -> {
                    return minutes
                }
                DateTimeUnits.SECONDS -> {
                    return seconds
                }
                else -> {
                    return diff
                }
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return 0
    }

    // Utility method to get that date from milliseconds
    fun getDateStrFromMilliSeconds(milliseconds: Long, format: String, locale: String = "en"): String {
        val date = Date(milliseconds)
        val dateFormat = SimpleDateFormat(format, Locale(locale))
        return dateFormat.format(date)
    }

    fun convertTimestampToDate(timestamp: Long, requiredFormat: String): String {
        val sdf = SimpleDateFormat(requiredFormat)
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val date = cal.time
        return sdf.format(date)
    }

    fun getCurrentDateTime(
        format: String = "yyyy-MM-dd HH:mm:ss",
        locale: Locale = Locale.ENGLISH
    ): String {
        val formatter = DateTimeFormatter.ofPattern(format, locale)
        return LocalDateTime.now().format(formatter)
    }
}

object DateTimeFormat {
    /**
     * ISO_8601
     */
    const val DATE_ISO_8601_WITH_ZONE = "yyyy-MM-dd'T'HH:mm:ss.SSX"

    /**
     * Typical MySqL/SQL dateTime format with dash as separator
     */
    const val DATE_TIME_PATTERN_1 = "yyyy-MM-dd HH:mm:ss"

    /**
     * Typical MySqL/SQL dateTime format with slash as seperator
     */
    const val DATE_TIME_PATTERN_2 = "dd/MM/yyyy HH:mm:ss"

    /**
     * Typical MySqL/SQL date format with dash as separator
     */
    const val DATE_PATTERN_1 = "yyyy-MM-dd"

    /**
     * Typical MySqL/SQL date format with slash as seperator
     */
    const val DATE_PATTERN_2 = "dd/MM/yyyy"

    /**
     * Time format full
     */
    const val TIME_PATTERN_1 = "HH:mm:ss"
}

enum class DateTimeUnits {
    /**
     * Days
     */
    DAYS,

    /**
     * Hours
     */
    HOURS,

    /**
     * Minutes
     */
    MINUTES,

    /**
     * Seconds
     */
    SECONDS,

    /**
     * Milliseconds
     */
    MILLISECONDS
}

enum class DateTimeStyle {
    /**
     * Style full e.g Tuesday, June 13, 2017
     */
    FULL,

    /**
     * Style long e.g June 13, 2017
     */
    LONG,

    /**
     * Style medium e.g Jun 13, 2017
     */
    MEDIUM,

    /**
     * Style short e.g 06/13/17
     */
    SHORT,

    /**
     * Style for ago time e.g 3h ago
     */
    AGO_SHORT_STRING,

    /**
     * Style for ago time e.g 3 hours ago
     */
    AGO_FULL_STRING
}
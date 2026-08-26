package com.qibla.locatorar.utils

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

import androidx.core.content.FileProvider
import android.graphics.Bitmap
import android.content.Intent
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object AppUtils {
    /**
     * Share a bitmap via Intent
     */
    fun shareBitmap(context: Context, bitmap: Bitmap) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val stream = FileOutputStream("$cachePath/zakat_calculation.png")
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val imagePath = File(context.cacheDir, "images")
            val newFile = File(imagePath, "zakat_calculation.png")
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                newFile
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "image/png"
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Zakat Calculation"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Calculate the bearing angle towards Kaaba (Qibla) from user's location
     */
    fun calculateQiblaBearing(latitude: Double, longitude: Double): Float {
        val kaabaLat = Math.toRadians(AppConstants.KAABA_LATITUDE)
        val kaabaLon = Math.toRadians(AppConstants.KAABA_LONGITUDE)
        val userLat = Math.toRadians(latitude)
        val userLon = Math.toRadians(longitude)
        val deltaLon = kaabaLon - userLon
        val y = sin(deltaLon)
        val x = cos(userLat) * tan(kaabaLat) - sin(userLat) * cos(deltaLon)
        return ((Math.toDegrees(atan2(y, x)) + 360.0) % 360.0).toFloat()
    }

    /**
     * Normalize bearing delta to [-180, 180] range
     */
    fun normalizeDelta(delta: Float): Float {
        return ((delta + 540f) % 360f) - 180f
    }

    /**
     * Format coordinate to 4 decimal places
     */
    // See extension function at bottom of file

    /**
     * Low-pass filter for smooth sensor readings
     */
    fun lowPass(input: FloatArray, output: FloatArray) {
        val alpha = AppConstants.LOW_PASS_FILTER_ALPHA
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
    }

    /**
     * Convert 24-hour time format to 12-hour format with AM/PM
     * Example: "21:24" -> "9:24 PM", "09:30" -> "9:30 AM"
     */
    fun convert24To12HourFormat(time24: String): String {
        return try {
            // Clean string: remove anything in parentheses like "(PKT)"
            val cleanedTime = time24.substringBefore(" (").trim()
            val parts = cleanedTime.split(":")
            if (parts.size < 2) return time24

            val hour = parts[0].toIntOrNull() ?: return time24
            val minute = parts[1]

            val ampm = if (hour >= 12) "PM" else "AM"
            val hour12 = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }

            "$hour12:$minute $ampm"
        } catch (e: Exception) {
            time24
        }
    }

    /**
     * Convert Timings to displayable prayer rows with 12-hour format
     */
    fun getPrayerRows(timings: com.qibla.locatorar.data.models.Timings): List<Pair<String, String>> {
        return listOf(
            "Fajr" to convert24To12HourFormat(timings.fajr),
            "Sunrise" to convert24To12HourFormat(timings.sunrise),
            "Dhuhr" to convert24To12HourFormat(timings.dhuhr),
            "Asr" to convert24To12HourFormat(timings.asr),
            "Maghrib" to convert24To12HourFormat(timings.maghrib),
            "Sunset" to convert24To12HourFormat(timings.sunset),
            "Isha" to convert24To12HourFormat(timings.isha)
        )
    }

    /**
     * Determine the next prayer time (excluding Sunrise)
     * Returns the index of the next prayer, or -1 if no prayer is remaining
     */
    fun getNextPrayerIndex(timings: com.qibla.locatorar.data.models.Timings): Int {
        val currentTime = LocalTime.now()
        val prayerTimings = listOf(
            Pair(0, timings.fajr),      // Fajr
            Pair(2, timings.dhuhr),     // Dhuhr (skipping Sunrise at index 1)
            Pair(3, timings.asr),       // Asr
            Pair(4, timings.maghrib),   // Maghrib
            Pair(6, timings.isha)       // Isha
        )

        for ((index, timeStr) in prayerTimings) {
            try {
                // Clean the time string (e.g. "05:12 (PKT)" -> "05:12")
                val cleanedTime = timeStr.substringBefore(" (").trim()
                val parts = cleanedTime.split(":")
                if (parts.size >= 2) {
                    val hour = parts[0].toIntOrNull() ?: continue
                    val minute = parts[1].toIntOrNull() ?: continue
                    val prayerTime = LocalTime.of(hour, minute)
                    if (currentTime.isBefore(prayerTime)) {
                        return index
                    }
                }
            } catch (e: Exception) {
                // Skip if parsing fails
            }
        }
        return -1 // No upcoming prayer today
    }

    /**
     * Generate safe Islamic reply based on input keywords
     */
    fun generateSafeIslamicReply(input: String): String {
        val normalized = input.lowercase(Locale.US)

        if (AppConstants.BLOCKED_KEYWORDS.any { normalized.contains(it) }) {
            return "I cannot answer fatwa, abusive, romantic, violent, or sensitive personal ruling requests. Please speak with a qualified scholar or a trusted local imam for a proper ruling."
        }

        return when {
            normalized.contains("prayer") || normalized.contains("salah") ->
                "Salah is centered on humility, cleanliness, time, and focus. I can help with general steps and reminders, but specific rulings should go to a qualified scholar."
            normalized.contains("dua") ->
                "A beautiful approach to dua is to praise Allah, send blessings upon the Prophet, ask sincerely, and stay patient with trust in Allah's wisdom."
            normalized.contains("quran") ->
                "For Quran study, read a reliable translation, note the context, and learn tafsir from trusted scholars. I can summarize general themes without issuing rulings."
            normalized.contains("ramadan") || normalized.contains("fast") ->
                "Ramadan worship often combines fasting, prayer, Quran, charity, and character. For medical or legal fasting questions, please ask a scholar and doctor where relevant."
            else ->
                "I can help with general Islamic learning, reminders, duas, prayer basics, and Quranic themes. I avoid issuing fatwas or handling abusive, romantic, or unsafe requests."
        }
    }

    fun getHijriDate(timeZone: String): String {
        val zone = ZoneId.of(timeZone)
        val today = LocalDate.now(zone)

        val formatter = DateTimeFormatter
            .ofLocalizedDate(FormatStyle.FULL)
            .withChronology(HijrahChronology.INSTANCE)
            .withLocale(Locale("en"))

        return formatter.format(today)
    }

    fun getNumberValue(value: Double, noOfFractions: Int): Double {
        var bd = BigDecimal(value)
        bd = bd.setScale(noOfFractions, RoundingMode.HALF_UP)
        return bd.toDouble()
    }
}

// Permission Extensions
fun Context.hasLocationPermission(): Boolean {
    val fineLocation = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    val coarseLocation = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    return fineLocation || coarseLocation
}

// Location Extensions
fun Context.getSavedLocation(): Location? {
    val prefs = getSharedPreferences(AppConstants.PRAYER_CACHE_PREF, Context.MODE_PRIVATE)
    if (!prefs.contains(AppConstants.LOCATION_CACHE_KEY_LAT)) return null
    
    val lat = prefs.getFloat(AppConstants.LOCATION_CACHE_KEY_LAT, 0f).toDouble()
    val lon = prefs.getFloat(AppConstants.LOCATION_CACHE_KEY_LON, 0f).toDouble()
    return Location("saved").apply {
        latitude = lat
        longitude = lon
    }
}

fun Context.saveLocation(location: Location) {
    val prefs = getSharedPreferences(AppConstants.PRAYER_CACHE_PREF, Context.MODE_PRIVATE)
    prefs.edit()
        .putFloat(AppConstants.LOCATION_CACHE_KEY_LAT, location.latitude.toFloat())
        .putFloat(AppConstants.LOCATION_CACHE_KEY_LON, location.longitude.toFloat())
        .apply()
}

suspend fun Context.currentLocation(): Location? = withContext(Dispatchers.IO) {
    if (!hasLocationPermission()) return@withContext null
    
    val client = LocationServices.getFusedLocationProviderClient(this@currentLocation)
    
    // 1. Try last known location (Instant)
    val lastKnown = runCatching { client.lastLocation.await() }.getOrNull()
    if (lastKnown != null) {
        saveLocation(lastKnown)
    }

    // 2. Always try to get a fresh accurate location in background if possible
    // but for now, we return the best we have quickly, 
    // and the screen will call this again or we can just get the accurate one.
    
    val accurateLocation = runCatching {
        client.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            CancellationTokenSource().token
        ).await()
    }.getOrNull()

    if (accurateLocation != null) {
        saveLocation(accurateLocation)
        return@withContext accurateLocation
    }
    
    lastKnown
}

// Coordinate Extensions
fun Double.formatCoord(): String = String.format(Locale.US, "%.4f", this)


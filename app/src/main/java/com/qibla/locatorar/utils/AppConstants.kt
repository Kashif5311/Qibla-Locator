package com.qibla.locatorar.utils

import androidx.compose.ui.graphics.Color

object AppConstants {
    // API
    const val PRAYER_API_BASE_URL = "https://api.aladhan.com/"
    const val PRAYER_API_DEFAULT_METHOD = 3
    const val PRAYER_API_DEFAULT_SHAFAQ = "general"
    const val PRAYER_API_DEFAULT_TUNE = "5,3,5,7,9,-1,0,8,-6"
    const val PRAYER_API_DEFAULT_SCHOOL = 0
    const val PRAYER_API_DEFAULT_MIDNIGHT_MODE = 0
    const val PRAYER_API_DEFAULT_LAT_ADJUSTMENT = 1
    const val PRAYER_API_DEFAULT_CALENDAR_METHOD = "UAQ"

    // Time Formats
    const val DISPLAY_DATE = "EEE, dd MMM yyyy"
    const val DATE_FORMAT_yyyy_MM_dd = "yyyy-MM-dd"
    const val DISPLAY_DATE_FORMAT_FINANCIAL_YEAR = "dd-MM-yyyy"
    const val MMMM_dd_yyyy = "MMMM dd, yyyy"
    const val DATE_FORMAT_ZAKAT_CALCULATION = "MMMM dd, yyyy"
    const val DATE_FORMAT_dd_mm_yyyy = "dd/MM/yyyy"
    const val TIME_FORMAT_ = "HH:mm:ss"
    const val DATE_FORMAT_CRM = "yyyy-MM-dd'T'HH:mm:ss"
    const val TIME_FORMAT_DUBAI_PAY = "yyyy-MM-dd'T'HH:mm:ssXXX"
    const val TIME_FORMAT_YYYY_MM_DD_T_HH_MM_SS_SSS_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    const val TIME_FORMAT_SEND = "HH:mm"
    const val TIME_DISPLAY_ = "hh:mm aa"

    // SharedPreferences
    const val PRAYER_CACHE_PREF = "prayer_cache"
    const val PRAYER_CACHE_KEY_RESPONSE = "response"
    const val PRAYER_CACHE_KEY_FETCH_DATE = "fetch_date"
    const val LOCATION_CACHE_KEY_LAT = "last_lat"
    const val LOCATION_CACHE_KEY_LON = "last_lon"

    // Coordinates
    const val KAABA_LATITUDE = 21.422487
    const val KAABA_LONGITUDE = 39.826206

    // Qibla Finder
    const val QIBLA_CENTERED_DELTA_THRESHOLD = 5f

    // Sensor
    const val LOW_PASS_FILTER_ALPHA = 0.15f

    // Theme
    val LIGHT_PRIMARY = Color(0xFF0F766E)
    val LIGHT_SECONDARY = Color(0xFF7C3AED)
    val LIGHT_TERTIARY = Color(0xFFB45309)
    val LIGHT_BACKGROUND = Color(0xFFF8FAFC)
    val LIGHT_SURFACE = Color(0xFFFFFFFF)
    val LIGHT_SURFACE_VARIANT = Color(0xFFE2E8F0)

    val DARK_PRIMARY = Color(0xFF5EEAD4)
    val DARK_SECONDARY = Color(0xFFC4B5FD)
    val DARK_TERTIARY = Color(0xFFFCD34D)
    val DARK_BACKGROUND = Color(0xFF0B1120)
    val DARK_SURFACE = Color(0xFF111827)
    val DARK_SURFACE_VARIANT = Color(0xFF1F2937)

    // Chat
    const val GEMINI_API_KEY = "AQ.Ab8RN6L_YNias9n4iKm0CNhxteEf-dWRJ2fJr-Ha1phqt8OGCg"
    val BLOCKED_KEYWORDS = listOf(
        "kill", "hate", "abuse", "curse", "romantic", "sex", "dating", "violence", "bomb", "weapon"
    )

    object CalendarType {
        const val HIJRI = "Hijri"
        const val GREGORIAN = "Gregorian"
    }
}


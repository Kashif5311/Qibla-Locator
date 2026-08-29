package com.qibla.locatorar.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.qibla.locatorar.data.models.ChatMessage
import com.qibla.locatorar.data.models.PrayerData
import com.qibla.locatorar.data.models.PrayerResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PreferenceHelper {

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    private const val PREF_NAME = "prayer_preferences"
    private const val KEY_FETCH_DATE = "fetch_date"
    private const val KEY_PRAYER_RESPONSE = "PRAYER_RESPONSE"
    private const val KEY_CALENDAR_DATA = "calendar_data"
    private const val KEY_LATITUDE = "latitude"
    private const val KEY_LONGITUDE = "longitude"
    private const val KEY_CALENDAR_TYPE = "CALENDAR_TYPE"
    private const val KEY_GOLD_PRICE = "GOLD_PRICE"
    private const val KEY_GOLD_UNIT = "GOLD_UNIT"
    private const val KEY_GOLD_CURRENCY = "GOLD_CURRENCY"
    private const val KEY_SAFE_CHAT_MESSAGES = "SAFE_CHAT_MESSAGES"

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // ---- generic get/put helpers, so every method below is one-liner ----

    private fun putString(key: String, value: String) = prefs.edit().putString(key, value).apply()
    private fun getString(key: String): String? = prefs.getString(key, null)

    private inline fun <reified T> getJson(key: String): T? {
        val json = getString(key) ?: return null
        return gson.fromJson(json, T::class.java)
    }

    private fun putJson(key: String, value: Any) = putString(key, gson.toJson(value))

    private fun todayStr(): String = LocalDate.now().format(dateFormatter)

    private fun saveLocation(latitude: Double, longitude: Double) {
        prefs.edit()
            .putString(KEY_FETCH_DATE, LocalDate.now().toString())
            .putFloat(KEY_LATITUDE, latitude.toFloat())
            .putFloat(KEY_LONGITUDE, longitude.toFloat())
            .apply()
    }

    // ---- location ----

    fun getLastLatitude(): Double? =
        if (prefs.contains(KEY_LATITUDE)) prefs.getFloat(KEY_LATITUDE, 0f).toDouble() else null

    fun getLastLongitude(): Double? =
        if (prefs.contains(KEY_LONGITUDE)) prefs.getFloat(KEY_LONGITUDE, 0f).toDouble() else null

    // ---- calendar / prayer data ----

    private fun getCachedCalendar(): List<PrayerData>? {
        val json = getString(KEY_CALENDAR_DATA) ?: return null
        val type = object : TypeToken<List<PrayerData>>() {}.type
        return gson.fromJson(json, type)
    }

    fun fetchedToday(): Boolean {
        if (getString(KEY_FETCH_DATE) == LocalDate.now().toString()) return true
        return getCachedCalendar()?.any { it.date.gregorian.date == todayStr() } == true
    }

    fun getCachedPrayerResponse(): PrayerResponse? {
        val today = todayStr()

        getCachedCalendar()?.find { it.date.gregorian.date == today }?.let {
            return PrayerResponse(200, "OK", it)
        }

        val response = getJson<PrayerResponse>(KEY_PRAYER_RESPONSE) ?: return null
        return response.takeIf { it.data.date.gregorian.date == today }
    }

    fun savePrayerResponse(latitude: Double, longitude: Double, response: PrayerResponse) {
        saveLocation(latitude, longitude)
        putJson(KEY_PRAYER_RESPONSE, response)
    }

    fun saveCalendarResponse(latitude: Double, longitude: Double, data: List<PrayerData>) {
        saveLocation(latitude, longitude)
        putJson(KEY_CALENDAR_DATA, data)
    }

    fun getPreferredCalender(): String =
        getString(KEY_CALENDAR_TYPE) ?: AppConstants.CalendarType.HIJRI

    fun setPreferredCalender(value: String) = putString(KEY_CALENDAR_TYPE, value)

    // ---- Zakat Preferences ----

    fun getGoldPrice(): String = getString(KEY_GOLD_PRICE) ?: ""
    fun setGoldPrice(value: String) = putString(KEY_GOLD_PRICE, value)

    fun getGoldUnit(): String = getString(KEY_GOLD_UNIT) ?: "1 Gram"
    fun setGoldUnit(value: String) = putString(KEY_GOLD_UNIT, value)

    fun getCurrency(): String = getString(KEY_GOLD_CURRENCY) ?: "PKR"
    fun setCurrency(value: String) = putString(KEY_GOLD_CURRENCY, value)

    // ---- Safe Chat ----

    fun getSafeChatMessages(): List<ChatMessage> {
        val json = getString(KEY_SAFE_CHAT_MESSAGES) ?: return emptyList()

        val type = object : TypeToken<List<ChatMessage>>() {}.type

        return try {
            gson.fromJson<List<ChatMessage>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveSafeChatMessages(messages: List<ChatMessage>) {
        putJson(KEY_SAFE_CHAT_MESSAGES, messages)
    }

    fun clearSafeChatMessages() {
        prefs.edit()
            .remove(KEY_SAFE_CHAT_MESSAGES)
            .apply()
    }
}
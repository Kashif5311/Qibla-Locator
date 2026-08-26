package com.qibla.locatorar.data.models

import com.google.gson.annotations.SerializedName

data class PrayerResponse(
    val code: Int,
    val status: String,
    val data: PrayerData
)

data class PrayerData(
    val timings: Timings,
    val date: PrayerDate,
    val meta: PrayerMeta
)

data class Timings(
    @SerializedName("Fajr") val fajr: String,
    @SerializedName("Sunrise") val sunrise: String,
    @SerializedName("Dhuhr") val dhuhr: String,
    @SerializedName("Asr") val asr: String,
    @SerializedName("Maghrib") val maghrib: String,
    @SerializedName("Sunset") val sunset: String,
    @SerializedName("Isha") val isha: String
)

data class PrayerDate(
    val readable: String,
    val hijri: HijriDate,
    val gregorian: GregorianDate
)

data class HijriDate(
    val date: String,
    val month: HijriMonth,
    val year: String
)

data class HijriMonth(
    val en: String
)

data class GregorianDate(
    val date: String,
    val weekday: Weekday
)

data class Weekday(
    val en: String
)

data class PrayerMeta(
    val latitude: Double,
    val longitude: Double,
    val timezone: String
)


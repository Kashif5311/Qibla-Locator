package com.qibla.locatorar.network.repository

import android.content.Context
import android.location.Location
import com.google.gson.Gson
import com.qibla.locatorar.data.models.PrayerResponse
import com.qibla.locatorar.network.api.PrayerApi
import com.qibla.locatorar.utils.PreferenceHelper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class PrayerRepository {

    companion object {
        private const val LOCATION_THRESHOLD_METERS = 1000f

        private val api: PrayerApi by lazy {
            Retrofit.Builder()
                .baseUrl("https://api.aladhan.com/")
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor(HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BASIC
                        })
                        .build()
                )
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PrayerApi::class.java)
        }
    }

    suspend fun refreshIfNeeded(
        latitude: Double,
        longitude: Double
    ): PrayerResponse? {

        val sameLocation = isSameLocation(
            latitude,
            longitude,
            PreferenceHelper.getLastLatitude(),
            PreferenceHelper.getLastLongitude()
        )

        if (PreferenceHelper.fetchedToday() && sameLocation) {
            return PreferenceHelper.getCachedPrayerResponse()
        }

        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US)

        return try {
            val response = api.getTimings(
                date = LocalDate.now().format(formatter),
                latitude = latitude,
                longitude = longitude
            )
            PreferenceHelper.savePrayerResponse(latitude, longitude, response)
            response
        } catch (e: Exception) {
            null
        }
    }

    private fun isSameLocation(
        currentLat: Double,
        currentLng: Double,
        lastLat: Double?,
        lastLng: Double?
    ): Boolean {

        if (lastLat == null || lastLng == null) return false

        val results = FloatArray(1)
        Location.distanceBetween(
            currentLat,
            currentLng,
            lastLat,
            lastLng,
            results
        )

        return results[0] < LOCATION_THRESHOLD_METERS
    }

    public fun readCached(): PrayerResponse? {
        return PreferenceHelper.getCachedPrayerResponse()
    }
}
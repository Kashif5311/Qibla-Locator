package com.qibla.locatorar.network.api

import com.qibla.locatorar.data.models.PrayerData
import com.qibla.locatorar.data.models.PrayerResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PrayerApi {
    @GET("v1/timings/{date}")
    suspend fun getTimings(
        @Path("date") date: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 3,
        @Query("shafaq") shafaq: String = "general",
        @Query("tune") tune: String = "5,3,5,7,9,-1,0,8,-6",
        @Query("school") school: Int = 0,
        @Query("midnightMode") midnightMode: Int = 0,
        @Query("latitudeAdjustmentMethod") latitudeAdjustmentMethod: Int = 1,
        @Query("calendarMethod") calendarMethod: String = "UAQ",
        @Query("iso8601") iso8601: Boolean = false
    ): PrayerResponse

    @GET("v1/calendar")
    suspend fun getCalendar(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("month") month: Int,
        @Query("year") year: Int,
        @Query("method") method: Int = 3,
        @Query("tune") tune: String = "5,3,5,7,9,-1,0,8,-6"
    ): CalendarResponse
}

data class CalendarResponse(
    val code: Int,
    val status: String,
    val data: List<PrayerData>
)



package com.qibla.locatorar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qibla.locatorar.data.models.PrayerResponse
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.util.Locale

@Composable
fun DateCard() {
    val today = LocalDate.now()
    val hijriDate = HijrahDate.from(today)


    val gregorianFormatter = DateTimeFormatter.ofPattern(
        "dd MMMM yyyy",
        Locale.ENGLISH
    )

    val weekdayFormatter = DateTimeFormatter.ofPattern(
        "EEEE",
        Locale.ENGLISH
    )

    val hijriDay = hijriDate.get(ChronoField.DAY_OF_MONTH)
    val hijriMonth = hijriDate.get(ChronoField.MONTH_OF_YEAR)
    val hijriYear = hijriDate.get(ChronoField.YEAR_OF_ERA)

    val hijriMonthName = listOf(
        "Muharram",
        "Safar",
        "Rabi al-Awwal",
        "Rabi al-Thani",
        "Jumada al-Awwal",
        "Jumada al-Thani",
        "Rajab",
        "Sha'ban",
        "Ramadan",
        "Shawwal",
        "Dhu al-Qi'dah",
        "Dhu al-Hijjah"
    ).getOrNull(hijriMonth - 1) ?: ""

    val timezone = ZoneId.systemDefault().id

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                today.format(weekdayFormatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                "${today.format(gregorianFormatter)} | " +
                        "$hijriDay $hijriMonthName $hijriYear AH"
            )

            Text(
                "Timezone: $timezone",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun PrayerTimeRow(name: String, time: String, isNext: Boolean = false) {
    val textColor = if (isNext) Color(0xFFDE880F) else Color.Unspecified

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, style = MaterialTheme.typography.titleMedium, color = textColor)
            Text(time, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}


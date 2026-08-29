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

import androidx.compose.ui.res.stringResource
import com.qibla.locatorar.R

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

    val hijriMonthName = when (hijriMonth) {
        1 -> stringResource(R.string.hijri_month_1)
        2 -> stringResource(R.string.hijri_month_2)
        3 -> stringResource(R.string.hijri_month_3)
        4 -> stringResource(R.string.hijri_month_4)
        5 -> stringResource(R.string.hijri_month_5)
        6 -> stringResource(R.string.hijri_month_6)
        7 -> stringResource(R.string.hijri_month_7)
        8 -> stringResource(R.string.hijri_month_8)
        9 -> stringResource(R.string.hijri_month_9)
        10 -> stringResource(R.string.hijri_month_10)
        11 -> stringResource(R.string.hijri_month_11)
        12 -> stringResource(R.string.hijri_month_12)
        else -> ""
    }

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
                stringResource(R.string.timezone_label, timezone),
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


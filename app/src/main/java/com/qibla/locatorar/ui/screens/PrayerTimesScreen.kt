package com.qibla.locatorar.ui.screens

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.qibla.locatorar.data.models.PrayerUiState
import com.qibla.locatorar.network.repository.PrayerRepository
import com.qibla.locatorar.ui.components.DateCard
import com.qibla.locatorar.ui.components.EmptyPanel
import com.qibla.locatorar.ui.components.PrayerTimeRow
import com.qibla.locatorar.utils.AppUtils
import com.qibla.locatorar.utils.currentLocation
import com.qibla.locatorar.utils.formatCoord
import com.qibla.locatorar.utils.getSavedLocation
import com.qibla.locatorar.utils.hasLocationPermission
import kotlinx.coroutines.launch

import androidx.compose.ui.res.stringResource
import com.qibla.locatorar.R

@Composable
fun PrayerTimesScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { PrayerRepository() }
    var state by remember { mutableStateOf(PrayerUiState(cached = repository.readCached())) }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            scope.launch { loadPrayerTimes(context, repository) { state = it } }
        } else {
            state = state.copy(message = context.getString(R.string.location_permission_denied_msg))
        }
    }

    var showLocationPermissionDialog by remember { mutableStateOf(false) }
    val hasPermission = context.hasLocationPermission()

    if (showLocationPermissionDialog && !hasPermission) {
        AlertDialog(
            onDismissRequest = { showLocationPermissionDialog = false },
            title = { Text(stringResource(R.string.location_permission_required)) },
            text = { Text(stringResource(R.string.location_permission_rationale)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLocationPermissionDialog = false
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) { Text(stringResource(R.string.allow)) }
            },
            dismissButton = {
                TextButton(onClick = { showLocationPermissionDialog = false }) {
                    Text(stringResource(R.string.not_now))
                }
            }
        )
    }

    // Load saved location immediately for fast startup
    LaunchedEffect(Unit) {
        val savedLocation = context.getSavedLocation()
        if (savedLocation != null) {
            repository.refreshIfNeeded(savedLocation.latitude, savedLocation.longitude)
            state = state.copy(
                cached = repository.readCached(),
                locationText = "Lat ${savedLocation.latitude.formatCoord()}, Lon ${savedLocation.longitude.formatCoord()}"
            )
        }
        
        state = state.copy(
            cached = repository.readCached(),
            message = if (repository.readCached() == null && savedLocation == null) 
                context.getString(R.string.request_location_msg) else ""
        )
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            loadPrayerTimes(context, repository) { state = it }
        } else {
            showLocationPermissionDialog = true
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            DateCard()
        }
        state.cached?.let { response ->
            val nextPrayerIndex = AppUtils.getNextPrayerIndex(response.data.timings)
            val prayerRows = AppUtils.getPrayerRows(response.data.timings)
            items(prayerRows.size) { index ->
                val (name, time) = prayerRows[index]
                PrayerTimeRow(name, time, isNext = index == nextPrayerIndex)
            }
        } ?: item {
            EmptyPanel(stringResource(R.string.no_prayer_times_available))
        }
    }


    if (state.isLoading) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.updating_prayer_times),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private suspend fun loadPrayerTimes(
    context: Context,
    repository: PrayerRepository,
    update: (PrayerUiState) -> Unit
) {
    update(
        PrayerUiState(
            cached = repository.readCached(),
            isLoading = true,
            message = context.getString(R.string.getting_location)
        )
    )
    val location = context.currentLocation()
    if (location == null) {
        update(
            PrayerUiState(
                cached = repository.readCached(),
                isLoading = false,
                message = context.getString(R.string.unable_read_location)
            )
        )
        return
    }
    update(
        PrayerUiState(
            cached = repository.readCached(),
            isLoading = true,
            message = context.getString(R.string.checking_cache),
            locationText = "Lat ${location.latitude.formatCoord()}, Lon ${location.longitude.formatCoord()}"
        )
    )
    runCatching {
        repository.refreshIfNeeded(location.latitude, location.longitude)
    }.onSuccess { latest ->
        update(
            PrayerUiState(
                cached = latest ?: repository.readCached(),
                isLoading = false,
                message = "",
                locationText = "Lat ${location.latitude.formatCoord()}, Lon ${location.longitude.formatCoord()}"
            )
        )
    }.onFailure { error ->
        update(
            PrayerUiState(
                cached = repository.readCached(),
                isLoading = false,
                message = context.getString(R.string.refresh_failed_msg, error.message.orEmpty()),
                locationText = "Lat ${location.latitude.formatCoord()}, Lon ${location.longitude.formatCoord()}"
            )
        )
    }
}


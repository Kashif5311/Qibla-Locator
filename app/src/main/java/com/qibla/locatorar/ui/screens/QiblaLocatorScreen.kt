package com.qibla.locatorar.ui.screens

import android.Manifest
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.qibla.locatorar.ui.components.HeroPanel
import com.qibla.locatorar.ui.components.QiblaCompassWheel
import com.qibla.locatorar.ui.components.rememberCompassState
import com.qibla.locatorar.ui.components.CalibrationDialog
import com.qibla.locatorar.utils.AppConstants
import com.qibla.locatorar.utils.AppUtils
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.graphics.Color
import com.qibla.locatorar.utils.currentLocation
import com.qibla.locatorar.utils.formatCoord
import com.qibla.locatorar.utils.getSavedLocation
import com.qibla.locatorar.utils.hasLocationPermission
import kotlinx.coroutines.launch

import androidx.compose.ui.res.stringResource
import com.qibla.locatorar.R

@Composable
fun QiblaLocatorScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hasLocationPermission by remember { mutableStateOf(context.hasLocationPermission()) }
    var location by remember { mutableStateOf<Location?>(context.getSavedLocation()) }
    
    val compassState = rememberCompassState()
    val heading = compassState.heading
    val accuracy = compassState.accuracy
    
    var showCalibrationDialog by remember { mutableStateOf(true) }

    val qiblaBearing = location?.let { AppUtils.calculateQiblaBearing(it.latitude, it.longitude) } ?: 0f

    val delta = AppUtils.normalizeDelta(qiblaBearing - heading)
    val (guidance, icon, iconColor) = when {
        location == null -> Triple(stringResource(R.string.waiting_for_location), Icons.Default.LocationOn, MaterialTheme.colorScheme.outline)
        kotlin.math.abs(delta) <= AppConstants.QIBLA_CENTERED_DELTA_THRESHOLD -> Triple(stringResource(R.string.qibla_found), Icons.Default.CheckCircle, Color(0xFF22C55E))
        delta > 0 -> {
            if (delta > 15f) Triple(stringResource(R.string.turn_right), Icons.AutoMirrored.Filled.ArrowForward, MaterialTheme.colorScheme.primary)
            else Triple(stringResource(R.string.turn_slightly_right), Icons.AutoMirrored.Filled.KeyboardArrowRight, MaterialTheme.colorScheme.secondary)
        }
        else -> {
            if (delta < -15f) Triple(stringResource(R.string.turn_left), Icons.AutoMirrored.Filled.ArrowBack, MaterialTheme.colorScheme.primary)
            else Triple(stringResource(R.string.turn_slightly_left), Icons.AutoMirrored.Filled.KeyboardArrowLeft, MaterialTheme.colorScheme.secondary)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                    context.hasLocationPermission()
        if (hasLocationPermission) {
            scope.launch { 
                val newLoc = context.currentLocation()
                if (newLoc != null) location = newLoc
            }
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val newLoc = context.currentLocation()
            if (newLoc != null) location = newLoc
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (showCalibrationDialog) {
            CalibrationDialog(accuracy = accuracy, onDismiss = { showCalibrationDialog = false })
        }

        HeroPanel(
            title = guidance,
            subtitle = "",
            icon = icon,
            iconColor = iconColor
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            // Interactive Qibla Compass Wheel Dial
            QiblaCompassWheel(
                heading = heading,
                qiblaBearing = qiblaBearing,
                modifier = Modifier
                    .size(300.dp)
                    .padding(8.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!hasLocationPermission) {
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Text(stringResource(R.string.grant_location))
                }
            }
//            FilledTonalButton(onClick = { scope.launch { location = context.currentLocation() } }) {
//                Text("Refresh Location")
//            }
        }

        Text(
//            text = location?.let { "Current location: ${it.latitude.formatCoord()}, ${it.longitude.formatCoord()}" }
            text = location?.let { "" }
                ?: stringResource(R.string.location_required_qibla),
            style = MaterialTheme.typography.bodyMedium,
            color = if (location == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

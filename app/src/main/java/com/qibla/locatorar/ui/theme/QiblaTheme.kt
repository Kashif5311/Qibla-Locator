package com.qibla.locatorar.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.qibla.locatorar.utils.AppConstants

fun qiblaLightScheme() = lightColorScheme(
    primary = AppConstants.LIGHT_PRIMARY,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF042F2E),
    secondary = AppConstants.LIGHT_SECONDARY,
    secondaryContainer = Color(0xFFEDE9FE),
    tertiary = AppConstants.LIGHT_TERTIARY,
    background = AppConstants.LIGHT_BACKGROUND,
    surface = AppConstants.LIGHT_SURFACE,
    surfaceVariant = AppConstants.LIGHT_SURFACE_VARIANT,
    outline = Color(0xFF64748B)
)

fun qiblaDarkScheme() = darkColorScheme(
    primary = AppConstants.DARK_PRIMARY,
    onPrimary = Color(0xFF042F2E),
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = AppConstants.DARK_SECONDARY,
    secondaryContainer = Color(0xFF4C1D95),
    tertiary = AppConstants.DARK_TERTIARY,
    background = AppConstants.DARK_BACKGROUND,
    surface = AppConstants.DARK_SURFACE,
    surfaceVariant = AppConstants.DARK_SURFACE_VARIANT,
    outline = Color(0xFF94A3B8)
)


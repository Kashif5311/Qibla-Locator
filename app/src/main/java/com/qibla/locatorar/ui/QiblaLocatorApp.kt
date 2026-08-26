package com.qibla.locatorar.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qibla.locatorar.data.models.AppTab
import com.qibla.locatorar.ui.components.NavMark
import com.qibla.locatorar.ui.screens.PrayerTimesScreen
import com.qibla.locatorar.ui.screens.QiblaLocatorScreen
import com.qibla.locatorar.ui.screens.SafeChatScreen
import com.qibla.locatorar.ui.screens.ZakatCalculatorScreen
import com.qibla.locatorar.ui.theme.qiblaDarkScheme
import com.qibla.locatorar.ui.theme.qiblaLightScheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaLocatorApp() {
    var darkMode by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(AppTab.Prayer) }

    MaterialTheme(
        colorScheme = if (darkMode) qiblaDarkScheme() else qiblaLightScheme()
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Qibla Finder", fontWeight = FontWeight.Bold)
                            Text(
                                "Prayer, Qibla, Zakat Calculation",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    },
                    actions = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Switch(checked = darkMode, onCheckedChange = { darkMode = it })
                            Text(if (darkMode) "Dark" else "Light", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    AppTab.entries.forEach { tab ->
                        val icon = when (tab) {
                            AppTab.Prayer -> Icons.Default.Notifications
                            AppTab.Qibla -> Icons.Default.LocationOn
                            AppTab.Zakat -> Icons.Default.ShoppingCart
                            AppTab.Chat -> Icons.Default.Info
                        }
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { NavMark(icon, selectedTab == tab) },
                            label = { Text(tab.title) }
                        )
                    }
                }
            }
        ) { padding ->
            AnimatedContent(
                targetState = selectedTab,
                label = "tab-content",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) { tab ->
                when (tab) {
                    AppTab.Prayer -> PrayerTimesScreen()
                    AppTab.Qibla -> QiblaLocatorScreen()
                    AppTab.Zakat -> ZakatCalculatorScreen()
                    AppTab.Chat -> SafeChatScreen()
                }
            }
        }
    }
}


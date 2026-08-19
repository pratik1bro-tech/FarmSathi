package com.example.features.weather

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.*
import com.example.data.repository.FarmRepository
import com.example.designsystem.components.*
import com.example.designsystem.theme.FarmSathiDesign
import com.example.designsystem.theme.FarmSemanticStatus
import com.example.ui.theme.*

@Composable
fun WeatherScreen(
    repository: FarmRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: WeatherViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return WeatherViewModel(
                    application = context.applicationContext as Application,
                    repository = repository
                ) as T
            }
        }
    )
    WeatherScreen(viewModel = viewModel, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    var zoneMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            WeatherTopAppBar(
                state = state,
                onBack = onBack,
                onRefresh = { viewModel.loadWeatherData(isPullToRefresh = true) },
                onToggleOffline = { viewModel.toggleOfflineMode(!state.isOffline) },
                onZoneClick = { zoneMenuExpanded = true }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.loadWeatherData(isPullToRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading && !state.isRefreshing -> {
                    FarmLoadingState(
                        message = "Loading microclimate weather & agronomic impact analysis...",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                state.errorMessage != null -> {
                    FarmErrorState(
                        title = "Weather Intelligence Sync Error",
                        description = state.errorMessage ?: "Failed to reach weather station telemetry.",
                        onRetry = { viewModel.loadWeatherData() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("weather_scrollable_column"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Zone Selector & Sync Status Header
                        item {
                            WeatherZoneAndSyncHeader(
                                selectedZone = state.selectedFarmZone,
                                availableZones = state.availableFarmZones,
                                lastUpdated = state.lastUpdatedText,
                                isOffline = state.isOffline,
                                isCached = state.isCachedData,
                                zoneMenuExpanded = zoneMenuExpanded,
                                onZoneMenuToggle = { zoneMenuExpanded = it },
                                onZoneSelected = {
                                    viewModel.selectFarmZone(it)
                                    zoneMenuExpanded = false
                                },
                                onOfflineToggle = { viewModel.toggleOfflineMode(!state.isOffline) }
                            )
                        }

                        // Offline Notice Banner if offline mode is active
                        if (state.isOffline || state.isCachedData) {
                            item {
                                WeatherOfflineBanner(
                                    lastUpdated = state.lastUpdatedText,
                                    onReconnect = { viewModel.toggleOfflineMode(false) }
                                )
                            }
                        }

                        // 2. View Tab Segmented Bar: Today | Tomorrow | 7 Days
                        item {
                            WeatherTabSegmentedBar(
                                selectedTab = state.selectedTab,
                                onTabSelected = { viewModel.selectTab(it) }
                            )
                        }

                        // 3. Tab-Specific Content
                        when (state.selectedTab) {
                            WeatherViewTab.TODAY -> {
                                // A. Main Today Weather Hero Card
                                item {
                                    TodayWeatherHeroCard(state = state)
                                }

                                // B. 4 Key Microclimate Metrics Grid
                                item {
                                    MicroclimateMetricsGrid(state = state)
                                }

                                // C. Agriculture Impact Section (Core user requirement)
                                item {
                                    AgricultureImpactSection(
                                        title = "Agriculture Impact & Advisories",
                                        subtitle = "Real-time agronomic recommendations backed by ICAR microclimate guidelines",
                                        impacts = state.todayImpacts
                                    )
                                }

                                // D. Farm Operations Suitability Matrix
                                item {
                                    FarmOperationsMatrixCard(matrix = state.operationsMatrix)
                                }

                                // E. Hourly Forecast & Spray Window Timeline
                                item {
                                    HourlyForecastSection(
                                        title = "Today's Hourly Weather & Spray Window",
                                        forecasts = state.todayHourlyForecast
                                    )
                                }

                                // F. AI Agronomic Synthesis & Voice Readout
                                item {
                                    AiAgronomicAdvisoryCard(
                                        advisoryEn = state.aiAdvisorySummaryEn,
                                        advisoryHi = state.aiAdvisorySummaryHi,
                                        isGenerating = state.isGeneratingAiAdvisory,
                                        isSpeaking = state.isSpeakingAdvisory,
                                        onGenerateAi = { viewModel.generateAiAgronomicAdvisory() },
                                        onSpeakToggle = {
                                            if (state.isSpeakingAdvisory) {
                                                viewModel.stopSpeaking()
                                            } else {
                                                viewModel.speakAdvisory(isHindi = false)
                                            }
                                        },
                                        onSpeakHindi = {
                                            if (state.isSpeakingAdvisory) {
                                                viewModel.stopSpeaking()
                                            } else {
                                                viewModel.speakAdvisory(isHindi = true)
                                            }
                                        }
                                    )
                                }
                            }

                            WeatherViewTab.TOMORROW -> {
                                // Tomorrow's Hero Card
                                item {
                                    TomorrowWeatherHeroCard(
                                        forecast = state.sevenDayForecast.getOrNull(1)
                                            ?: state.sevenDayForecast.firstOrNull(),
                                        state = state
                                    )
                                }

                                // Tomorrow's Agriculture Impact Section
                                item {
                                    AgricultureImpactSection(
                                        title = "Tomorrow's Agronomic Action Plan",
                                        subtitle = "Field activity feasibility & preventive measures for upcoming conditions",
                                        impacts = state.tomorrowImpacts
                                    )
                                }

                                // Tomorrow's Hourly Forecast Timeline
                                item {
                                    HourlyForecastSection(
                                        title = "Tomorrow's Hourly Forecast Timeline",
                                        forecasts = state.tomorrowHourlyForecast
                                    )
                                }

                                // Preparation Checklist for Tonight
                                item {
                                    TomorrowPreparationChecklistCard()
                                }
                            }

                            WeatherViewTab.SEVEN_DAYS -> {
                                // 7-Day Trend Overview Banner
                                item {
                                    SevenDayTrendHeroCard(forecasts = state.sevenDayForecast)
                                }

                                // 7-Day Strategic Planning Windows
                                item {
                                    SevenDayStrategicWindowsCard()
                                }

                                // Agriculture Impact for Weekly Trends
                                item {
                                    AgricultureImpactSection(
                                        title = "7-Day Agronomic Outlook",
                                        subtitle = "Weekly crop protection, nutrient scheduling, and disease vulnerability",
                                        impacts = state.weeklyImpacts
                                    )
                                }

                                // 7 Day-by-Day Forecast Detailed Cards
                                item {
                                    Text(
                                        text = "Day-by-Day Agricultural Forecast",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                items(state.sevenDayForecast) { dayForecast ->
                                    DailyWeatherForecastCard(forecast = dayForecast)
                                }
                            }
                        }

                        // Bottom Spacer for clean navigation clearance
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TOP APP BAR ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherTopAppBar(
    state: WeatherIntelligenceUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onToggleOffline: () -> Unit,
    onZoneClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(0.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(44.dp).testTag("weather_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to previous screen",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Weather Intelligence",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (state.isOffline) MaterialTheme.colorScheme.errorContainer else FarmTechBlueContainer
                        ) {
                            Text(
                                text = if (state.isOffline) "OFFLINE" else "LIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isOffline) MaterialTheme.colorScheme.error else FarmTechBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp
                            )
                        }
                    }
                    Text(
                        text = "Microclimate & Agronomic Impact",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Offline Simulator Button
                IconButton(
                    onClick = onToggleOffline,
                    modifier = Modifier.size(40.dp).testTag("weather_offline_toggle"),
                ) {
                    Icon(
                        imageVector = if (state.isOffline) Icons.Default.CloudOff else Icons.Default.CloudQueue,
                        contentDescription = "Toggle Offline Mode",
                        tint = if (state.isOffline) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Sync / Refresh Button
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(40.dp).testTag("weather_refresh_button"),
                    enabled = !state.isRefreshing
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync Weather Data",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = if (state.isRefreshing) Modifier.rotate(rotation) else Modifier
                    )
                }
            }
        }
    }
}

// ---------------- ZONE SELECTOR & SYNC HEADER ----------------

@Composable
private fun WeatherZoneAndSyncHeader(
    selectedZone: String,
    availableZones: List<String>,
    lastUpdated: String,
    isOffline: Boolean,
    isCached: Boolean,
    zoneMenuExpanded: Boolean,
    onZoneMenuToggle: (Boolean) -> Unit,
    onZoneSelected: (String) -> Unit,
    onOfflineToggle: () -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), FarmSathiDesign.shapes.large)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Farm Zone Dropdown Selector
                Box {
                    Surface(
                        onClick = { onZoneMenuToggle(true) },
                        shape = FarmSathiDesign.shapes.pill,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.testTag("weather_zone_selector")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedZone.split("(").firstOrNull()?.trim() ?: selectedZone,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select farm plot",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = zoneMenuExpanded,
                        onDismissRequest = { onZoneMenuToggle(false) }
                    ) {
                        availableZones.forEach { zone ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = zone,
                                        fontWeight = if (zone == selectedZone) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Park, contentDescription = null, tint = FarmPrimaryGreen)
                                },
                                onClick = { onZoneSelected(zone) }
                            )
                        }
                    }
                }

                // Last Updated Time Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Updated $lastUpdated",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ---------------- OFFLINE BANNER ----------------

@Composable
private fun WeatherOfflineBanner(
    lastUpdated: String,
    onReconnect: () -> Unit
) {
    Surface(
        shape = FarmSathiDesign.shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f), FarmSathiDesign.shapes.medium)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Offline Mode (Cached Data)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Showing last synced microclimate forecast ($lastUpdated)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f),
                        fontSize = 11.sp
                    )
                }
            }

            TextButton(
                onClick = onReconnect,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "RETRY SYNC",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ---------------- VIEW TAB SEGMENTED BAR ----------------

@Composable
private fun WeatherTabSegmentedBar(
    selectedTab: WeatherViewTab,
    onTabSelected: (WeatherViewTab) -> Unit
) {
    Surface(
        shape = FarmSathiDesign.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), FarmSathiDesign.shapes.extraLarge)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            WeatherViewTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                Surface(
                    onClick = { onTabSelected(tab) },
                    shape = FarmSathiDesign.shapes.pill,
                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("weather_tab_${tab.name.lowercase()}")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val icon = when (tab) {
                                WeatherViewTab.TODAY -> Icons.Default.WbSunny
                                WeatherViewTab.TOMORROW -> Icons.Default.Schedule
                                WeatherViewTab.SEVEN_DAYS -> Icons.Default.CalendarMonth
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.labelEn,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TODAY WEATHER HERO CARD ----------------

@Composable
private fun TodayWeatherHeroCard(state: WeatherIntelligenceUiState) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clip(FarmSathiDesign.shapes.extraLarge)
            .testTag("weather_today_hero_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF004D40), // Deep Forest Teal
                            Color(0xFF00695C),
                            Color(0xFF00382E)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                // Top Row: Location & Condition Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CURRENT MICROCLIMATE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.75f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = state.selectedFarmZone.split("(").firstOrNull()?.trim() ?: "Indore Agri Zone",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = FarmSathiDesign.shapes.pill,
                        color = Color.White.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = getWeatherIcon(state.conditionType),
                                contentDescription = null,
                                tint = HarvestGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = state.conditionType.titleEn,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Temperature Big Display Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "${state.currentTempC.toInt()}°",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 64.sp
                            )
                            Text(
                                text = "C",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
                        Text(
                            text = "Feels like ${state.feelsLikeC.toInt()}°C • Max ${state.tempMaxTodayC}° / Min ${state.tempMinTodayC}°",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getWeatherIcon(state.conditionType),
                            contentDescription = state.weatherCondition,
                            tint = if (state.conditionType.isRainy) SkyWaterBlue else HarvestGold,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Condition Summary Banner
                Surface(
                    shape = FarmSathiDesign.shapes.medium,
                    color = Color.Black.copy(alpha = 0.25f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.weatherCondition,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// ---------------- 4 KEY MICROCLIMATE METRICS GRID ----------------

@Composable
private fun MicroclimateMetricsGrid(state: WeatherIntelligenceUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Row 1: Temperature & Humidity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MicroclimateMetricCard(
                title = "Temperature",
                value = "${state.currentTempC.toInt()}°C",
                subtitle = "Feels like ${state.feelsLikeC.toInt()}°C",
                icon = Icons.Default.Thermostat,
                iconTint = if (state.currentTempC >= 32.0) FarmWarningOrange else FarmPrimaryGreen,
                statusTag = if (state.currentTempC >= 32.0) "High Heat" else "Moderate",
                modifier = Modifier.weight(1f)
            )

            MicroclimateMetricCard(
                title = "Humidity",
                value = "${state.humidityPercent}%",
                subtitle = "Dew Pt: ${state.dewPointC.toInt()}°C",
                icon = Icons.Default.WaterDrop,
                iconTint = if (state.humidityPercent >= 75) FarmAlertRed else SkyWaterBlue,
                statusTag = if (state.humidityPercent >= 75) "Disease Risk" else "Optimal",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Rain Probability & Wind
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MicroclimateMetricCard(
                title = "Rain Probability",
                value = "${state.rainProbabilityPercent}%",
                subtitle = "${state.rainfallExpectedMm} mm Expected",
                icon = Icons.Default.CloudQueue,
                iconTint = if (state.rainProbabilityPercent >= 60) FarmAlertRed else SkyWaterBlue,
                statusTag = if (state.rainProbabilityPercent >= 60) "Heavy Rain" else "Low Rain",
                modifier = Modifier.weight(1f)
            )

            MicroclimateMetricCard(
                title = "Wind Velocity",
                value = "${state.windSpeedKmh.toInt()} km/h",
                subtitle = "${state.windDirection} • Gusts ${state.windGustKmh.toInt()}",
                icon = Icons.Default.Air,
                iconTint = if (state.windSpeedKmh >= 15.0) FarmWarningOrange else FarmPrimaryGreen,
                statusTag = if (state.windSpeedKmh >= 15.0) "Spray Drift" else "Safe Wind",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MicroclimateMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    statusTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), FarmSathiDesign.shapes.large)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = FarmSathiDesign.shapes.pill,
                    color = iconTint.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = statusTag,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = iconTint,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------- AGRICULTURE IMPACT SECTION ----------------

@Composable
private fun AgricultureImpactSection(
    title: String,
    subtitle: String,
    impacts: List<AgricultureImpactItem>
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Agriculture,
                        contentDescription = null,
                        tint = FarmPrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }

        if (impacts.isEmpty()) {
            FarmEmptyState(
                title = "No Critical Weather Warnings",
                description = "Weather conditions are optimal for normal field operations."
            )
        } else {
            impacts.forEach { impact ->
                AgricultureImpactCard(impact = impact)
            }
        }
    }
}

@Composable
private fun AgricultureImpactCard(impact: AgricultureImpactItem) {
    var expanded by remember { mutableStateOf(false) }

    val (headerBg, borderColor, iconTint) = when (impact.severity) {
        ImpactSeverity.CRITICAL -> Triple(FarmAlertRedContainer.copy(alpha = 0.4f), FarmAlertRed, FarmAlertRed)
        ImpactSeverity.WARNING -> Triple(FarmWarningContainer.copy(alpha = 0.4f), FarmWarningOrange, FarmWarningOrange)
        ImpactSeverity.ADVISORY -> Triple(FarmTechBlueContainer.copy(alpha = 0.5f), FarmTechBlue, FarmTechBlue)
        ImpactSeverity.FAVORABLE -> Triple(FarmContainerGreen.copy(alpha = 0.6f), FarmPrimaryGreen, FarmPrimaryGreen)
    }

    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor.copy(alpha = 0.4f), FarmSathiDesign.shapes.extraLarge)
            .clickable { expanded = !expanded }
            .testTag("agri_impact_card_${impact.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category Pill & Severity Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val categoryIcon = when (impact.category) {
                        AgriImpactCategory.FERTILIZER -> Icons.Default.Science
                        AgriImpactCategory.DISEASE_RISK -> Icons.Default.Coronavirus
                        AgriImpactCategory.IRRIGATION -> Icons.Default.WaterDrop
                        AgriImpactCategory.SPRAYING -> Icons.Default.Sanitizer
                        AgriImpactCategory.HARVEST_STORAGE -> Icons.Default.Inventory
                        AgriImpactCategory.HEAT_STRESS -> Icons.Default.WbSunny
                    }
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = impact.category.titleEn,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = iconTint
                    )
                }

                Surface(
                    shape = FarmSathiDesign.shapes.pill,
                    color = headerBg
                ) {
                    Text(
                        text = impact.severity.labelEn,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = iconTint,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Impact Title (e.g. "Heavy Rain Expected → Avoid Fertilizer Application")
            Text(
                text = impact.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Farmer Action Box
            Surface(
                shape = FarmSathiDesign.shapes.medium,
                color = headerBg.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Recommended Farmer Action:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = impact.farmerAction,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Expandable Scientific Rationale
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Agronomic Scientific Basis:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = impact.scientificRationale,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Trigger: ${impact.weatherTrigger}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Timing: ${impact.timingRecommendation}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = iconTint
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Tap to expand / collapse prompt
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "Show Less" else "Why? (Scientific Rationale)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ---------------- FARM OPERATIONS MATRIX CARD ----------------

@Composable
private fun FarmOperationsMatrixCard(matrix: FarmOperationsMatrix) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), FarmSathiDesign.shapes.extraLarge)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EventAvailable,
                    contentDescription = null,
                    tint = FarmPrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Today's Field Activity Feasibility Matrix",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MatrixActivityRow(
                    activity = "Foliar Spraying",
                    status = matrix.sprayingSuitability,
                    timing = matrix.sprayingTiming,
                    icon = Icons.Default.Sanitizer
                )
                MatrixActivityRow(
                    activity = "Fertilizer Top-Dressing",
                    status = matrix.fertilizerSuitability,
                    timing = matrix.fertilizerTiming,
                    icon = Icons.Default.Science
                )
                MatrixActivityRow(
                    activity = "Irrigation Cycle",
                    status = matrix.irrigationSuitability,
                    timing = matrix.irrigationTiming,
                    icon = Icons.Default.WaterDrop
                )
                MatrixActivityRow(
                    activity = "Harvest & Threshing",
                    status = matrix.harvestingSuitability,
                    timing = matrix.harvestingTiming,
                    icon = Icons.Default.Inventory
                )
            }
        }
    }
}

@Composable
private fun MatrixActivityRow(
    activity: String,
    status: AgriActivityStatus,
    timing: String,
    icon: ImageVector
) {
    val (statusColor, containerColor) = when (status) {
        AgriActivityStatus.OPTIMAL -> FarmPrimaryGreen to FarmContainerGreen
        AgriActivityStatus.MARGINAL -> FarmWarningOrange to FarmWarningContainer
        AgriActivityStatus.AVOID -> FarmAlertRed to FarmAlertRedContainer
        AgriActivityStatus.RESTRICTED -> FarmTextMuted to MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        shape = FarmSathiDesign.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = activity,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timing,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Surface(
                shape = FarmSathiDesign.shapes.pill,
                color = containerColor
            ) {
                Text(
                    text = status.labelEn,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ---------------- HOURLY FORECAST SECTION ----------------

@Composable
private fun HourlyForecastSection(
    title: String,
    forecasts: List<HourlyWeatherForecast>
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Swipe →",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(forecasts) { hour ->
                HourlyForecastCard(hour = hour)
            }
        }
    }
}

@Composable
private fun HourlyForecastCard(hour: HourlyWeatherForecast) {
    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .width(115.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), FarmSathiDesign.shapes.large)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = hour.timeLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Icon(
                imageVector = getWeatherIcon(hour.conditionType),
                contentDescription = hour.condition,
                tint = if (hour.conditionType.isRainy) SkyWaterBlue else HarvestGold,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${hour.tempC.toInt()}°C",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = SkyWaterBlue,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${hour.rainProbabilityPercent}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = SkyWaterBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = FarmSathiDesign.shapes.pill,
                color = if (hour.isFavorableForSpraying) FarmContainerGreen else FarmAlertRedContainer
            ) {
                Text(
                    text = if (hour.isFavorableForSpraying) "Safe Spray" else "Unsafe",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (hour.isFavorableForSpraying) FarmPrimaryGreen else FarmAlertRed,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 9.sp
                )
            }
        }
    }
}

// ---------------- AI AGRONOMIC ADVISORY & VOICE CARD ----------------

@Composable
private fun AiAgronomicAdvisoryCard(
    advisoryEn: String,
    advisoryHi: String,
    isGenerating: Boolean,
    isSpeaking: Boolean,
    onGenerateAi: () -> Unit,
    onSpeakToggle: () -> Unit,
    onSpeakHindi: () -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = FarmTechBlueContainer.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FarmTechBlueBorder, FarmSathiDesign.shapes.extraLarge)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(FarmTechBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "AI Agronomic Advisory",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = FarmTechBlueText
                        )
                        Text(
                            text = "Contextual Microclimate Synthesis",
                            style = MaterialTheme.typography.bodySmall,
                            color = FarmTechBlue,
                            fontSize = 11.sp
                        )
                    }
                }

                Row {
                    // Voice Listen Button (English)
                    IconButton(
                        onClick = onSpeakToggle,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Listen to advisory",
                            tint = FarmTechBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isGenerating) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = FarmTechBlue
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "AI analyzing satellite radar & soil NPK...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FarmTechBlueText
                    )
                }
            } else {
                Text(
                    text = advisoryEn.ifEmpty { "Weather intelligence indicates high rainfall. Avoid chemical broadcasting." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = FarmTechBlueText,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Hindi Accordion Section
                if (advisoryHi.isNotBlank()) {
                    Surface(
                        shape = FarmSathiDesign.shapes.medium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🇮🇳 हिन्दी कृषि परामर्श:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmPrimaryDark
                                )
                                TextButton(
                                    onClick = onSpeakHindi,
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = null,
                                        tint = FarmPrimaryGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "सुनें",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FarmPrimaryGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = advisoryHi,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onGenerateAi,
                colors = ButtonDefaults.buttonColors(containerColor = FarmTechBlue),
                shape = FarmSathiDesign.shapes.pill,
                modifier = Modifier.fillMaxWidth().testTag("weather_ai_synthesis_button"),
                enabled = !isGenerating
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Regenerate Crop-Specific AI Advisory",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ---------------- TOMORROW WEATHER HERO CARD ----------------

@Composable
private fun TomorrowWeatherHeroCard(
    forecast: DailyWeatherForecast?,
    state: WeatherIntelligenceUiState
) {
    val day = forecast ?: DailyWeatherForecast(
        dayLabel = "Tomorrow",
        dateLabel = "20 Aug",
        tempMaxC = 30,
        tempMinC = 22,
        condition = "Afternoon Rain Showers",
        conditionType = WeatherConditionType.LIGHT_RAIN,
        rainProbabilityPercent = 65,
        rainfallExpectedMm = 12.0,
        humidityPercent = 82,
        windSpeedKmh = 14.0,
        windDirection = "SW",
        uvIndex = 7,
        primaryAgriImpact = "Morning spray window (7-10:30 AM); disease risk may increase.",
        primaryAgriImpactHi = "सुबह 7-10:30 बजे स्प्रे का मौका; फफूंद का खतरा बढ़ सकता है।",
        fertilizerStatus = AgriActivityStatus.MARGINAL,
        sprayingStatus = AgriActivityStatus.MARGINAL,
        irrigationStatus = AgriActivityStatus.AVOID,
        harvestingStatus = AgriActivityStatus.MARGINAL,
        farmSuitabilityScore = 58
    )

    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clip(FarmSathiDesign.shapes.extraLarge)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1565C0), // Royal Sky Blue
                            Color(0xFF0D47A1)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOMORROW'S OUTLOOK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.75f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${day.dayLabel} • ${day.dateLabel}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = FarmSathiDesign.shapes.pill,
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "${day.rainProbabilityPercent}% Rain Chance",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${day.tempMaxC}° / ${day.tempMinC}°C",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = day.condition,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Icon(
                        imageVector = getWeatherIcon(day.conditionType),
                        contentDescription = null,
                        tint = HarvestGold,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Shift vs Today
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TomorrowShiftChip(label = "Rain Volume", value = "${day.rainfallExpectedMm} mm (-12.5mm vs today)")
                    TomorrowShiftChip(label = "Wind", value = "${day.windSpeedKmh.toInt()} km/h SW")
                }
            }
        }
    }
}

@Composable
private fun TomorrowShiftChip(label: String, value: String) {
    Surface(
        shape = FarmSathiDesign.shapes.pill,
        color = Color.Black.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: $value",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontSize = 11.sp
            )
        }
    }
}

// ---------------- TOMORROW PREPARATION CHECKLIST CARD ----------------

@Composable
private fun TomorrowPreparationChecklistCard() {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), FarmSathiDesign.shapes.extraLarge)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AssignmentTurnedIn,
                    contentDescription = null,
                    tint = FarmPrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Field Preparation Checklist For Tomorrow",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ChecklistItem(
                    text = "Fill sprayer tank tonight for 7:00 AM bio-spray window before afternoon rain.",
                    icon = Icons.Default.CheckCircle,
                    tint = FarmPrimaryGreen
                )
                ChecklistItem(
                    text = "Inspect and clear field bund drainage outlets to prevent cotton root waterlogging.",
                    icon = Icons.Default.CheckCircle,
                    tint = FarmPrimaryGreen
                )
                ChecklistItem(
                    text = "Hold automated solar pump drip timers (save ~4,200L reservoir water).",
                    icon = Icons.Default.CheckCircle,
                    tint = SkyWaterBlue
                )
            }
        }
    }
}

@Composable
private fun ChecklistItem(text: String, icon: ImageVector, tint: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 17.sp
        )
    }
}

// ---------------- 7-DAY TREND HERO CARD ----------------

@Composable
private fun SevenDayTrendHeroCard(forecasts: List<DailyWeatherForecast>) {
    val totalRain = forecasts.sumOf { it.rainfallExpectedMm }
    val maxTemp = forecasts.maxOfOrNull { it.tempMaxC } ?: 35
    val minTemp = forecasts.minOfOrNull { it.tempMinC } ?: 20

    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clip(FarmSathiDesign.shapes.extraLarge)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF2E7D32), // Rich Agriculture Green
                            Color(0xFF1B5E20)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "7-DAY SYNOPTIC OUTLOOK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.75f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Weekly Agronomic Forecast",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = FarmSathiDesign.shapes.pill,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "~${String.format("%.1f", totalRain)} mm Cumulative Rain",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Weekly Temp Range: $minTemp°C (Min) to $maxTemp°C (Max)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Monsoon showers expected Wed-Fri, followed by a clear, dry weekend (Saturday-Monday) ideal for foliar nutrition and spraying.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 17.sp
                )
            }
        }
    }
}

// ---------------- 7-DAY STRATEGIC WINDOWS CARD ----------------

@Composable
private fun SevenDayStrategicWindowsCard() {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), FarmSathiDesign.shapes.extraLarge)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = FarmPrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Strategic Field Activity Windows",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StrategicWindowItem(
                    activity = "Foliar Spraying",
                    windowText = "Saturday 22 Aug & Sunday 23 Aug (Dry Canopies, low wind)",
                    status = "OPTIMAL",
                    color = FarmPrimaryGreen
                )
                StrategicWindowItem(
                    activity = "Fertilizer Top-Dress",
                    windowText = "Saturday 22 Aug onwards (Post-monsoon drainage)",
                    status = "OPTIMAL",
                    color = FarmPrimaryGreen
                )
                StrategicWindowItem(
                    activity = "Smart Irrigation",
                    windowText = "Pause until Sunday evening (Rain meets soil ETc needs)",
                    status = "PAUSE",
                    color = SkyWaterBlue
                )
                StrategicWindowItem(
                    activity = "Produce Transport / Mandi",
                    windowText = "Sunday & Monday (Clear roads, dry conditions)",
                    status = "FAVORABLE",
                    color = FarmHarvestGold
                )
            }
        }
    }
}

@Composable
private fun StrategicWindowItem(activity: String, windowText: String, status: String, color: Color) {
    Surface(
        shape = FarmSathiDesign.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activity,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = FarmSathiDesign.shapes.pill,
                    color = color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = windowText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

// ---------------- DAILY WEATHER FORECAST CARD ----------------

@Composable
private fun DailyWeatherForecastCard(forecast: DailyWeatherForecast) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), FarmSathiDesign.shapes.large)
            .clickable { expanded = !expanded }
            .testTag("weather_day_${forecast.dayLabel.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Day & Date
                Column(modifier = Modifier.width(90.dp)) {
                    Text(
                        text = forecast.dayLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = forecast.dateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                // Weather Icon & Condition
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = getWeatherIcon(forecast.conditionType),
                        contentDescription = forecast.condition,
                        tint = if (forecast.conditionType.isRainy) SkyWaterBlue else HarvestGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = forecast.condition,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Rain Probability
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = SkyWaterBlue,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${forecast.rainProbabilityPercent}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkyWaterBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // Temp High / Low
                Text(
                    text = "${forecast.tempMaxC}° / ${forecast.tempMinC}°",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Agri Impact Summary Pill
            Surface(
                shape = FarmSathiDesign.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = FarmPrimaryGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = forecast.primaryAgriImpact,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            // Expandable details with 4 activity badges
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Humidity: ${forecast.humidityPercent}% • Wind: ${forecast.windSpeedKmh.toInt()} km/h ${forecast.windDirection} • UV: ${forecast.uvIndex}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DailyActivityPill(label = "Spray", status = forecast.sprayingStatus, modifier = Modifier.weight(1f))
                        DailyActivityPill(label = "Fertilizer", status = forecast.fertilizerStatus, modifier = Modifier.weight(1f))
                        DailyActivityPill(label = "Irrigation", status = forecast.irrigationStatus, modifier = Modifier.weight(1f))
                        DailyActivityPill(label = "Harvest", status = forecast.harvestingStatus, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyActivityPill(
    label: String,
    status: AgriActivityStatus,
    modifier: Modifier = Modifier
) {
    val (statusColor, containerColor) = when (status) {
        AgriActivityStatus.OPTIMAL -> FarmPrimaryGreen to FarmContainerGreen
        AgriActivityStatus.MARGINAL -> FarmWarningOrange to FarmWarningContainer
        AgriActivityStatus.AVOID -> FarmAlertRed to FarmAlertRedContainer
        AgriActivityStatus.RESTRICTED -> FarmTextMuted to MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        shape = FarmSathiDesign.shapes.pill,
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                fontSize = 9.sp
            )
            Text(
                text = status.labelEn.split(" ").firstOrNull() ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = statusColor.copy(alpha = 0.8f),
                fontSize = 8.sp
            )
        }
    }
}

// ---------------- ICON HELPER ----------------

private fun getWeatherIcon(conditionType: WeatherConditionType): ImageVector {
    return when (conditionType) {
        WeatherConditionType.SUNNY -> Icons.Default.WbSunny
        WeatherConditionType.HOT_DRY -> Icons.Default.WbSunny
        WeatherConditionType.PARTLY_CLOUDY -> Icons.Default.CloudQueue
        WeatherConditionType.CLOUDY -> Icons.Default.Cloud
        WeatherConditionType.LIGHT_RAIN -> Icons.Default.WaterDrop
        WeatherConditionType.HEAVY_RAIN -> Icons.Default.Thunderstorm
        WeatherConditionType.THUNDERSTORM -> Icons.Default.FlashOn
        WeatherConditionType.WINDY -> Icons.Default.Air
        WeatherConditionType.MIST_FOG -> Icons.Default.BlurOn
    }
}

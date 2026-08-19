package com.example.features.home

import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.navigation.Screen
import com.example.designsystem.components.*
import com.example.designsystem.theme.FarmSathiDesign
import com.example.designsystem.theme.FarmSemanticStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigate: (Screen) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenLanguageSelector: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    var farmMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            HomeHeaderBar(
                greeting = "${viewModel.getGreetingTimeText()}, ${state.farmerProfile.name.split(" ").firstOrNull() ?: "Farmer"} 🌾",
                selectedFarm = state.selectedFarm,
                availableFarms = state.availableFarms,
                unreadCount = state.unreadNotificationsCount,
                farmMenuExpanded = farmMenuExpanded,
                onFarmMenuToggle = { farmMenuExpanded = it },
                onFarmSelected = {
                    viewModel.selectFarm(it)
                    farmMenuExpanded = false
                },
                onNotificationsClick = onOpenNotifications,
                onLanguageClick = onOpenLanguageSelector
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refreshDashboard() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading && !state.isRefreshing -> {
                    FarmLoadingState(
                        message = "Loading live farm telemetry & satellite weather...",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                state.errorMessage != null -> {
                    FarmErrorState(
                        title = "Telemetry Sync Error",
                        description = state.errorMessage ?: "Failed to reach farm sensors.",
                        onRetry = { viewModel.loadData() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("home_scrollable_column"),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Offline Notice Banner
                        if (state.isOffline) {
                            item {
                                OfflineStateBanner(
                                    onSyncClick = { viewModel.refreshDashboard() }
                                )
                            }
                        }

                        // 1. Large Farm Health Card (82 / 100 • Healthy)
                        item {
                            LargeFarmHealthCard(
                                score = state.farmHealthScore,
                                status = state.farmHealthStatus,
                                cropHealthText = "92% (Pod Filling)",
                                soilHealthText = "Optimal NPK (pH 6.9)",
                                waterStatusText = "Adequate (Drip Ready)",
                                diseaseRiskText = "Low Risk (12% Alert)",
                                onClick = { onNavigate(Screen.DigitalTwin) }
                            )
                        }

                        // 2. Prominent FarmSathi AI Card
                        item {
                            FarmSathiAiUpdateCard(
                                alerts = state.aiUpdates,
                                onAskAiClick = { onNavigate(Screen.AgroSathiAi) },
                                onAlertClick = { route ->
                                    when (route) {
                                        "smart_irrigation" -> onNavigate(Screen.SmartIrrigation)
                                        "disease_detection" -> onNavigate(Screen.DiseaseDetection)
                                        "market" -> onNavigate(Screen.Market)
                                        else -> onNavigate(Screen.AgroSathiAi)
                                    }
                                }
                            )
                        }

                        // 3. Quick Actions Grid (6 items)
                        item {
                            Text(
                                text = "Quick Actions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        item {
                            QuickActionsSection(onNavigate = onNavigate)
                        }

                        // 4. Live Farm Status (IoT Telemetry: Moisture, Temp, Humidity, Soil Health)
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Live Farm Status",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    FarmStatusBadge(
                                        status = if (state.isOffline) FarmSemanticStatus.Offline else FarmSemanticStatus.Live,
                                        customLabel = if (state.isOffline) "Offline (Cached)" else "ESP32 Live"
                                    )
                                }
                                TextButton(onClick = { onNavigate(Screen.Telemetry) }) {
                                    Text(
                                        text = "DETAILS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        item {
                            LiveFarmStatusGrid(
                                state = state,
                                onNavigate = onNavigate
                            )
                        }

                        // 5. Today's Farm Tasks (1. Irrigate Field 2, 2. Inspect soybean leaves, 3. Monitor prices)
                        item {
                            val doneCount = state.farmTasks.count { it.isCompleted }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Today's Farm Tasks",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = FarmSathiDesign.shapes.pill,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "$doneCount/${state.farmTasks.size} Completed",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        item {
                            TodayFarmTasksCard(
                                tasks = state.farmTasks,
                                onTaskToggle = { viewModel.toggleTask(it) },
                                onTaskAction = { route ->
                                    when (route) {
                                        "smart_irrigation" -> onNavigate(Screen.SmartIrrigation)
                                        "disease_detection" -> onNavigate(Screen.DiseaseDetection)
                                        "market" -> onNavigate(Screen.Market)
                                    }
                                }
                            )
                        }

                        // 6. Market Summary (Current Price, Change, Trend)
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Market Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                TextButton(onClick = { onNavigate(Screen.Market) }) {
                                    Text(
                                        text = "VIEW MANDI",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        item {
                            MarketSummaryCard(
                                mandiPrices = state.mandiPrices,
                                onNavigateMarket = { onNavigate(Screen.Market) }
                            )
                        }

                        // Bottom Spacer for clean navigation bar clearance
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ---------------- HEADER COMPONENT ----------------

@Composable
private fun HomeHeaderBar(
    greeting: String,
    selectedFarm: String,
    availableFarms: List<String>,
    unreadCount: Int,
    farmMenuExpanded: Boolean,
    onFarmMenuToggle: (Boolean) -> Unit,
    onFarmSelected: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onLanguageClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(0.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Greeting & Farm Selector
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 19.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Farm selector chip
                    Box {
                        Surface(
                            shape = FarmSathiDesign.shapes.pill,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(FarmSathiDesign.shapes.pill)
                                .clickable { onFarmMenuToggle(true) }
                                .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.pill)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = selectedFarm,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = farmMenuExpanded,
                            onDismissRequest = { onFarmMenuToggle(false) }
                        ) {
                            availableFarms.forEach { farm ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = farm,
                                            fontWeight = if (farm == selectedFarm) FontWeight.Bold else FontWeight.Normal,
                                            color = if (farm == selectedFarm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = { onFarmSelected(farm) }
                                )
                            }
                        }
                    }
                }

                // Actions: Language & Notifications
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onLanguageClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = onNotificationsClick,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- 1. LARGE FARM HEALTH CARD ----------------

@Composable
private fun LargeFarmHealthCard(
    score: Int,
    status: FarmSemanticStatus,
    cropHealthText: String,
    soilHealthText: String,
    waterStatusText: String,
    diseaseRiskText: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FARM HEALTH",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = " / 100",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    FarmStatusBadge(status = status, customLabel = "Healthy")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap for Digital Twin ➔",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Linear Progress Visualizer
            FarmLinearProgress(
                progress = score / 100f,
                status = status,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Pillars Grid: Crop Health, Soil Health, Water Status, Disease Risk
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FarmSathiDesign.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HealthPillarItem(
                        icon = Icons.Default.Eco,
                        label = "Crop Health",
                        value = cropHealthText,
                        modifier = Modifier.weight(1f)
                    )
                    HealthPillarItem(
                        icon = Icons.Default.Spa,
                        label = "Soil Health",
                        value = soilHealthText,
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HealthPillarItem(
                        icon = Icons.Default.WaterDrop,
                        label = "Water Status",
                        value = waterStatusText,
                        modifier = Modifier.weight(1f)
                    )
                    HealthPillarItem(
                        icon = Icons.Default.Shield,
                        label = "Disease Risk",
                        value = diseaseRiskText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthPillarItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(FarmSathiDesign.shapes.small)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------- 2. PROMINENT FARMSATHI AI CARD ----------------

@Composable
private fun FarmSathiAiUpdateCard(
    alerts: List<FarmAiAlertItem>,
    onAskAiClick: () -> Unit,
    onAlertClick: (String) -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = FarmTechBlueContainer),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FarmTechBlueBorder, FarmSathiDesign.shapes.extraLarge)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row with Robot Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(FarmTechBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "FarmSathi AI",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "🤖 FarmSathi AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FarmTechBlueText
                        )
                        Text(
                            text = "“I found 3 important updates about your farm.”",
                            style = MaterialTheme.typography.bodySmall,
                            color = FarmTechBlueText.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Alert Pills
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                alerts.forEach { alert ->
                    Surface(
                        shape = FarmSathiDesign.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FarmTechBlueBorder, FarmSathiDesign.shapes.medium)
                            .clickable { onAlertClick(alert.targetRoute) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = alert.icon, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = alert.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 16.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = FarmTechBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ask FarmSathi Button (Voice & Chat)
            Button(
                onClick = onAskAiClick,
                shape = FarmSathiDesign.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = FarmTechBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("home_ask_farmsathi_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ask FarmSathi / AI से पूछें",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ---------------- 3. QUICK ACTIONS GRID ----------------

@Composable
private fun QuickActionsSection(onNavigate: (Screen) -> Unit) {
    val quickActions = listOf(
        QuickActionItem("Scan Crop", Icons.Default.CameraAlt, "AI Leaf Doctor", Screen.DiseaseDetection, FarmPrimaryGreen),
        QuickActionItem("Irrigation", Icons.Default.WaterDrop, "Smart Valves", Screen.SmartIrrigation, FarmTechBlue),
        QuickActionItem("Soil Health", Icons.Default.Spa, "NPK & Lab", Screen.SoilHealth, FarmEarthBrown),
        QuickActionItem("Market", Icons.Default.TrendingUp, "Mandi Rates", Screen.Market, FarmHarvestGold),
        QuickActionItem("Sell Harvest", Icons.Default.Handshake, "Direct Buyers", Screen.Buyers, FarmPrimaryLight),
        QuickActionItem("Ask AI", Icons.Default.AutoAwesome, "Voice Assistant", Screen.AgroSathiAi, FarmTechBlue)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        quickActions.chunked(3).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowActions.forEach { action ->
                    QuickActionCard(
                        action = action,
                        onClick = { onNavigate(action.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

data class QuickActionItem(
    val title: String,
    val icon: ImageVector,
    val subtitle: String,
    val route: Screen,
    val color: Color
)

@Composable
private fun QuickActionCard(
    action: QuickActionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = FarmSathiDesign.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.large)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(FarmSathiDesign.shapes.medium)
                    .background(action.color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.title,
                    tint = action.color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = action.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

// ---------------- 4. LIVE FARM STATUS GRID ----------------

@Composable
private fun LiveFarmStatusGrid(
    state: HomeUiState,
    onNavigate: (Screen) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Soil Moisture
            TelemetryMetricCard(
                title = "Soil Moisture",
                value = "${state.telemetry.soilMoistureTop10cm}",
                unit = "%",
                status = FarmSemanticStatus.Healthy,
                statusLabel = "Optimal (40-60%)",
                lastUpdated = state.telemetry.lastSyncTime,
                icon = Icons.Default.WaterDrop,
                onClick = { onNavigate(Screen.Telemetry) },
                modifier = Modifier.weight(1f)
            )

            // 2. Temperature
            TelemetryMetricCard(
                title = "Temperature",
                value = "${state.telemetry.ambientTemperature}",
                unit = "°C",
                status = FarmSemanticStatus.Healthy,
                statusLabel = "Normal Range",
                lastUpdated = state.telemetry.lastSyncTime,
                icon = Icons.Default.Thermostat,
                onClick = { onNavigate(Screen.Telemetry) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 3. Humidity
            TelemetryMetricCard(
                title = "Humidity",
                value = "${state.telemetry.ambientHumidity}",
                unit = "%",
                status = FarmSemanticStatus.Healthy,
                statusLabel = "Favorable",
                lastUpdated = state.telemetry.lastSyncTime,
                icon = Icons.Default.Air,
                onClick = { onNavigate(Screen.Telemetry) },
                modifier = Modifier.weight(1f)
            )

            // 4. Soil Health
            TelemetryMetricCard(
                title = "Soil Health",
                value = "pH ${state.soilHealth.phLevel}",
                unit = "NPK Rich",
                status = FarmSemanticStatus.Healthy,
                statusLabel = "Optimal",
                lastUpdated = "Sample: ${state.soilHealth.sampleDate}",
                icon = Icons.Default.Spa,
                onClick = { onNavigate(Screen.SoilHealth) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TelemetryMetricCard(
    title: String,
    value: String,
    unit: String,
    status: FarmSemanticStatus,
    statusLabel: String,
    lastUpdated: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.large)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FarmStatusBadge(status = status, customLabel = statusLabel)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "🕒 $lastUpdated",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp
            )
        }
    }
}

// ---------------- 5. TODAY'S FARM TASKS ----------------

@Composable
private fun TodayFarmTasksCard(
    tasks: List<FarmTaskItem>,
    onTaskToggle: (String) -> Unit,
    onTaskAction: (String) -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            tasks.forEachIndexed { index, task ->
                Surface(
                    shape = FarmSathiDesign.shapes.medium,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onTaskToggle(task.id) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { onTaskToggle(task.id) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Text(
                                    text = task.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )
                            }
                        }

                        if (task.actionRoute != null && !task.isCompleted) {
                            FilledTonalButton(
                                onClick = { onTaskAction(task.actionRoute) },
                                shape = FarmSathiDesign.shapes.pill,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Do Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- 6. MARKET SUMMARY ----------------

@Composable
private fun MarketSummaryCard(
    mandiPrices: List<com.example.data.models.MandiPriceItem>,
    onNavigateMarket: () -> Unit
) {
    val topItem = mandiPrices.firstOrNull() ?: return

    Card(
        onClick = onNavigateMarket,
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
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
                            .size(38.dp)
                            .clip(FarmSathiDesign.shapes.small)
                            .background(FarmHarvestGoldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌾", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = topItem.cropName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = topItem.mandiName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                FarmStatusBadge(
                    status = FarmSemanticStatus.Healthy,
                    customLabel = "HOLD (+₹300/Q)"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Price numbers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FarmSathiDesign.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CURRENT MANDI PRICE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "₹${topItem.currentPricePerQuintal} / Q",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "24H CHANGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+₹230 (+${topItem.priceChangePercent}%) ↑",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FarmSuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "📈 AI Trend: ${topItem.sellRecommendation}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        }
    }
}

// ---------------- OFFLINE BANNER ----------------

@Composable
private fun OfflineStateBanner(onSyncClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = FarmSathiDesign.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.large)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Offline Mode (Cached Data)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Displaying last known sensor telemetry from 20s ago.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(onClick = onSyncClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sync",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

package com.example.features.market

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.navigation.Screen
import com.example.data.models.*
import com.example.data.repository.FarmRepository
import com.example.designsystem.components.*
import com.example.designsystem.theme.FarmSathiDesign
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun MarketScreen(
    repository: FarmRepository,
    onNavigate: (Screen) -> Unit,
    onOpenNotifications: () -> Unit = {},
    onOpenLanguageSelector: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: MarketViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MarketViewModel(
                    application = context.applicationContext as Application,
                    repository = repository
                ) as T
            }
        }
    )
    MarketScreen(
        viewModel = viewModel,
        onNavigate = onNavigate,
        onOpenNotifications = onOpenNotifications,
        onOpenLanguageSelector = onOpenLanguageSelector
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    viewModel: MarketViewModel,
    onNavigate: (Screen) -> Unit,
    onOpenNotifications: () -> Unit = {},
    onOpenLanguageSelector: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            MarketTopAppBar(
                state = state,
                onRefresh = { viewModel.refreshMarketData(isPullToRefresh = true) },
                onToggleOffline = { viewModel.toggleOffline(!state.isOffline) },
                onOpenNotifications = onOpenNotifications
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refreshMarketData(isPullToRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading && !state.isRefreshing -> {
                    FarmLoadingState(
                        message = "Syncing live APMC mandi rates...",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                state.errorMessage != null && state.historicalPoints.isEmpty() -> {
                    FarmErrorState(
                        title = "Mandi Gateway Offline",
                        description = state.errorMessage ?: "Unable to connect to Agmarknet server",
                        onRetry = { viewModel.refreshMarketData() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("market_screen_scroll"),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 40.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Offline Warning Banner
                        if (state.isOffline) {
                            item {
                                OfflineNoticeCard(
                                    lastSyncTime = state.lastSyncTimestamp,
                                    onGoOnline = { viewModel.toggleOffline(false) }
                                )
                            }
                        }

                        // 1. Quick Navigation Action Bar (Sell or Wait, Direct Buyers & Shared Truck)
                        item {
                            MarketQuickActionsRow(
                                onSellDecisionClick = { onNavigate(Screen.SellDecision) },
                                onDirectBuyersClick = { onNavigate(Screen.Buyers) },
                                onLogisticsClick = { onNavigate(Screen.Logistics) }
                            )
                        }

                        // Sell Now or Wait AI Decision Teaser Banner
                        item {
                            SellDecisionTeaserBanner(
                                cropName = state.selectedCrop.name,
                                onOpenDecision = { onNavigate(Screen.SellDecision) }
                            )
                        }

                        // 2. Crop Selector Chips (Horizontal Scroll)
                        item {
                            CropSelectorSection(
                                availableCrops = state.availableCrops,
                                selectedCrop = state.selectedCrop,
                                onSelectCrop = { viewModel.selectCrop(it) }
                            )
                        }

                        // 3. Current Price & Primary Market Hero Card
                        item {
                            CurrentPriceHeroCard(
                                quote = state.currentMandiPrice,
                                crop = state.selectedCrop,
                                lastUpdated = state.lastSyncTimestamp
                            )
                        }

                        // 4. Interactive Historical Price Trend Graph (Today, 7D, 30D, 6M)
                        item {
                            HistoricalPriceGraphCard(
                                points = state.historicalPoints,
                                stats = state.periodStats,
                                selectedPeriod = state.selectedTimePeriod,
                                onSelectPeriod = { viewModel.selectTimePeriod(it) },
                                cropUnit = state.selectedCrop.unit
                            )
                        }

                        // 5. Nearby Mandi Comparison Section (Indore, Dewas, Ujjain, etc.)
                        item {
                            NearbyMandiComparisonSection(
                                quotes = state.nearbyMandiQuotes,
                                searchQuery = state.searchQuery,
                                onSearchChange = { viewModel.setSearchQuery(it) }
                            )
                        }

                        // 6. AI Market Forecast & Uncertainty Intelligence
                        item {
                            AiForecastSection(
                                forecast = state.aiForecast,
                                onSpeakEnglish = { viewModel.speakForecast(isHindi = false) },
                                onSpeakHindi = { viewModel.speakForecast(isHindi = true) }
                            )
                        }

                        // 7. Mandatory Disclaimer Card (Probabilistic, Non-Guaranteed)
                        item {
                            MarketDisclaimerCard(disclaimerText = state.aiForecast.disclaimerText)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Top App Bar for Market Intelligence.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketTopAppBar(
    state: MarketIntelligenceState,
    onRefresh: () -> Unit,
    onToggleOffline: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Market Intelligence",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (state.isOffline) FarmHarvestGold.copy(alpha = 0.2f) else FarmPrimaryGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (state.isOffline) "OFFLINE" else "LIVE APMC",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (state.isOffline) FarmHarvestGold else FarmPrimaryGreen,
                            fontSize = 9.sp
                        )
                    }
                }
                Text(
                    text = "Live Rates • Mandi Comparison • AI Forecast",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        },
        actions = {
            // Offline Toggle
            IconButton(
                onClick = onToggleOffline,
                modifier = Modifier.testTag("btn_market_offline_toggle")
            ) {
                Icon(
                    imageVector = if (state.isOffline) Icons.Default.CloudOff else Icons.Default.CloudQueue,
                    contentDescription = "Toggle Offline Mode",
                    tint = if (state.isOffline) FarmHarvestGold else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Sync / Refresh
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.testTag("btn_market_refresh")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Market Rates"
                )
            }

            // Notifications
            IconButton(onClick = onOpenNotifications) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

/**
 * Offline status banner.
 */
@Composable
private fun OfflineNoticeCard(
    lastSyncTime: String,
    onGoOnline: () -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = FarmHarvestGold.copy(alpha = 0.12f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FarmHarvestGold.copy(alpha = 0.35f), FarmSathiDesign.shapes.medium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = FarmHarvestGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Viewing Offline Cached Mandi Rates",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Last synced: $lastSyncTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            TextButton(
                onClick = onGoOnline,
                colors = ButtonDefaults.textButtonColors(contentColor = FarmPrimaryGreen)
            ) {
                Text("Sync Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

/**
 * Quick Action Bar for Direct Farm-gate Buyers, Shared Logistics and Sell/Wait Decision.
 */
@Composable
private fun MarketQuickActionsRow(
    onSellDecisionClick: () -> Unit,
    onDirectBuyersClick: () -> Unit,
    onLogisticsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSellDecisionClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = FarmPrimaryDark,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
            modifier = Modifier
                .weight(1.2f)
                .testTag("btn_sell_now_or_wait")
        ) {
            Icon(Icons.Default.Balance, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Sell or Wait?", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onDirectBuyersClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = CleanMinPrimary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("btn_direct_buyers")
        ) {
            Icon(Icons.Default.Handshake, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Buyers", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onLogisticsClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = CleanMinContainer,
                contentColor = CleanMinOnContainerDark
            ),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("btn_shared_truck")
        ) {
            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Logistics", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Sell Now vs Wait AI Decision Teaser Banner.
 */
@Composable
private fun SellDecisionTeaserBanner(
    cropName: String,
    onOpenDecision: () -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = FarmTechBlueContainer),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FarmTechBlueBorder, FarmSathiDesign.shapes.medium)
            .clickable { onOpenDecision() }
            .testTag("sell_decision_teaser_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FarmTechBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Sell Now or Wait AI Advisor",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmTechBlueText
                    )
                    Text(
                        text = "Net revenue comparison, storage costs & holding payoff for $cropName",
                        style = MaterialTheme.typography.bodySmall,
                        color = FarmTechBlueText.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open Decision Engine",
                tint = FarmTechBlue
            )
        }
    }
}

/**
 * Crop Selector Section with horizontally scrollable filter chips.
 */
@Composable
private fun CropSelectorSection(
    availableCrops: List<CropMarketItem>,
    selectedCrop: CropMarketItem,
    onSelectCrop: (CropMarketItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SELECTED CROP",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "${availableCrops.size} Commodities Available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(availableCrops) { crop ->
                val isSelected = crop.id == selectedCrop.id
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectCrop(crop) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = crop.name.substringBefore(" ("),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                            if (crop.hindiName.isNotEmpty()) {
                                Text(
                                    text = " • ${crop.hindiName}",
                                    fontSize = 11.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    } else null,
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FarmPrimaryGreen.copy(alpha = 0.15f),
                        selectedLabelColor = FarmPrimaryDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outline,
                        selectedBorderColor = FarmPrimaryGreen
                    ),
                    modifier = Modifier.testTag("crop_chip_${crop.id}")
                )
            }
        }
    }
}

/**
 * Hero Card displaying Current Price, Price Change, Primary Market, Distance, and Last Updated.
 */
@Composable
private fun CurrentPriceHeroCard(
    quote: MandiPriceDetail,
    crop: CropMarketItem,
    lastUpdated: String
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale("en", "IN")) }
    val isPositive = quote.priceChangeAmount >= 0
    val trendBg = if (isPositive) FarmSuccessGreenContainer else FarmAlertRedContainer
    val trendColor = if (isPositive) FarmSuccessGreen else FarmCriticalRed

    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.large)
            .testTag("current_price_hero_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Crop and Variety
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = crop.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = crop.variety,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                // Trend Pill Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(trendBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (isPositive) "+" else ""}${quote.priceChangePercent}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = trendColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Big Price Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "CURRENT MODAL PRICE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${formatter.format(quote.currentPricePerQuintal)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = FarmPrimaryDark,
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "/ Quintal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }
                }

                // Change Amount
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "VS YESTERDAY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )
                    Text(
                        text = "${if (isPositive) "+₹" else "-₹"}${formatter.format(kotlin.math.abs(quote.priceChangeAmount))}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = trendColor
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.8.dp)

            // Market Name, Distance, and Last Updated Row
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Market Name & Distance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = FarmPrimaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = quote.mandiName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Distance Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = null,
                            tint = FarmTechBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${quote.distanceKm} km away",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = FarmTechBlue,
                            fontSize = 11.sp
                        )
                    }
                }

                // Day Range & Last Updated
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Day Range: ₹${formatter.format(quote.minPrice)} – ₹${formatter.format(quote.maxPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = lastUpdated,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Historical Price Trend Card with multi-horizon selectors (Today, 7 Days, 30 Days, 6 Months),
 * interactive Canvas chart, and statistical summary metrics.
 */
@Composable
private fun HistoricalPriceGraphCard(
    points: List<MarketHistoricalPoint>,
    stats: PeriodPriceStats,
    selectedPeriod: MarketTimePeriod,
    onSelectPeriod: (MarketTimePeriod) -> Unit,
    cropUnit: String
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale("en", "IN")) }
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.large)
            .testTag("historical_price_graph_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section Header & Time Period Segmented Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PRICE TREND & HISTORY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${selectedPeriod.label} Historical Curve",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Time Period Buttons (Today | 7 Days | 30 Days | 6 Months)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MarketTimePeriod.values().forEach { period ->
                    val isSelected = period == selectedPeriod
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) FarmPrimaryGreen else Color.Transparent)
                            .clickable {
                                selectedPointIndex = null
                                onSelectPeriod(period)
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = period.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Interactive Tooltip Info Box if user scrubs/taps
            val activePoint = selectedPointIndex?.let { points.getOrNull(it) }
            if (activePoint != null) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmPrimaryDark.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Time: ${activePoint.label}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Rate: ₹${formatter.format(activePoint.priceInr.toInt())} / Q",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = FarmPrimaryDark
                        )
                    }
                }
            }

            // High-fidelity Canvas Line Chart
            if (points.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    MarketPriceLineChart(
                        points = points,
                        selectedIndex = selectedPointIndex,
                        onPointSelected = { selectedPointIndex = it }
                    )
                }
            }

            // Period Summary Stats Grid (High, Low, Average, Movement)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PeriodStatItem(label = "Period High", value = "₹${formatter.format(stats.highPrice)}", color = FarmPrimaryDark)
                PeriodStatItem(label = "Period Low", value = "₹${formatter.format(stats.lowPrice)}", color = FarmCriticalRed)
                PeriodStatItem(label = "Average", value = "₹${formatter.format(stats.avgPrice)}", color = MaterialTheme.colorScheme.onSurface)
                PeriodStatItem(
                    label = "Net Shift",
                    value = "${if (stats.changeAmount >= 0) "+" else ""}₹${formatter.format(stats.changeAmount)}",
                    color = if (stats.changeAmount >= 0) FarmSuccessGreen else FarmCriticalRed
                )
            }
        }
    }
}

@Composable
private fun PeriodStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 12.sp
        )
    }
}

/**
 * Custom Compose Canvas Line Chart for smooth price trends with gradient fills and touch interactivity.
 */
@Composable
private fun MarketPriceLineChart(
    points: List<MarketHistoricalPoint>,
    selectedIndex: Int?,
    onPointSelected: (Int) -> Unit
) {
    val lineColor = FarmPrimaryGreen

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(points) {
                detectTapGestures { offset ->
                    val pointWidth = size.width / (points.size - 1).coerceAtLeast(1)
                    val clickedIndex = (offset.x / pointWidth).roundToInt().coerceIn(0, points.size - 1)
                    onPointSelected(clickedIndex)
                }
            }
    ) {
        if (points.size < 2) return@Canvas

        val w = size.width
        val h = size.height - 30.dp.toPx()
        val bottomY = h
        val minPrice = (points.minOf { it.priceInr } * 0.985).toFloat()
        val maxPrice = (points.maxOf { it.priceInr } * 1.015).toFloat()
        val priceRange = (maxPrice - minPrice).coerceAtLeast(1f)

        fun getX(index: Int): Float = (index.toFloat() / (points.size - 1)) * w
        fun getY(price: Double): Float = bottomY - (((price.toFloat() - minPrice) / priceRange) * bottomY)

        // Draw horizontal grid lines
        val gridLines = 3
        for (i in 0..gridLines) {
            val y = (bottomY / gridLines) * i
            drawLine(
                color = Color.LightGray.copy(alpha = 0.35f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // Build line path and fill path
        val path = Path()
        val fillPath = Path()

        path.moveTo(getX(0), getY(points[0].priceInr))
        fillPath.moveTo(getX(0), bottomY)
        fillPath.lineTo(getX(0), getY(points[0].priceInr))

        for (i in 1 until points.size) {
            val prevX = getX(i - 1)
            val prevY = getY(points[i - 1].priceInr)
            val curX = getX(i)
            val curY = getY(points[i].priceInr)

            val controlX1 = prevX + (curX - prevX) / 2
            val controlY1 = prevY
            val controlX2 = prevX + (curX - prevX) / 2
            val controlY2 = curY

            path.cubicTo(controlX1, controlY1, controlX2, controlY2, curX, curY)
            fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, curX, curY)
        }

        fillPath.lineTo(getX(points.size - 1), bottomY)
        fillPath.close()

        // Draw Gradient Fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.32f),
                    lineColor.copy(alpha = 0.02f)
                ),
                startY = 0f,
                endY = bottomY
            )
        )

        // Draw Stroke Line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw interactive nodes and selection indicator
        points.forEachIndexed { index, point ->
            val x = getX(index)
            val y = getY(point.priceInr)
            val isSelected = selectedIndex == index

            if (isSelected) {
                // Vertical selection dashline
                drawLine(
                    color = FarmPrimaryDark,
                    start = Offset(x, 0f),
                    end = Offset(x, bottomY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                // Large halo circle
                drawCircle(
                    color = FarmPrimaryGreen.copy(alpha = 0.25f),
                    radius = 9.dp.toPx(),
                    center = Offset(x, y)
                )
            }

            // Center Point Circle
            drawCircle(
                color = Color.White,
                radius = if (isSelected) 5.5.dp.toPx() else 3.5.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = if (isSelected) FarmPrimaryDark else lineColor,
                radius = if (isSelected) 4.dp.toPx() else 2.5.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

/**
 * Nearby Mandi Comparison Section (Indore, Dewas, Ujjain, Sanwer, Dhar - dynamically served).
 */
@Composable
private fun NearbyMandiComparisonSection(
    quotes: List<NearbyMandiQuote>,
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale("en", "IN")) }
    val filteredQuotes = remember(quotes, searchQuery) {
        if (searchQuery.isBlank()) quotes
        else quotes.filter {
            it.mandiName.contains(searchQuery, ignoreCase = true) ||
            it.district.contains(searchQuery, ignoreCase = true)
        }
    }

    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.large)
            .testTag("nearby_mandi_comparison_section")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NEARBY MANDI COMPARISON",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "APMC Price & Net Realization",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(FarmPrimaryGreen.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${quotes.size} Mandis Active",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmPrimaryDark,
                        fontSize = 11.sp
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_mandi_search"),
                placeholder = { Text("Search Mandi (e.g. Indore, Dewas, Ujjain)...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FarmPrimaryGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Mandi Comparison Cards List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredQuotes.forEach { quote ->
                    NearbyMandiItemCard(quote = quote, formatter = formatter)
                }
            }
        }
    }
}

/**
 * Individual nearby mandi quote card showing Rate, Distance, Transport Cost, and Net In-Hand Realization.
 */
@Composable
private fun NearbyMandiItemCard(
    quote: NearbyMandiQuote,
    formatter: NumberFormat
) {
    Card(
        shape = FarmSathiDesign.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (quote.isBestNetReturn) FarmPrimaryGreen.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (quote.isBestNetReturn) FarmPrimaryGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                FarmSathiDesign.shapes.medium
            )
            .testTag("mandi_card_${quote.mandiId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mandi Name, District & Best Return Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = quote.mandiName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${quote.district} District • ${quote.distanceKm} km away",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                if (quote.isBestNetReturn) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(FarmPrimaryDark)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "BEST NET RETURN",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.6.dp)

            // Price Breakdown Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gross Mandi Price
                Column {
                    Text(
                        text = "MANDI RATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "₹${formatter.format(quote.pricePerQuintal)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Distance Freight Cost
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "EST. FREIGHT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "-₹${quote.transportCostPerQtl}/Q",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Net In-Hand Realization
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "NET REALIZATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (quote.isBestNetReturn) FarmPrimaryDark else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "₹${formatter.format(quote.netRealizationPerQtl)}/Q",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (quote.isBestNetReturn) FarmPrimaryDark else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Daily Arrivals and Update Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Arrivals: ${formatter.format(quote.arrivalsQuintals.toInt())} Quintals",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )

                Text(
                    text = quote.lastUpdated,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * AI Market Forecast & Uncertainty Intelligence Section.
 */
@Composable
private fun AiForecastSection(
    forecast: AiMarketForecastIntelligence,
    onSpeakEnglish: () -> Unit,
    onSpeakHindi: () -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale("en", "IN")) }

    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.large)
            .testTag("ai_market_forecast_section")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with AI Badge & Voice Assistant
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(FarmPrimaryGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "AGROSATHI AI FORECAST",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = FarmPrimaryDark,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${forecast.cropName} (${forecast.targetHorizonLabel})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Bilingual Audio Readout Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledTonalIconButton(
                        onClick = onSpeakEnglish,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Text("EN", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                    FilledTonalIconButton(
                        onClick = onSpeakHindi,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Text("HI", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }

            // Projected Target Price & Confidence Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FarmPrimaryGreen.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PROJECTED MEDIAN TARGET",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "₹${formatter.format(forecast.projectedMedianPrice)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = FarmPrimaryDark
                                )
                                Text(
                                    text = " / Q",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }
                        }

                        // Confidence Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(FarmPrimaryDark)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "${forecast.confidencePercent}% Confidence",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Uncertainty Range Bar Visualizer
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Min: ₹${formatter.format(forecast.expectedPriceRangeMin)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "Forecast Uncertainty Range (±₹${forecast.uncertaintySpreadInr}/Q)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = FarmTechBlue,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "Max: ₹${formatter.format(forecast.expectedPriceRangeMax)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }

                        // Gradient range progress indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            FarmTechBlue.copy(alpha = 0.3f),
                                            FarmPrimaryGreen,
                                            FarmHarvestGold.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                        )
                    }
                }
            }

            // Action Recommendation Callout
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = FarmTechBlueContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FarmTechBlueBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = FarmTechBlueText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STRATEGIC RECOMMENDATION: ${forecast.recommendationTitle}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = FarmTechBlueText,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = forecast.recommendationSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = FarmTechBlueText,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "💡 ${forecast.recommendationSummaryHi}",
                        style = MaterialTheme.typography.bodySmall,
                        color = FarmTechBlueText.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }
            }

            // Market Drivers
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "PRIMARY PRICE DRIVERS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
                forecast.primaryMarketDrivers.forEach { driver ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "•",
                            fontWeight = FontWeight.Bold,
                            color = FarmPrimaryGreen,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = driver,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Risk & Volatility Factors
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "UNCERTAINTY & RISK FACTORS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
                forecast.riskFactors.forEach { risk ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 11.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = risk,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Mandatory Non-Guaranteed Disclaimer Card.
 */
@Composable
private fun MarketDisclaimerCard(disclaimerText: String) {
    Card(
        shape = FarmSathiDesign.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), FarmSathiDesign.shapes.medium)
            .testTag("market_disclaimer_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Statistical Projection Notice",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = disclaimerText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

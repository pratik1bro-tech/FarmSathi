package com.example.features.market

import android.app.Application
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.*
import com.example.data.repository.FarmRepository
import com.example.designsystem.components.*
import com.example.designsystem.theme.FarmSathiDesign
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SellDecisionScreen(
    repository: FarmRepository,
    onBack: () -> Unit,
    onNavigateToAiChat: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: SellDecisionViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return SellDecisionViewModel(
                    application = context.applicationContext as Application,
                    repository = repository
                ) as T
            }
        }
    )
    SellDecisionScreen(
        viewModel = viewModel,
        onBack = onBack,
        onNavigateToAiChat = onNavigateToAiChat
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellDecisionScreen(
    viewModel: SellDecisionViewModel,
    onBack: () -> Unit,
    onNavigateToAiChat: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val scenario = state.scenario

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Sell Now or Wait?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(FarmPrimaryGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AI DECISION",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmPrimaryDark,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Text(
                            text = "Net Revenue & Holding Cost Optimizer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_sell_decision_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Bilingual Voice Advisory
                    FilledTonalIconButton(
                        onClick = { viewModel.speakRecommendation(isHindi = false) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Text("EN", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    FilledTonalIconButton(
                        onClick = { viewModel.speakRecommendation(isHindi = true) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Text("HI", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("sell_decision_scroll"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Trade Success Notice if an action was executed
            if (state.tradeSuccessMessage != null) {
                item {
                    TradeSuccessBanner(
                        message = state.tradeSuccessMessage ?: "",
                        onDismiss = { viewModel.clearTradeSuccessMessage() }
                    )
                }
            }

            // 1. Crop Selector & Quantity Adjuster
            item {
                CropAndQuantitySelectorCard(
                    selectedCropId = state.selectedCropId,
                    quantityQuintals = state.enteredQuantityQuintals,
                    holdDays = state.customHoldDurationDays,
                    onSelectCrop = { viewModel.selectCrop(it) },
                    onUpdateQuantity = { viewModel.updateQuantity(it) },
                    onUpdateHoldDays = { viewModel.updateHoldDuration(it) }
                )
            }

            // 2. AI Recommendation Banner (SELL NOW / WAIT / COMPARE)
            item {
                AiRecommendationHeroBanner(
                    scenario = scenario,
                    onSpeak = { viewModel.speakRecommendation(isHindi = false) }
                )
            }

            // 3. Side-by-Side Financial Comparison (SELL NOW vs. WAIT)
            item {
                Text(
                    text = "FINANCIAL COMPARISON (NET IN-HAND)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }

            item {
                SellNowOptionCard(
                    sellNow = scenario.sellNow,
                    cropName = scenario.cropName,
                    quantity = scenario.quantityQuintals,
                    onInitiateSellNow = { viewModel.initiateTradeIntent("BUYER") },
                    onInitiateMandi = { viewModel.initiateTradeIntent("MANDI") }
                )
            }

            item {
                WaitAndHoldOptionCard(
                    wait = scenario.wait,
                    cropName = scenario.cropName,
                    quantity = scenario.quantityQuintals,
                    onInitiateHold = { viewModel.initiateTradeIntent("STORAGE") }
                )
            }

            // 4. "Why this recommendation?" Section
            item {
                WhyThisRecommendationSection(scenario = scenario)
            }

            // 5. "Ask FarmSathi" Interactive Advisory
            item {
                AskFarmSathiSection(
                    quickQuestions = state.quickQuestions,
                    aiResponse = state.aiChatResponse,
                    isAsking = state.isAskingAi,
                    onAskQuestion = { viewModel.askQuestion(it) },
                    onOpenFullChat = onNavigateToAiChat
                )
            }

            // 6. Mandatory Non-Guaranteed Disclaimer Card
            item {
                DecisionDisclaimerCard()
            }
        }

        // Explicit Trade Authorization Modal (NO AUTO SELLS)
        if (state.showTradeConfirmationDialog && state.pendingTradeIntent != null) {
            ExplicitTradeConfirmationDialog(
                intent = state.pendingTradeIntent!!,
                onConfirm = { viewModel.confirmAndExecuteTrade() },
                onDismiss = { viewModel.dismissTradeDialog() }
            )
        }
    }
}

/**
 * Crop, Quantity and Duration configuration selector.
 */
@Composable
private fun CropAndQuantitySelectorCard(
    selectedCropId: String,
    quantityQuintals: Double,
    holdDays: Int,
    onSelectCrop: (String) -> Unit,
    onUpdateQuantity: (Double) -> Unit,
    onUpdateHoldDays: (Int) -> Unit
) {
    val crops = listOf(
        "soybean" to "Soybean (JS-2034)",
        "cotton" to "Cotton (Kapas)",
        "tomato" to "Tomato (Fresh)",
        "wheat" to "Wheat (Sharbati)"
    )

    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.large)
            .testTag("crop_quantity_config_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "COMMODITY & BATCH PARAMETERS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )

            // Crop Horizontal Selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(crops) { (id, name) ->
                    val isSelected = id == selectedCropId
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCrop(id) },
                        label = { Text(name, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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
                        modifier = Modifier.testTag("filter_crop_$id")
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.8.dp)

            // Expected Harvest Quantity presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Expected Batch Quantity",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${quantityQuintals.toInt()} Quintals (${(quantityQuintals * 100).toInt()} kg)",
                        style = MaterialTheme.typography.bodySmall,
                        color = FarmPrimaryDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(20.0, 50.0, 100.0).forEach { qty ->
                        val isSel = quantityQuintals == qty
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) FarmPrimaryGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { onUpdateQuantity(qty) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${qty.toInt()} Q",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Holding Duration Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Holding Time Horizon",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$holdDays Days in Storage",
                        style = MaterialTheme.typography.bodySmall,
                        color = FarmTechBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(7, 14, 30).forEach { days ->
                        val isSel = holdDays == days
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) FarmTechBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { onUpdateHoldDays(days) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$days D",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * AI Recommendation Banner with dynamic highlights for SELL NOW, WAIT, or COMPARE.
 */
@Composable
private fun AiRecommendationHeroBanner(
    scenario: SellDecisionScenario,
    onSpeak: () -> Unit
) {
    val bgColor: Color
    val borderColor: Color
    val textColor: Color
    val badgeText: String

    when (scenario.recommendation) {
        DecisionActionType.SELL_NOW -> {
            bgColor = FarmSuccessGreenContainer
            borderColor = FarmPrimaryGreen
            textColor = FarmPrimaryDark
            badgeText = "RECOMMENDATION: SELL NOW"
        }
        DecisionActionType.WAIT -> {
            bgColor = FarmTechBlueContainer
            borderColor = FarmTechBlueBorder
            textColor = FarmTechBlueText
            badgeText = "RECOMMENDATION: WAIT / HOLD"
        }
        DecisionActionType.COMPARE -> {
            bgColor = FarmHarvestGoldContainer
            borderColor = FarmHarvestGold.copy(alpha = 0.4f)
            textColor = FarmHarvestGold
            badgeText = "RECOMMENDATION: COMPARE TRANCHES"
        }
    }

    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, FarmSathiDesign.shapes.large)
            .testTag("ai_recommendation_hero_banner")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Recommendation Tag & Confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(textColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = textColor,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${scenario.confidencePercent}% Confidence",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 11.sp
                    )
                }
            }

            // Headline
            Text(
                text = scenario.whyRecommendationTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 17.sp,
                lineHeight = 22.sp
            )

            // Tranche Strategy
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = FarmPrimaryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = scenario.recommendedTrancheStrategy,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * SELL NOW Option Financial Card.
 */
@Composable
private fun SellNowOptionCard(
    sellNow: SellNowBreakdown,
    cropName: String,
    quantity: Double,
    onInitiateSellNow: () -> Unit,
    onInitiateMandi: () -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale("en", "IN")) }

    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.large)
            .testTag("sell_now_option_card")
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(FarmPrimaryGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = FarmPrimaryGreen, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OPTION A: SELL TODAY",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Spot Settlement",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            // Net Revenue Highlight
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ESTIMATED NET REVENUE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                        Text(
                            text = "₹${formatter.format(sellNow.netEstimatedRevenue.toInt())}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = FarmPrimaryDark
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Net In-Hand Rate",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "₹${formatter.format(sellNow.netRealizedPricePerQtl)}/Q",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Detailed Cost Breakdown
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CostLineItem(
                    label = "Current Mandi Spot Price (${sellNow.mandiName})",
                    value = "₹${formatter.format(sellNow.currentMandiPricePerQtl)} / Q",
                    isDeduction = false
                )
                if (sellNow.bestBuyerOfferPerQtl != null) {
                    CostLineItem(
                        label = "Verified Buyer Farm-Gate Offer (${sellNow.buyerName})",
                        value = "₹${formatter.format(sellNow.bestBuyerOfferPerQtl)} / Q",
                        isDeduction = false,
                        highlightColor = FarmPrimaryGreen
                    )
                }
                CostLineItem(
                    label = "Transportation Logistics (${quantity.toInt()} Q)",
                    value = "-₹${formatter.format((sellNow.transportationCostPerQtl * quantity).toInt())} (₹${sellNow.transportationCostPerQtl}/Q)",
                    isDeduction = true
                )
                CostLineItem(
                    label = "Mandi Cess & Weighbridge Fee",
                    value = "-₹${formatter.format((sellNow.mandiFeeAndHandlingPerQtl * quantity).toInt())} (₹${sellNow.mandiFeeAndHandlingPerQtl}/Q)",
                    isDeduction = true
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.8.dp)

            // Action Buttons: FarmGate Buyer vs Mandi Delivery
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onInitiateMandi,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_sell_mandi")
                ) {
                    Text("Send to Mandi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onInitiateSellNow,
                    colors = ButtonDefaults.buttonColors(containerColor = FarmPrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_sell_buyer")
                ) {
                    Text("Sell to Buyer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

/**
 * WAIT / HOLD Option Financial Card.
 */
@Composable
private fun WaitAndHoldOptionCard(
    wait: WaitHoldingBreakdown,
    cropName: String,
    quantity: Double,
    onInitiateHold: () -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale("en", "IN")) }
    val isAdvantage = wait.netRevenueDeltaVsSellNow >= 0

    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isAdvantage) FarmTechBlue.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
                FarmSathiDesign.shapes.large
            )
            .testTag("wait_option_card")
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(FarmTechBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = FarmTechBlue, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OPTION B: WAIT ${wait.holdDurationDays} DAYS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Delta badge vs Sell Now
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isAdvantage) FarmSuccessGreenContainer else FarmAlertRedContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${if (isAdvantage) "+" else ""}₹${formatter.format(wait.netRevenueDeltaVsSellNow.toInt())}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isAdvantage) FarmSuccessGreen else FarmCriticalRed,
                        fontSize = 11.sp
                    )
                }
            }

            // Net Revenue Highlight
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = FarmTechBlueContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PROJECTED NET REVENUE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                        Text(
                            text = "₹${formatter.format(wait.netEstimatedRevenue.toInt())}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = FarmTechBlueText
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Projected Price",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "₹${formatter.format(wait.predictedPricePerQtl)}/Q",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FarmTechBlue
                        )
                        Text(
                            text = "Est: ₹${formatter.format(wait.predictedPriceRangeMin)}–${formatter.format(wait.predictedPriceRangeMax)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            // Detailed Holding Cost Breakdown
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CostLineItem(
                    label = "Projected Gross Revenue (${wait.effectiveQuantityAfterLoss} Q after shrinkage)",
                    value = "₹${formatter.format(wait.grossEstimatedRevenue.toInt())}",
                    isDeduction = false
                )
                CostLineItem(
                    label = "Certified Storage (${wait.holdDurationDays} days @ ₹${wait.storageCostPerQtlPerMonth}/Q/mo)",
                    value = "-₹${formatter.format(wait.totalStorageCost.toInt())}",
                    isDeduction = true
                )
                CostLineItem(
                    label = "Moisture Shrinkage & Loss (${wait.moistureShrinkageLossPercent}%)",
                    value = "-${(quantity * (wait.moistureShrinkageLossPercent / 100.0))} Q weight loss",
                    isDeduction = true
                )
                CostLineItem(
                    label = "Future Transportation Logistics",
                    value = "-₹${formatter.format(wait.totalFutureTransportCost.toInt())} (₹${wait.futureTransportationCostPerQtl}/Q)",
                    isDeduction = true
                )
                CostLineItem(
                    label = "Capital Opportunity Interest Cost",
                    value = "-₹${formatter.format(wait.capitalHoldingOpportunityCost.toInt())}",
                    isDeduction = true
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.8.dp)

            // Hold Storage Action Button
            Button(
                onClick = onInitiateHold,
                colors = ButtonDefaults.buttonColors(containerColor = FarmTechBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_lock_storage")
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lock Certified Storage & Hold Batch", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun CostLineItem(
    label: String,
    value: String,
    isDeduction: Boolean,
    highlightColor: Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = highlightColor ?: if (isDeduction) FarmCriticalRed else MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp
        )
    }
}

/**
 * "Why this recommendation?" Section.
 */
@Composable
private fun WhyThisRecommendationSection(scenario: SellDecisionScenario) {
    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.large)
            .testTag("why_recommendation_section")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = FarmPrimaryGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WHY THIS RECOMMENDATION?",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = FarmPrimaryDark,
                    letterSpacing = 0.5.sp
                )
            }

            // Agronomic and Market Reasons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                scenario.whyRecommendationReasons.forEach { reason ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(FarmSuccessGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = FarmSuccessGreen, modifier = Modifier.size(10.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.6.dp)

            // Risk & Caution Factors
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "RISKS & MONITORING TRIGGERS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
                scenario.riskFactors.forEach { risk ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(text = "⚠️", fontSize = 11.sp, modifier = Modifier.padding(end = 6.dp))
                        Text(
                            text = risk,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * "Ask FarmSathi" AI Advisory Section.
 */
@Composable
private fun AskFarmSathiSection(
    quickQuestions: List<String>,
    aiResponse: String?,
    isAsking: Boolean,
    onAskQuestion: (String) -> Unit,
    onOpenFullChat: () -> Unit
) {
    var customQuery by remember { mutableStateOf("") }

    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.large)
            .testTag("ask_farmsathi_section")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = FarmTechBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ASK FARMSATHI ADVISOR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmTechBlue,
                        letterSpacing = 0.5.sp
                    )
                }

                TextButton(onClick = onOpenFullChat) {
                    Text("Full AI Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FarmTechBlue)
                }
            }

            // Quick Prompt Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickQuestions) { question ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(FarmTechBlueContainer)
                            .clickable { onAskQuestion(question) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = FarmTechBlueText,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Query Input
            OutlinedTextField(
                value = customQuery,
                onValueChange = { customQuery = it },
                placeholder = { Text("Ask FarmSathi about storage, rates, or buyers...", fontSize = 12.sp) },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (customQuery.isNotBlank()) {
                                onAskQuestion(customQuery)
                                customQuery = ""
                            }
                        },
                        enabled = customQuery.isNotBlank() && !isAsking
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = FarmTechBlue)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_ask_farmsathi"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FarmTechBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // AI Answer Display
            if (isAsking) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = FarmTechBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("FarmSathi AI is calculating best monetization strategy...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (aiResponse != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmTechBlueContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = FarmTechBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("FarmSathi Advisory", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = FarmTechBlueText)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = aiResponse,
                            style = MaterialTheme.typography.bodySmall,
                            color = FarmTechBlueText,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Explicit Farmer Authorization Modal.
 * STRICT SAFETY DIRECTIVE: The system must never automatically sell crops. Every trade requires explicit farmer confirmation.
 */
@Composable
private fun ExplicitTradeConfirmationDialog(
    intent: FarmerTradeExecutionIntent,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale("en", "IN")) }
    var userAcceptedCheckbox by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = FarmPrimaryGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Farmer Authorization Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Safety Mandate Banner
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmHarvestGold.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = FarmHarvestGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Safety Guarantee: FarmSathi never automatically executes sales. Your explicit consent is strictly required.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Trade Summary Box
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ORDER EXECUTION SUMMARY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Commodity:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(intent.cropName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Quantity:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${intent.quantityToSellQuintals} Quintals", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Target Channel:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(intent.channelTargetName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Agreed Rate:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${formatter.format(intent.agreedPricePerQtl)} / Q", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Logistics:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(intent.transportArrangedBy, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.6.dp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Bank Payout:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("₹${formatter.format(intent.netPayoutToAccount.toInt())}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = FarmPrimaryDark)
                        }
                    }
                }

                // Explicit Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { userAcceptedCheckbox = !userAcceptedCheckbox }
                ) {
                    Checkbox(
                        checked = userAcceptedCheckbox,
                        onCheckedChange = { userAcceptedCheckbox = it },
                        modifier = Modifier.testTag("checkbox_farmer_confirm")
                    )
                    Text(
                        text = "I explicitly authorize this transaction and confirm my quantity and agreed rates.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = userAcceptedCheckbox,
                colors = ButtonDefaults.buttonColors(containerColor = FarmPrimaryGreen),
                modifier = Modifier.testTag("btn_confirm_trade_action")
            ) {
                Text("Confirm & Execute Sale", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        shape = FarmSathiDesign.shapes.large
    )
}

/**
 * Trade execution success banner.
 */
@Composable
private fun TradeSuccessBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = FarmSuccessGreenContainer),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FarmPrimaryGreen, FarmSathiDesign.shapes.medium)
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
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FarmPrimaryGreen, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = FarmPrimaryDark,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = FarmPrimaryDark, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/**
 * Mandatory Non-Guaranteed Disclaimer Card.
 */
@Composable
private fun DecisionDisclaimerCard() {
    Card(
        shape = FarmSathiDesign.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), FarmSathiDesign.shapes.medium)
            .testTag("decision_disclaimer_card")
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
                    text = "Statistical Estimate Notice",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "⚠️ Statistical Projection Notice: Projected prices, net holding revenues, and storage calculations are AI-assisted estimates based on historical APMC arrivals and weather dynamics. Projections are probabilistic and do NOT represent guaranteed prices. The final trade decision rests entirely with the farmer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

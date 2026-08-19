package com.example.features.market

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.navigation.Screen
import com.example.data.models.BuyerOffer
import com.example.data.models.MandiPriceItem
import com.example.shared.components.*
import com.example.ui.theme.*

@Composable
fun MarketScreen(
    viewModel: MarketViewModel,
    onNavigate: (Screen) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenLanguageSelector: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            FarmSathiTopBar(
                title = "Mandi Intelligence",
                subtitle = "Live APMC Rates • Sell-Now AI Insights",
                onNotificationsClick = onOpenNotifications,
                onLanguageClick = onOpenLanguageSelector
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Market Quick Action Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onNavigate(Screen.Buyers) },
                        colors = ButtonDefaults.buttonColors(containerColor = CleanMinPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Handshake, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Direct Buyers", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onNavigate(Screen.Logistics) },
                        colors = ButtonDefaults.buttonColors(containerColor = CleanMinContainer, contentColor = CleanMinOnContainerDark),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Shared Truck", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Sathi Recommendation Highlight Card (Clean Minimalism Blue: bg-[#E8F3FF] border-[#D1E4F9])
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanMinBlueContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CleanMinBlueBorder, RoundedCornerShape(28.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CleanMinBluePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SATHI PRICE ADVISORY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CleanMinBlueText,
                                    letterSpacing = 0.5.sp,
                                    fontSize = 10.sp
                                )
                            }
                            StatusBadge(text = "91% Confidence", type = StatusType.INFO)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "🟡 Soybean (Indore Mandi): Current rate ₹4,850/Q (+4.97%). High solvent extraction demand. Hold for 5–7 days; expected peak at ₹5,150/Q.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleanMinBlueText,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Crop Filter Chips
            item {
                val filters = listOf("All Crops", "Soybean", "Cotton", "Tomato", "Wheat")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { filter ->
                        FilterChip(
                            selected = state.selectedFilter == filter,
                            onClick = { viewModel.setFilter(filter) },
                            label = { Text(filter, fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = state.selectedFilter == filter,
                                borderColor = CleanMinBorder,
                                selectedBorderColor = CleanMinPrimary
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = CleanMinCard,
                                selectedContainerColor = CleanMinPrimary,
                                labelColor = CleanMinTextSecondary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Mandi Price Cards Section
            item {
                Text(
                    text = "Live Mandi Rates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CleanMinTextDark
                )
            }

            items(state.mandiPrices) { item ->
                MandiDetailPriceCard(
                    item = item,
                    onContactTraders = { onNavigate(Screen.Buyers) }
                )
            }

            // Direct Buyer Offers Snippet
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Verified Buyer Inquiries",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CleanMinTextDark
                    )
                    Text(
                        text = "VIEW ALL (${state.buyerOffers.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CleanMinPrimary,
                        fontSize = 11.sp,
                        modifier = Modifier.clickable { onNavigate(Screen.Buyers) }
                    )
                }
            }

            items(state.buyerOffers.take(2)) { buyer ->
                BuyerOfferCard(buyer = buyer, onAccept = { onNavigate(Screen.Buyers) })
            }
        }
    }
}

@Composable
private fun MandiDetailPriceCard(
    item: MandiPriceItem,
    onContactTraders: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CleanMinCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CleanMinBorder, RoundedCornerShape(28.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.cropName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleanMinTextDark
                    )
                    Text(
                        text = item.mandiName,
                        style = MaterialTheme.typography.bodySmall,
                        color = CleanMinTextMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${item.currentPricePerQuintal} /Q",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CleanMinTextDark
                    )
                    Text(
                        text = "${if (item.priceChangePercent >= 0) "+" else ""}${item.priceChangePercent}% ${if (item.priceChangePercent >= 0) "↑" else "↓"}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (item.priceChangePercent >= 0) CleanMinPrimary else CleanMinRed,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CleanMinCardVariant)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("DAY RANGE", style = MaterialTheme.typography.labelSmall, color = CleanMinTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("₹${item.minPrice} - ₹${item.maxPrice}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = CleanMinTextDark)
                }
                Column {
                    Text("7-DAY FORECAST", style = MaterialTheme.typography.labelSmall, color = CleanMinTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("₹${item.forecast7Days} /Q", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = CleanMinPrimary)
                }
                Column {
                    Text("AI CONFIDENCE", style = MaterialTheme.typography.labelSmall, color = CleanMinTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("${item.confidenceScore}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = CleanMinPrimaryLight)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = CleanMinContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = CleanMinPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.sellRecommendation,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = CleanMinOnContainerDark,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BuyerOfferCard(
    buyer: BuyerOffer,
    onAccept: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CleanMinCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CleanMinBorder, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = buyer.buyerName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CleanMinTextDark
                        )
                        if (buyer.verified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = CleanMinBluePrimary, modifier = Modifier.size(15.dp))
                        }
                    }
                    Text(
                        text = "${buyer.buyerCompany} • ${buyer.distanceKm} km away",
                        style = MaterialTheme.typography.bodySmall,
                        color = CleanMinTextMuted,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = "₹${buyer.offeredPricePerQuintal} /Q",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CleanMinPrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Need: ${buyer.quantityQuintals} Q • ${buyer.pickupLocation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CleanMinTextSecondary,
                    fontSize = 11.sp
                )
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = CleanMinContainer, contentColor = CleanMinOnContainerDark),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

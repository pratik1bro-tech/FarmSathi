package com.example.features.forecasting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.YieldForecastData
import com.example.data.repository.FarmRepository
import com.example.shared.components.*
import com.example.ui.theme.*

@Composable
fun ForecastingScreen(
    repository: FarmRepository,
    onBack: () -> Unit
) {
    val forecasts by repository.getYieldForecasts().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            FarmSathiTopBar(
                title = "AI Yield & Price Forecasting",
                subtitle = "Satellite NDVI & Machine Learning",
                showBackButton = true,
                onBackClick = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmGreenContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = FarmGreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Total Projected Farm Harvest Value",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = FarmOnGreenContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "₹2,53,925 Estimated Revenue",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = FarmGreenPrimary
                        )
                        Text(
                            text = "Based on current biomass vigor, soil health, and 7-day Mandi futures",
                            style = MaterialTheme.typography.bodySmall,
                            color = FarmOnGreenContainer
                        )
                    }
                }
            }

            items(forecasts) { item ->
                YieldForecastCard(forecast = item)
            }
        }
    }
}

@Composable
private fun YieldForecastCard(forecast: YieldForecastData) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(forecast.cropName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Field Area: ${forecast.areaAcres} Acres", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
                StatusBadge(text = "Harvest in ${forecast.harvestReadinessDays}d", type = StatusType.INFO)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Predicted Yield", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Text("${forecast.estimatedYieldQuintals} Quintals", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)
                }
                Column {
                    Text("Range (Min-Max)", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Text("${forecast.yieldRangeMin} - ${forecast.yieldRangeMax} Q", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Est. Revenue", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Text("₹${forecast.estimatedRevenueInr.toInt()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = HarvestGold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Key AI Growth Indicators:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            forecast.keyYieldDrivers.forEach { driver ->
                Text("• $driver", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

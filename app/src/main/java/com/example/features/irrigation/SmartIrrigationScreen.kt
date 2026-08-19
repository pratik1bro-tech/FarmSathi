package com.example.features.irrigation

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.IrrigationHistoryEvent
import com.example.data.models.IrrigationRecommendation
import com.example.data.models.IrrigationRecommendationStatus
import com.example.data.models.SmartIrrigationFieldState
import com.example.data.repository.FarmRepository
import com.example.designsystem.theme.FarmSathiDesign
import com.example.shared.components.FarmSathiTopBar
import com.example.ui.theme.*

@Composable
fun SmartIrrigationScreen(
    repository: FarmRepository,
    onBack: () -> Unit,
    onNavigateToAi: (() -> Unit)? = null,
    viewModel: SmartIrrigationViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val field = state.fieldState
    val recommendation = field.recommendation

    Scaffold(
        topBar = {
            FarmSathiTopBar(
                title = "Smart Irrigation",
                subtitle = field.fieldName,
                showBackButton = true,
                onBackClick = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("smart_irrigation_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Crop & Growth Stage Overview Card
            item {
                CropStageHeaderCard(field = field)
            }

            // 2. AI Irrigation Recommendation Card
            item {
                IrrigationRecommendationCard(
                    recommendation = recommendation,
                    rainProbability = field.rainProbabilityPercent,
                    onAskFarmSathi = { onNavigateToAi?.invoke() }
                )
            }

            // 3. Soil Moisture Visual Dual Gauge (Current vs Target)
            item {
                SoilMoistureVisualizationCard(
                    currentMoisture = field.currentSoilMoisture,
                    targetMin = field.targetMoistureMin,
                    targetMax = field.targetMoistureMax
                )
            }

            // 4. Microclimate & Rain Forecast Weather Card
            item {
                IrrigationWeatherCard(
                    tempCelsius = field.temperatureCelsius,
                    humidityPercent = field.humidityPercent,
                    rainProbability = field.rainProbabilityPercent,
                    rainSummary = field.rainForecastSummary,
                    et0 = field.evapotranspirationEt0
                )
            }

            // 5. Active Drip Valve & Solar Pump Controller
            item {
                ValveControllerCard(
                    isValveOpen = field.isValveOpen,
                    isActuating = state.isActuatingValve,
                    onToggleValve = { viewModel.toggleValve(it) }
                )
            }

            // 6. Irrigation History
            item {
                Text(
                    text = "IRRIGATION HISTORY / सिंचाई का इतिहास",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }

            items(state.history) { historyItem ->
                IrrigationHistoryItemCard(item = historyItem)
            }
        }
    }
}

// ================= 1. CROP & STAGE HEADER =================

@Composable
private fun CropStageHeaderCard(field: SmartIrrigationFieldState) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = field.crop,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Stage: ${field.cropGrowthStage}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Soil: ${field.soilType}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(FarmPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌾", fontSize = 22.sp)
            }
        }
    }
}

// ================= 2. RECOMMENDATION CARD =================

@Composable
private fun IrrigationRecommendationCard(
    recommendation: IrrigationRecommendation,
    rainProbability: Int,
    onAskFarmSathi: () -> Unit
) {
    val isRecommended = recommendation.status == IrrigationRecommendationStatus.RECOMMENDED
    val bannerBg = if (isRecommended) FarmHarvestGoldContainer else FarmSuccessGreenContainer
    val bannerText = if (isRecommended) FarmHarvestGold else FarmSuccessGreen
    val borderColor = if (isRecommended) FarmHarvestGold.copy(alpha = 0.4f) else FarmPrimaryGreen.copy(alpha = 0.4f)

    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, FarmSathiDesign.shapes.extraLarge)
            .testTag("irrigation_recommendation_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Status Tag & Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = FarmSathiDesign.shapes.pill,
                    color = bannerBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(bannerText)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = recommendation.status.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = bannerText,
                            fontSize = 11.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = bannerText,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Headline
            Text(
                text = recommendation.headline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Detailed Rationale from Backend Model
            Text(
                text = recommendation.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Metric Key Facts Grid (Rain Prob, Recommended Time, Water Requirement)
            Surface(
                shape = FarmSathiDesign.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Rain Probability
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = FarmTechBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rain Probability", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("$rainProbability%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    // Recommended Irrigation Window
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = FarmHarvestGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Recommended Time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(recommendation.recommendedTime, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = FarmHarvestGold)
                    }

                    // Estimated Water Requirement
                    if (recommendation.estimatedWaterRequirementLiters != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Opacity, contentDescription = null, tint = SkyWaterBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Water Requirement", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                "${recommendation.estimatedWaterRequirementLiters} L / Acre",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SkyWaterBlue
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ask FarmSathi AI Button
            Button(
                onClick = onAskFarmSathi,
                shape = FarmSathiDesign.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = FarmTechBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ask_farmsathi_irrigation_button")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ask FarmSathi AI About Irrigation", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ================= 3. SOIL MOISTURE VISUALIZATION =================

@Composable
private fun SoilMoistureVisualizationCard(
    currentMoisture: Double,
    targetMin: Double,
    targetMax: Double
) {
    val deficit = targetMin - currentMoisture
    val isDeficit = deficit > 0

    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SOIL MOISTURE GAUGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Root Zone Level",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = FarmSathiDesign.shapes.pill,
                    color = if (isDeficit) FarmHarvestGoldContainer else FarmSuccessGreenContainer
                ) {
                    Text(
                        text = if (isDeficit) String.format("-%.0f%% Deficit", deficit) else "Optimal Moisture",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDeficit) FarmHarvestGold else FarmSuccessGreen,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dual Value Callouts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Current Moisture", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = String.format("%.0f%%", currentMoisture),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDeficit) FarmHarvestGold else FarmPrimaryGreen
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Target Optimal Range", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = String.format("%.0f%% – %.0f%%", targetMin, targetMax),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = FarmPrimaryGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Range Gauge Bar
            SoilMoistureBarCanvas(
                current = currentMoisture.toFloat(),
                targetMin = targetMin.toFloat(),
                targetMax = targetMax.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Legend labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dry (0%)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Target Zone (55–65%)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = FarmPrimaryGreen)
                Text("Saturated (100%)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ---------------- CANVAS SOIL MOISTURE BAR ----------------

@Composable
private fun SoilMoistureBarCanvas(
    current: Float,
    targetMin: Float,
    targetMax: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val r = CornerRadius(h / 2f, h / 2f)

        // Background track
        drawRoundRect(
            color = Color(0xFFE0E0E0),
            size = Size(w, h),
            cornerRadius = r
        )

        // Optimal Target Zone Highlight
        val targetStartX = (targetMin / 100f) * w
        val targetWidth = ((targetMax - targetMin) / 100f) * w
        drawRect(
            color = FarmPrimaryContainer,
            topLeft = Offset(targetStartX, 0f),
            size = Size(targetWidth, h)
        )

        // Current Moisture Progress Fill
        val currentW = ((current / 100f) * w).coerceIn(0f, w)
        drawRoundRect(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFFFFA726), Color(0xFF2D6A4F))
            ),
            size = Size(currentW, h),
            cornerRadius = r
        )

        // Needle/Marker Line at current value
        drawCircle(
            color = Color.White,
            radius = (h / 2f) + 2.dp.toPx(),
            center = Offset(currentW, h / 2f)
        )
        drawCircle(
            color = FarmHarvestGold,
            radius = (h / 2f) - 2.dp.toPx(),
            center = Offset(currentW, h / 2f)
        )
    }
}

// ================= 4. WEATHER CARD =================

@Composable
private fun IrrigationWeatherCard(
    tempCelsius: Double,
    humidityPercent: Double,
    rainProbability: Int,
    rainSummary: String,
    et0: Double
) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "WEATHER & EVAPOTRANSPIRATION / मौसम पूर्वानुमान",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherMetricItem(icon = Icons.Default.Thermostat, label = "Temperature", value = "${tempCelsius}°C", color = FarmHarvestGold)
                WeatherMetricItem(icon = Icons.Default.WaterDrop, label = "Humidity", value = "${humidityPercent.toInt()}%", color = FarmTechBlue)
                WeatherMetricItem(icon = Icons.Default.CloudQueue, label = "Rain Prob.", value = "$rainProbability%", color = SkyWaterBlue)
                WeatherMetricItem(icon = Icons.Default.WbSunny, label = "ET₀ Rate", value = "$et0 mm/d", color = FarmPrimaryGreen)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = FarmSathiDesign.shapes.medium,
                color = FarmTechBlueContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = FarmTechBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = rainSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = FarmTechBlueText,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherMetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
    }
}

// ================= 5. VALVE CONTROLLER CARD =================

@Composable
private fun ValveControllerCard(
    isValveOpen: Boolean,
    isActuating: Boolean,
    onToggleValve: (Boolean) -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isValveOpen) FarmGreenContainer else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isValveOpen) FarmPrimaryGreen else MaterialTheme.colorScheme.outline,
                FarmSathiDesign.shapes.large
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isValveOpen) FarmPrimaryGreen else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = null,
                        tint = if (isValveOpen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Field 2 Drip Valve Actuator",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isValveOpen) "Valve OPEN • Flowing (2.2 L/min)" else "Valve CLOSED (Ready for 6:00 PM Cycle)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isValveOpen) FarmPrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            if (isActuating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Switch(
                    checked = isValveOpen,
                    onCheckedChange = onToggleValve
                )
            }
        }
    }
}

// ================= 6. IRRIGATION HISTORY CARD =================

@Composable
private fun IrrigationHistoryItemCard(item: IrrigationHistoryEvent) {
    Card(
        shape = FarmSathiDesign.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.medium)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.date,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    shape = FarmSathiDesign.shapes.pill,
                    color = FarmTechBlueContainer
                ) {
                    Text(
                        text = item.method,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = FarmTechBlueText,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Duration: ${item.durationMinutes} mins", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text("Moisture impact: ${item.moistureBeforeAfter}", style = MaterialTheme.typography.labelSmall, color = FarmSuccessGreen)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("${item.waterVolumeLiters} L", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SkyWaterBlue)
                    Text("Energy: ${item.energyConsumedKwh} kWh", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

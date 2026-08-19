package com.example.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.designsystem.theme.FarmSathiDesign
import com.example.designsystem.theme.FarmSemanticStatus
import com.example.ui.theme.*

/**
 * Base FarmCard with standardized border, surface color, and 28dp rounded corners.
 */
@Composable
fun FarmCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = FarmSathiDesign.elevation.none),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, FarmSathiDesign.shapes.extraLarge)
    ) {
        Column(
            modifier = Modifier.padding(FarmSathiDesign.spacing.spacing18),
            content = content
        )
    }
}

/**
 * Farm Health Card: Visualizes crop block status, growth stage, soil moisture, and health score.
 */
@Composable
fun FarmHealthCard(
    cropName: String,
    fieldName: String,
    healthScore: Int,
    growthStage: String,
    soilMoisture: Int,
    areaAcres: Double,
    onOpenDigitalTwin: () -> Unit,
    onOpenSoil: () -> Unit,
    onOpenIrrigation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticStatus = when {
        healthScore >= 85 -> FarmSemanticStatus.Healthy
        healthScore >= 65 -> FarmSemanticStatus.Moderate
        healthScore >= 45 -> FarmSemanticStatus.Warning
        else -> FarmSemanticStatus.Critical
    }

    FarmCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = fieldName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp,
                    fontSize = 10.sp
                )
                Text(
                    text = cropName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            FarmStatusBadge(
                status = semanticStatus,
                customLabel = "$healthScore% Health"
            )
        }

        Spacer(modifier = Modifier.height(FarmSathiDesign.spacing.spacing14))

        // Metrics grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FarmSathiDesign.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(FarmSathiDesign.spacing.spacing12),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("AREA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                Text("$areaAcres Ac", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            Column {
                Text("STAGE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                Text(growthStage, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            Column {
                Text("MOISTURE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                Text("$soilMoisture%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(FarmSathiDesign.spacing.spacing14))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FarmSathiDesign.spacing.spacing8)
        ) {
            FarmOutlinedButton(
                text = "NPK",
                leadingIcon = Icons.Default.Spa,
                onClick = onOpenSoil,
                modifier = Modifier.weight(1f)
            )
            FarmOutlinedButton(
                text = "Water",
                leadingIcon = Icons.Default.WaterDrop,
                onClick = onOpenIrrigation,
                modifier = Modifier.weight(1f)
            )
            FarmSecondaryButton(
                text = "3D Twin",
                leadingIcon = Icons.Default.Layers,
                onClick = onOpenDigitalTwin,
                modifier = Modifier.weight(1.1f)
            )
        }
    }
}

/**
 * Telemetry Card: Displays live IoT sensor telemetry (Moisture, Temp, Battery, Solar Pump).
 */
@Composable
fun FarmTelemetryCard(
    telemetry: TelemetryData,
    onCalibrateSensors: () -> Unit,
    modifier: Modifier = Modifier
) {
    FarmCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(FarmSathiDesign.shapes.small)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(FarmSathiDesign.spacing.spacing10))
                Column {
                    Text(
                        text = telemetry.deviceId,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Synced: ${telemetry.lastSyncTime} • Battery: ${telemetry.batteryPercent}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
            FarmStatusBadge(
                status = if (telemetry.isOnline) FarmSemanticStatus.Live else FarmSemanticStatus.Offline
            )
        }

        Spacer(modifier = Modifier.height(FarmSathiDesign.spacing.spacing14))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FarmSathiDesign.spacing.spacing8)
        ) {
            TelemetryMetricPill(
                label = "SOIL MOISTURE",
                value = "${telemetry.soilMoistureTop10cm}%",
                modifier = Modifier.weight(1f)
            )
            TelemetryMetricPill(
                label = "SOIL TEMP",
                value = "${telemetry.soilTemperature}°C",
                modifier = Modifier.weight(1f)
            )
            TelemetryMetricPill(
                label = "SOLAR PUMP",
                value = if (telemetry.solarPumpRunning) "ON" else "OFF",
                isHighlighted = telemetry.solarPumpRunning,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TelemetryMetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    Column(
        modifier = modifier
            .clip(FarmSathiDesign.shapes.medium)
            .background(
                if (isHighlighted) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(FarmSathiDesign.spacing.spacing10),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isHighlighted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * AI Recommendation Card: Highlights actionable agricultural intelligence from FarmSathi AI.
 */
@Composable
fun FarmAiRecommendationCard(
    title: String,
    recommendation: String,
    confidencePercent: Int,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = FarmTechBlueContainer),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, FarmTechBlueBorder, FarmSathiDesign.shapes.extraLarge)
    ) {
        Column(modifier = Modifier.padding(FarmSathiDesign.spacing.spacing18)) {
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
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(FarmSathiDesign.spacing.spacing10))
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmTechBlueText,
                        letterSpacing = 0.5.sp,
                        fontSize = 10.sp
                    )
                }
                FarmStatusBadge(
                    status = FarmSemanticStatus.Cached,
                    customLabel = "$confidencePercent% AI Confidence"
                )
            }

            Spacer(modifier = Modifier.height(FarmSathiDesign.spacing.spacing10))

            Text(
                text = recommendation,
                style = MaterialTheme.typography.bodyMedium,
                color = FarmTechBlueText,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium
            )

            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(FarmSathiDesign.spacing.spacing12))
                Button(
                    onClick = onActionClick,
                    shape = FarmSathiDesign.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FarmTechBlue,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = actionLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Alert Card: Highlights severe alerts (Outbreak, Flash Rain, Mandi drop).
 */
@Composable
fun FarmAlertCard(
    title: String,
    description: String,
    severity: FarmSemanticStatus,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isCritical = severity == FarmSemanticStatus.Critical

    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (isCritical) MaterialTheme.colorScheme.errorContainer else FarmHarvestGoldContainer
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isCritical) MaterialTheme.colorScheme.error.copy(alpha = 0.3f) else FarmHarvestGold.copy(alpha = 0.3f),
                FarmSathiDesign.shapes.extraLarge
            )
    ) {
        Column(modifier = Modifier.padding(FarmSathiDesign.spacing.spacing16)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCritical) Icons.Default.Error else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isCritical) MaterialTheme.colorScheme.error else FarmHarvestGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(FarmSathiDesign.spacing.spacing8))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCritical) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF4A3800)
                    )
                }
                FarmStatusBadge(status = severity)
            }

            Spacer(modifier = Modifier.height(FarmSathiDesign.spacing.spacing8))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isCritical) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF4A3800),
                lineHeight = 18.sp
            )

            if (actionText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(FarmSathiDesign.spacing.spacing10))
                TextButton(
                    onClick = onActionClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = actionText.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCritical) MaterialTheme.colorScheme.error else FarmHarvestGold
                    )
                }
            }
        }
    }
}

/**
 * Market Card: Mandi intelligence with current price, 7-day forecast, and recommendation.
 */
@Composable
fun FarmMarketCard(
    item: MandiPriceItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FarmCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = item.cropName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.mandiName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${item.currentPricePerQuintal} /Q",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (item.priceChangePercent >= 0) "+" else ""}${item.priceChangePercent}% ${if (item.priceChangePercent >= 0) "↑" else "↓"}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (item.priceChangePercent >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(FarmSathiDesign.spacing.spacing12))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FarmSathiDesign.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(FarmSathiDesign.spacing.spacing10),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("DAY RANGE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                Text("₹${item.minPrice} - ₹${item.maxPrice}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
            Column {
                Text("7-DAY FORECAST", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                Text("₹${item.forecast7Days} /Q", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Column {
                Text("CONFIDENCE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                Text("${item.confidenceScore}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(FarmSathiDesign.spacing.spacing10))

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = FarmSathiDesign.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(FarmSathiDesign.spacing.spacing6))
                Text(
                    text = item.sellRecommendation,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * Buyer Card: Verified miller or wholesaler direct buyer offer.
 */
@Composable
fun FarmBuyerCard(
    buyer: BuyerOffer,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    FarmCard(modifier = modifier) {
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
                        fontWeight = FontWeight.Bold
                    )
                    if (buyer.verified) {
                        Spacer(modifier = Modifier.width(FarmSathiDesign.spacing.spacing4))
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = FarmTechBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Text(
                    text = "${buyer.buyerCompany} • ${buyer.distanceKm} km away",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            Text(
                text = "₹${buyer.offeredPricePerQuintal} /Q",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(FarmSathiDesign.spacing.spacing10))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Need: ${buyer.quantityQuintals} Q • ${buyer.pickupLocation}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            FarmSecondaryButton(
                text = "Connect",
                onClick = onConnect
            )
        }
    }
}

/**
 * Logistics Card: Shared freight transportation pooling for mandi transport.
 */
@Composable
fun FarmLogisticsCard(
    trip: LogisticsTrip,
    onBookSpace: () -> Unit,
    modifier: Modifier = Modifier
) {
    FarmCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(FarmSathiDesign.shapes.small)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(FarmSathiDesign.spacing.spacing10))
                Column {
                    Text(
                        text = trip.truckType,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Departs: ${trip.departureTime} → ${trip.destinationMandi}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
            FarmStatusBadge(
                status = FarmSemanticStatus.Healthy,
                customLabel = "${trip.availableSpaceTons} T Left"
            )
        }

        Spacer(modifier = Modifier.height(FarmSathiDesign.spacing.spacing12))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "₹${trip.costPerQuintalInr} / Quintal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${trip.pooledFarmersCount} farmers pooled (45% freight saved)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
            FarmPrimaryButton(
                text = "Book Space",
                onClick = onBookSpace
            )
        }
    }
}

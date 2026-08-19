package com.example.features.telemetry

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.*
import com.example.data.repository.FarmRepository
import com.example.designsystem.theme.FarmSathiDesign
import com.example.shared.components.FarmSathiTopBar
import com.example.ui.theme.*

@Composable
fun TelemetryScreen(
    repository: FarmRepository,
    onBack: () -> Unit,
    viewModel: TelemetryViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val telemetry = state.telemetry

    Scaffold(
        topBar = {
            FarmSathiTopBar(
                title = "ESP32 IoT Telemetry",
                subtitle = "Node: ${telemetry.nodeId}",
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
                .testTag("telemetry_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Connection Status & Gateway Hardware Banner
            item {
                GatewayStatusCard(
                    telemetry = telemetry,
                    isRefreshing = state.isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    onSelectConnectionState = { viewModel.setConnectionMode(it) }
                )
            }

            // 2. Interactive Historical Trend Chart Section
            item {
                HistoricalTelemetryChartCard(
                    selectedMetricId = state.selectedMetricId,
                    selectedTimeRange = state.selectedTimeRange,
                    historicalData = state.historicalData,
                    sensors = telemetry.sensors,
                    onSelectMetric = { viewModel.selectMetric(it) },
                    onSelectTimeRange = { viewModel.selectTimeRange(it) }
                )
            }

            // 3. Live Sensor Telemetry Matrix (All 7 Soil & Microclimate Metrics)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE SENSOR METRICS (7 NODES)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Freshness: ${telemetry.freshness}",
                        style = MaterialTheme.typography.labelSmall,
                        color = FarmPrimaryGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                }
            }

            items(telemetry.sensors.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SensorMetricCard(
                        sensor = pair[0],
                        isSelected = state.selectedMetricId == pair[0].id,
                        onClick = { viewModel.selectMetric(pair[0].id) },
                        modifier = Modifier.weight(1f)
                    )
                    if (pair.size > 1) {
                        SensorMetricCard(
                            sensor = pair[1],
                            isSelected = state.selectedMetricId == pair[1].id,
                            onClick = { viewModel.selectMetric(pair[1].id) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ================= 1. GATEWAY HARDWARE STATUS CARD =================

@Composable
private fun GatewayStatusCard(
    telemetry: Esp32NodeTelemetry,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSelectConnectionState: (DataConnectionState) -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Connection status pill & Refresh button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ConnectionStateBadge(state = telemetry.connectionState)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = telemetry.fieldName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("telemetry_refresh_button")
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = FarmTechBlue,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Telemetry",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Telemetry hardware stats (Battery, RSSI, Sync time)
            Surface(
                shape = FarmSathiDesign.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Battery
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (telemetry.batteryPercent > 20) Icons.Default.BatteryChargingFull else Icons.Default.BatteryAlert,
                            contentDescription = null,
                            tint = if (telemetry.batteryPercent > 20) FarmSuccessGreen else FarmAlertRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("Battery", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${telemetry.batteryPercent}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // RSSI Signal
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = FarmTechBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("Signal (RSSI)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${telemetry.signalDbm} dBm", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Last Sync
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("Last Sync", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(telemetry.lastSyncTime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // State Simulation Switcher (LIVE / CACHED / OFFLINE / UNAVAILABLE)
            Text(
                text = "TEST CONNECTION RESILIENCE:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DataConnectionState.entries.forEach { mode ->
                    val isSelected = telemetry.connectionState == mode
                    Surface(
                        onClick = { onSelectConnectionState(mode) },
                        shape = FarmSathiDesign.shapes.pill,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.pill)
                    ) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(vertical = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionStateBadge(state: DataConnectionState) {
    val (bgColor, textColor, dotColor) = when (state) {
        DataConnectionState.LIVE -> Triple(FarmSuccessGreenContainer, FarmSuccessGreen, FarmSuccessGreen)
        DataConnectionState.CACHED -> Triple(FarmTechBlueContainer, FarmTechBlue, FarmTechBlue)
        DataConnectionState.OFFLINE -> Triple(FarmWarningAmberContainer, FarmWarningAmber, FarmWarningAmber)
        DataConnectionState.UNAVAILABLE -> Triple(FarmAlertRedContainer, FarmAlertRed, FarmAlertRed)
    }

    Surface(
        shape = FarmSathiDesign.shapes.pill,
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = state.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 10.sp
            )
        }
    }
}

// ================= 2. HISTORICAL TELEMETRY CHART CARD =================

@Composable
private fun HistoricalTelemetryChartCard(
    selectedMetricId: String,
    selectedTimeRange: TelemetryTimeRange,
    historicalData: List<TelemetryChartDataPoint>,
    sensors: List<TelemetrySensorMetric>,
    onSelectMetric: (String) -> Unit,
    onSelectTimeRange: (TelemetryTimeRange) -> Unit
) {
    val activeSensor = sensors.find { it.id == selectedMetricId } ?: sensors.firstOrNull()

    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Chart Title & Time Range Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HISTORICAL TELEMETRY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${activeSensor?.name ?: "Sensor"} Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Time Range Segmented Selector (24H, 7D, 30D)
                Surface(
                    shape = FarmSathiDesign.shapes.pill,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.pill)
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        TelemetryTimeRange.entries.forEach { range ->
                            val isSelected = selectedTimeRange == range
                            Surface(
                                onClick = { onSelectTimeRange(range) },
                                shape = FarmSathiDesign.shapes.pill,
                                color = if (isSelected) FarmTechBlue else Color.Transparent
                            ) {
                                Text(
                                    text = range.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metric Selector Pills
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(sensors) { sensor ->
                    val isSelected = sensor.id == selectedMetricId
                    Surface(
                        onClick = { onSelectMetric(sensor.id) },
                        shape = FarmSathiDesign.shapes.pill,
                        color = if (isSelected) FarmTechBlueContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.border(
                            1.dp,
                            if (isSelected) FarmTechBlue else MaterialTheme.colorScheme.outline,
                            FarmSathiDesign.shapes.pill
                        )
                    ) {
                        Text(
                            text = "${sensor.name} (${sensor.currentValue}${sensor.unit})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) FarmTechBlueText else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Smooth Canvas Line Chart
            TelemetryLineChart(
                points = historicalData,
                unit = activeSensor?.unit ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Metric Stats Summary (Min, Max, Avg, Target Range)
            val values = historicalData.map { it.value }
            val minVal = values.minOrNull() ?: 0.0
            val maxVal = values.maxOrNull() ?: 0.0
            val avgVal = if (values.isNotEmpty()) values.average() else 0.0

            Surface(
                shape = FarmSathiDesign.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Min", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(String.format("%.1f%s", minVal, activeSensor?.unit ?: ""), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Average", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(String.format("%.1f%s", avgVal, activeSensor?.unit ?: ""), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FarmTechBlue)
                    }
                    Column {
                        Text("Max", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(String.format("%.1f%s", maxVal, activeSensor?.unit ?: ""), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Optimal Range", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(activeSensor?.optimalRange ?: "Optimal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FarmSuccessGreen)
                    }
                }
            }
        }
    }
}

// ---------------- CUSTOM CANVAS LINE CHART ----------------

@Composable
private fun TelemetryLineChart(
    points: List<TelemetryChartDataPoint>,
    unit: String,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val min = points.minOf { it.value }
    val max = points.maxOf { it.value }
    val range = if (max - min == 0.0) 1.0 else max - min
    val primaryColor = FarmTechBlue
    val primaryColorLight = FarmTechBlueLight

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val paddingBottom = 20.dp.toPx()
        val chartHeight = h - paddingBottom
        val stepX = w / (points.size - 1).coerceAtLeast(1)

        // Draw horizontal grid guide lines
        for (i in 0..3) {
            val y = (chartHeight / 3f) * i
            drawLine(
                color = Color.LightGray.copy(alpha = 0.35f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
            )
        }

        // Compute coordinate points
        val coords = points.mapIndexed { index, dp ->
            val x = index * stepX
            val norm = (dp.value - min) / range
            val y = chartHeight - (norm * (chartHeight * 0.8f)) - (chartHeight * 0.1f)
            Offset(x, y.toFloat())
        }

        // Draw gradient filled area under line
        val fillPath = Path().apply {
            moveTo(coords.first().x, chartHeight)
            coords.forEach { lineTo(it.x, it.y) }
            lineTo(coords.last().x, chartHeight)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColorLight.copy(alpha = 0.45f), primaryColor.copy(alpha = 0.02f)),
                startY = 0f,
                endY = chartHeight
            )
        )

        // Draw continuous smooth trend line
        val strokePath = Path().apply {
            moveTo(coords.first().x, coords.first().y)
            for (i in 1 until coords.size) {
                val prev = coords[i - 1]
                val curr = coords[i]
                val midX = (prev.x + curr.x) / 2f
                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
            }
        }

        drawPath(
            path = strokePath,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw point dots
        coords.forEachIndexed { i, pt ->
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = primaryColor,
                radius = 3.dp.toPx(),
                center = pt
            )
        }
    }
}

// ================= 3. SENSOR METRIC CARD =================

@Composable
private fun SensorMetricCard(
    sensor: TelemetrySensorMetric,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusBg, statusText) = when (sensor.status) {
        SensorHealthStatus.OPTIMAL -> FarmSuccessGreenContainer to FarmSuccessGreen
        SensorHealthStatus.WARNING -> FarmHarvestGoldContainer to FarmHarvestGold
        SensorHealthStatus.CRITICAL -> FarmAlertRedContainer to FarmAlertRed
        SensorHealthStatus.CALIBRATING -> FarmTechBlueContainer to FarmTechBlue
    }

    Card(
        onClick = onClick,
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) FarmTechBlueContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) FarmTechBlue else MaterialTheme.colorScheme.outline,
                shape = FarmSathiDesign.shapes.large
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Sensor Icon & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) FarmTechBlue else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (sensor.iconType) {
                            "moisture" -> Icons.Default.WaterDrop
                            "temp" -> Icons.Default.Thermostat
                            "humidity" -> Icons.Default.CloudQueue
                            "ph" -> Icons.Default.Science
                            "npk_n" -> Icons.Default.Eco
                            "npk_p" -> Icons.Default.Spa
                            else -> Icons.Default.Grass
                        },
                        contentDescription = null,
                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Surface(
                    shape = FarmSathiDesign.shapes.pill,
                    color = statusBg
                ) {
                    Text(
                        text = sensor.status.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusText,
                        fontSize = 8.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Value & Unit
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format("%.1f", sensor.currentValue),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = sensor.unit,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Sensor Title & Hindi label
            Text(
                text = sensor.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = sensor.hindiName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Freshness indicator
            Text(
                text = "Updated ${sensor.freshness}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 9.sp
            )
        }
    }
}

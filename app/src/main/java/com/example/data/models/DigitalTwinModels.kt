package com.example.data.models

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class FieldHealthStatus(
    val label: String,
    val color: Color,
    val containerColor: Color,
    val iconEmoji: String
) {
    HEALTHY(
        label = "Healthy",
        color = FarmSuccessGreen,
        containerColor = FarmSuccessGreenContainer,
        iconEmoji = "🟢"
    ),
    WARNING(
        label = "Warning",
        color = FarmWarningAmber,
        containerColor = FarmWarningAmberContainer,
        iconEmoji = "🟡"
    ),
    CRITICAL(
        label = "Critical",
        color = FarmAlertRed,
        containerColor = FarmAlertRedContainer,
        iconEmoji = "🔴"
    )
}

data class FarmFieldParcel(
    val fieldId: String,
    val fieldName: String,
    val areaAcres: Double,
    val cropName: String,
    val growthStage: String,
    val healthStatus: FieldHealthStatus,
    val healthScore: Int,
    val soilMoisturePct: Double,
    val diseaseRisk: String,
    val sensorStatus: String,
    val ndviIndex: Double,
    val temperatureC: Double
)

enum class WhatIfScenario(
    val title: String,
    val subtitle: String,
    val iconEmoji: String
) {
    DELAY_IRRIGATION_3_DAYS(
        title = "Delay Irrigation 3 Days",
        subtitle = "Simulate 72h moisture depletion without water application",
        iconEmoji = "⏱️"
    ),
    IRRIGATE_TODAY(
        title = "Irrigate Today (25mm)",
        subtitle = "Simulate immediate root-zone drip irrigation cycle",
        iconEmoji = "💧"
    ),
    HEAVY_RAINFALL(
        title = "Heavy Rainfall (45mm)",
        subtitle = "Simulate unseasonal 45mm downpour in 6 hours",
        iconEmoji = "🌧️"
    ),
    EXTREME_HEATWAVE(
        title = "Extreme Heatwave (40°C)",
        subtitle = "Simulate 4 consecutive days above 40°C canopy heat",
        iconEmoji = "☀️"
    )
}

data class WhatIfSimulationResult(
    val scenario: WhatIfScenario,
    val fieldName: String,
    val currentMoisturePct: Double,
    val predictedSoilMoisturePct: Double,
    val moistureChangeLabel: String,
    val cropStressLevel: String,
    val cropStressColor: Color,
    val estimatedYieldImpactPct: Double,
    val yieldImpactLabel: String,
    val yieldImpactColor: Color,
    val assumptionsList: List<String>,
    val uncertaintyMargin: String,
    val AIAdvice: String
)

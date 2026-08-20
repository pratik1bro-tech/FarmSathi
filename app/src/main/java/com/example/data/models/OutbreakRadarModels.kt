package com.example.data.models

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class RegionalRiskLevel(
    val colorCode: String,
    val title: String,
    val hindiTitle: String,
    val color: Color,
    val containerColor: Color
) {
    GREEN_LOW(
        colorCode = "GREEN",
        title = "LOW",
        hindiTitle = "निम्न जोखिम",
        color = FarmSuccessGreen,
        containerColor = FarmSuccessGreenContainer
    ),
    YELLOW_MODERATE(
        colorCode = "YELLOW",
        title = "MODERATE",
        hindiTitle = "मध्यम जोखिम",
        color = FarmWarningAmber,
        containerColor = FarmWarningAmberContainer
    ),
    RED_HIGH(
        colorCode = "RED",
        title = "HIGH",
        hindiTitle = "उच्च जोखिम",
        color = FarmAlertRed,
        containerColor = FarmAlertRedContainer
    )
}

data class FarmRiskStatus(
    val yourFarmRisk: RegionalRiskLevel,
    val yourFarmRiskReason: String,
    val regionalRisk: RegionalRiskLevel,
    val regionalRiskReason: String,
    val radarRadiusKm: Double = 25.0,
    val totalActiveReportsInRadius: Int = 24,
    val lastUpdated: String = "10 minutes ago"
)

data class AggregatedOutbreakReport(
    val id: String,
    val diseaseName: String,
    val hindiDiseaseName: String,
    val affectedCrop: String,
    val regionName: String,
    val distanceKm: Double,
    val reportCount: Int,
    val riskLevel: RegionalRiskLevel,
    val lastUpdated: String,
    val spreadDirection: String,
    val preventionGuidance: String
)

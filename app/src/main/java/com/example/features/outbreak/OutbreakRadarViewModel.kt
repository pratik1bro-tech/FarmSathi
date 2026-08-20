package com.example.features.outbreak

import androidx.lifecycle.ViewModel
import com.example.data.models.AggregatedOutbreakReport
import com.example.data.models.FarmRiskStatus
import com.example.data.models.RegionalRiskLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OutbreakRadarUiState(
    val farmStatus: FarmRiskStatus = FarmRiskStatus(
        yourFarmRisk = RegionalRiskLevel.YELLOW_MODERATE,
        yourFarmRiskReason = "Ambient humidity (82%) and proximity to Sanwer cluster increase fungal spore germination risk on active Soybean crop.",
        regionalRisk = RegionalRiskLevel.RED_HIGH,
        regionalRiskReason = "24 verified outbreak reports within 25 km corridor driven by high Easterly wind dispersion.",
        radarRadiusKm = 25.0,
        totalActiveReportsInRadius = 24,
        lastUpdated = "10 minutes ago"
    ),
    val reports: List<AggregatedOutbreakReport> = listOf(
        AggregatedOutbreakReport(
            id = "OUTBREAK_001",
            diseaseName = "Soybean Asian Rust (Phakopsora pachyrhizi)",
            hindiDiseaseName = "सोयाबीन एशियाई गेरुआ रोग",
            affectedCrop = "Soybean (JS 20-34)",
            regionName = "Sanwer-Kanadiya Corridor",
            distanceKm = 6.2,
            reportCount = 18,
            riskLevel = RegionalRiskLevel.RED_HIGH,
            lastUpdated = "12 mins ago",
            spreadDirection = "Eastward @ 12 km/h wind vector",
            preventionGuidance = "Execute prophylactic spray of Hexaconazole 5% EC @ 2ml/L or Azoxystrobin. Inspect leaf undersides for brownish pustules."
        ),
        AggregatedOutbreakReport(
            id = "OUTBREAK_002",
            diseaseName = "Fall Armyworm (Spodoptera frugiperda)",
            hindiDiseaseName = "फॉल्स आर्मीवर्म कीट",
            affectedCrop = "Maize / Corn",
            regionName = "Depalpur Agricultural Belt",
            distanceKm = 14.8,
            reportCount = 5,
            riskLevel = RegionalRiskLevel.YELLOW_MODERATE,
            lastUpdated = "45 mins ago",
            spreadDirection = "Localized whorl feeding clusters",
            preventionGuidance = "Apply Emamectin Benzoate 5% SG @ 0.4g/L directly into plant whorls during morning hours."
        ),
        AggregatedOutbreakReport(
            id = "OUTBREAK_003",
            diseaseName = "Yellow Vein Mosaic Virus (YVMV)",
            hindiDiseaseName = "पीला मोज़ेक वायरस (सफेद मक्खी)",
            affectedCrop = "Okra / Cotton",
            regionName = "Ujjain Border Sub-district",
            distanceKm = 22.1,
            reportCount = 1,
            riskLevel = RegionalRiskLevel.GREEN_LOW,
            lastUpdated = "2 hours ago",
            spreadDirection = "Whitefly vector movement",
            preventionGuidance = "Install yellow sticky traps (10 traps/acre) and spray Imidacloprid 17.8% SL @ 0.5ml/L to control vector population."
        )
    )
)

class OutbreakRadarViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OutbreakRadarUiState())
    val uiState: StateFlow<OutbreakRadarUiState> = _uiState.asStateFlow()
}

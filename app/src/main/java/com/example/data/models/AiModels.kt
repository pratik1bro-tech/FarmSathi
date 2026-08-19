package com.example.data.models

enum class FarmAiPriority(val label: String, val level: Int) {
    CRITICAL("CRITICAL ACTION", 4),
    HIGH("HIGH PRIORITY", 3),
    MEDIUM("ADVISORY", 2),
    OPTIMAL("HEALTHY / OPTIMAL", 1),
    INFO("INFO", 0)
}

enum class OrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    OFFLINE
}

data class FarmAiAction(
    val label: String,
    val targetRoute: String? = null,
    val prompt: String? = null,
    val iconName: String = "arrow"
)

data class FarmAiMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: String,
    val priority: FarmAiPriority? = null,
    val reason: String? = null,
    val recommendedActions: List<FarmAiAction> = emptyList(),
    val sourceRoute: String? = null,
    val audioAvailable: Boolean = true
)

data class FarmIntelligenceContext(
    val farmerProfile: FarmerProfile = FarmerProfile(),
    val cropFields: List<CropField> = emptyList(),
    val telemetry: TelemetryData = TelemetryData(),
    val soilHealth: SoilHealthReport = SoilHealthReport(),
    val weather: WeatherForecast = WeatherForecast(),
    val mandiPrices: List<MandiPriceItem> = emptyList(),
    val buyerOffers: List<BuyerOffer> = emptyList(),
    val outbreakAlerts: List<OutbreakAlert> = emptyList()
)

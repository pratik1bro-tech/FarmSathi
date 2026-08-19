package com.example.data.models

enum class IrrigationRecommendationStatus(
    val title: String,
    val hindiTitle: String
) {
    RECOMMENDED("IRRIGATION RECOMMENDED", "सिंचाई की आवश्यकता है"),
    NOT_NEEDED("IRRIGATION NOT NEEDED", "सिंचाई की आवश्यकता नहीं"),
    DELAY_DUE_TO_RAIN("DELAY DUE TO RAIN", "बारिश के कारण स्थगित")
}

data class IrrigationRecommendation(
    val status: IrrigationRecommendationStatus,
    val headline: String,
    val explanation: String,
    val recommendedTime: String,
    val estimatedWaterRequirementLiters: Int? = null,
    val estimatedDurationMinutes: Int? = null,
    val methodRecommended: String = "Drip Irrigation",
    val waterSavingsVersusFlood: String = "42% water conserved vs flood method"
)

data class SmartIrrigationFieldState(
    val fieldId: String = "FIELD_COTTON_02",
    val fieldName: String = "Field 2 (South Cotton Block)",
    val crop: String = "Cotton (BT-RCH-659)",
    val cropGrowthStage: String = "Flowering & Boll Formation (Day 62)",
    val soilType: String = "Deep Black Clay Loam (Vertisol)",
    val currentSoilMoisture: Double = 38.0,
    val targetMoistureMin: Double = 55.0,
    val targetMoistureMax: Double = 65.0,
    val temperatureCelsius: Double = 31.2,
    val humidityPercent: Double = 62.0,
    val rainProbabilityPercent: Int = 15,
    val rainForecastSummary: String = "15% rain probability over next 36 hours",
    val evapotranspirationEt0: Double = 5.4, // mm/day
    val isValveOpen: Boolean = false,
    val recommendation: IrrigationRecommendation = IrrigationRecommendation(
        status = IrrigationRecommendationStatus.RECOMMENDED,
        headline = "Soil moisture is below the target range.",
        explanation = "Current root-zone moisture (38%) is 17% below the optimal 55–65% threshold for the boll development stage. Low rain probability (15%) allows safe irrigation. Evening scheduling avoids midday evaporation.",
        recommendedTime = "6:00 PM – 8:00 PM",
        estimatedWaterRequirementLiters = 18500,
        estimatedDurationMinutes = 120,
        methodRecommended = "Precision Drip Irrigation (2.2 L/hr inline emitters)"
    )
)

data class IrrigationHistoryEvent(
    val id: String,
    val date: String,
    val fieldName: String,
    val crop: String,
    val durationMinutes: Int,
    val waterVolumeLiters: Int,
    val method: String,
    val moistureBeforeAfter: String,
    val energyConsumedKwh: Double
)

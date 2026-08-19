package com.example.data.repository

import com.example.data.models.IrrigationHistoryEvent
import com.example.data.models.IrrigationRecommendation
import com.example.data.models.IrrigationRecommendationStatus
import com.example.data.models.SmartIrrigationFieldState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface SmartIrrigationRepository {
    fun getIrrigationState(): Flow<SmartIrrigationFieldState>
    fun getIrrigationHistory(): Flow<List<IrrigationHistoryEvent>>
    suspend fun toggleIrrigationValve(open: Boolean): Result<Boolean>
    suspend fun refreshIrrigationAdvisory(): Result<SmartIrrigationFieldState>
}

class DevelopmentSmartIrrigationRepository : SmartIrrigationRepository {

    private val _fieldState = MutableStateFlow(
        SmartIrrigationFieldState(
            fieldId = "FIELD_COTTON_02",
            fieldName = "Field 2 (South Cotton Block)",
            crop = "Cotton (BT-RCH-659)",
            cropGrowthStage = "Flowering & Boll Formation",
            soilType = "Deep Black Clay Loam",
            currentSoilMoisture = 38.0,
            targetMoistureMin = 55.0,
            targetMoistureMax = 65.0,
            temperatureCelsius = 31.2,
            humidityPercent = 62.0,
            rainProbabilityPercent = 15,
            rainForecastSummary = "15% chance of light drizzle; no heavy rain expected",
            evapotranspirationEt0 = 5.4,
            isValveOpen = false,
            recommendation = IrrigationRecommendation(
                status = IrrigationRecommendationStatus.RECOMMENDED,
                headline = "Soil moisture is below the target range.",
                explanation = "Root-zone sensor telemetry (38%) indicates moderate soil water deficit during critical boll formation. Rain probability is low (15%). Apply evening drip irrigation to maximize absorption.",
                recommendedTime = "6:00 PM – 8:00 PM",
                estimatedWaterRequirementLiters = 18500,
                estimatedDurationMinutes = 120,
                methodRecommended = "Drip Irrigation (2.2 L/hr emitters)",
                waterSavingsVersusFlood = "42% water conserved vs flood furrow method"
            )
        )
    )

    private val _history = MutableStateFlow(
        listOf(
            IrrigationHistoryEvent(
                id = "irr_hist_1",
                date = "16 Aug 2026, 6:00 PM",
                fieldName = "Field 2 (South Cotton Block)",
                crop = "Cotton",
                durationMinutes = 90,
                waterVolumeLiters = 14200,
                method = "Solar Drip System",
                moistureBeforeAfter = "35% → 58%",
                energyConsumedKwh = 3.2
            ),
            IrrigationHistoryEvent(
                id = "irr_hist_2",
                date = "12 Aug 2026, 6:30 PM",
                fieldName = "Field 1 (Main Soybean)",
                crop = "Soybean",
                durationMinutes = 110,
                waterVolumeLiters = 17500,
                method = "Sprinkler Micro-jet",
                moistureBeforeAfter = "40% → 64%",
                energyConsumedKwh = 4.1
            ),
            IrrigationHistoryEvent(
                id = "irr_hist_3",
                date = "08 Aug 2026, 7:00 PM",
                fieldName = "Field 2 (South Cotton Block)",
                crop = "Cotton",
                durationMinutes = 80,
                waterVolumeLiters = 12800,
                method = "Solar Drip System",
                moistureBeforeAfter = "38% → 60%",
                energyConsumedKwh = 2.8
            )
        )
    )

    override fun getIrrigationState(): Flow<SmartIrrigationFieldState> = _fieldState.asStateFlow()

    override fun getIrrigationHistory(): Flow<List<IrrigationHistoryEvent>> = _history.asStateFlow()

    override suspend fun toggleIrrigationValve(open: Boolean): Result<Boolean> {
        delay(300) // backend valve actuation latency
        _fieldState.update { it.copy(isValveOpen = open) }
        return Result.success(open)
    }

    override suspend fun refreshIrrigationAdvisory(): Result<SmartIrrigationFieldState> {
        delay(400)
        return Result.success(_fieldState.value)
    }
}

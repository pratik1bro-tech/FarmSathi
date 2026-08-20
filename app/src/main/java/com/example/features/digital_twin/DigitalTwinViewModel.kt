package com.example.features.digital_twin

import androidx.lifecycle.ViewModel
import com.example.data.models.FarmFieldParcel
import com.example.data.models.FieldHealthStatus
import com.example.data.models.WhatIfScenario
import com.example.data.models.WhatIfSimulationResult
import com.example.data.service.WhatIfSimulationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DigitalTwinUiState(
    val fields: List<FarmFieldParcel> = listOf(
        FarmFieldParcel(
            fieldId = "FIELD_01",
            fieldName = "Block A - Soybean Main",
            areaAcres = 4.5,
            cropName = "Soybean (JS 20-34)",
            growthStage = "Pod Formation Stage",
            healthStatus = FieldHealthStatus.HEALTHY,
            healthScore = 88,
            soilMoisturePct = 52.0,
            diseaseRisk = "LOW (8%)",
            sensorStatus = "Active (3 Node ESP32 Mesh)",
            ndviIndex = 0.78,
            temperatureC = 28.5
        ),
        FarmFieldParcel(
            fieldId = "FIELD_02",
            fieldName = "Block B - South Cotton",
            areaAcres = 3.2,
            cropName = "Cotton (BT-RCH-659)",
            growthStage = "Flowering & Boll Stage",
            healthStatus = FieldHealthStatus.WARNING,
            healthScore = 72,
            soilMoisturePct = 38.0,
            diseaseRisk = "MODERATE (28%)",
            sensorStatus = "Active (2 Node ESP32 Mesh)",
            ndviIndex = 0.64,
            temperatureC = 31.2
        ),
        FarmFieldParcel(
            fieldId = "FIELD_03",
            fieldName = "Block C - North Wheat/Gram",
            areaAcres = 2.8,
            cropName = "Gram / Chickpea (JG-14)",
            growthStage = "Vegetative Stage",
            healthStatus = FieldHealthStatus.CRITICAL,
            healthScore = 54,
            soilMoisturePct = 26.0,
            diseaseRisk = "HIGH (42%)",
            sensorStatus = "Low Battery Sensor Node",
            ndviIndex = 0.48,
            temperatureC = 33.0
        )
    ),
    val selectedField: FarmFieldParcel = FarmFieldParcel(
        fieldId = "FIELD_01",
        fieldName = "Block A - Soybean Main",
        areaAcres = 4.5,
        cropName = "Soybean (JS 20-34)",
        growthStage = "Pod Formation Stage",
        healthStatus = FieldHealthStatus.HEALTHY,
        healthScore = 88,
        soilMoisturePct = 52.0,
        diseaseRisk = "LOW (8%)",
        sensorStatus = "Active (3 Node ESP32 Mesh)",
        ndviIndex = 0.78,
        temperatureC = 28.5
    ),
    val selectedScenario: WhatIfScenario = WhatIfScenario.DELAY_IRRIGATION_3_DAYS,
    val simulationResult: WhatIfSimulationResult? = null
)

class DigitalTwinViewModel(
    private val simulationEngine: WhatIfSimulationEngine = WhatIfSimulationEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DigitalTwinUiState())
    val uiState: StateFlow<DigitalTwinUiState> = _uiState.asStateFlow()

    init {
        runSimulation(_uiState.value.selectedField, _uiState.value.selectedScenario)
    }

    fun selectField(field: FarmFieldParcel) {
        _uiState.update { it.copy(selectedField = field) }
        runSimulation(field, _uiState.value.selectedScenario)
    }

    fun selectScenario(scenario: WhatIfScenario) {
        _uiState.update { it.copy(selectedScenario = scenario) }
        runSimulation(_uiState.value.selectedField, scenario)
    }

    private fun runSimulation(field: FarmFieldParcel, scenario: WhatIfScenario) {
        val result = simulationEngine.simulateScenario(field, scenario)
        _uiState.update { it.copy(simulationResult = result) }
    }
}

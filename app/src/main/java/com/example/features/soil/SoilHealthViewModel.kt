package com.example.features.soil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.FieldSoilReport
import com.example.data.models.SoilHistoryDataPoint
import com.example.data.repository.DevelopmentSoilHealthRepository
import com.example.data.repository.SoilHealthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SoilHealthScreenUiState(
    val fields: List<FieldSoilReport> = emptyList(),
    val selectedFieldId: String = "field_2",
    val selectedNutrientId: String = "phosphorus",
    val selectedTimeRange: String = "4 Months",
    val historicalData: List<SoilHistoryDataPoint> = emptyList(),
    val isLoadingHistory: Boolean = false
) {
    val currentReport: FieldSoilReport?
        get() = fields.find { it.fieldId == selectedFieldId } ?: fields.firstOrNull()
}

class SoilHealthViewModel(
    private val repository: SoilHealthRepository = DevelopmentSoilHealthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoilHealthScreenUiState())
    val uiState: StateFlow<SoilHealthScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllFieldSoilReports().collect { reports ->
                _uiState.update { it.copy(fields = reports) }
                loadHistory(_uiState.value.selectedFieldId, _uiState.value.selectedNutrientId, _uiState.value.selectedTimeRange)
            }
        }
    }

    fun selectField(fieldId: String) {
        _uiState.update { it.copy(selectedFieldId = fieldId) }
        loadHistory(fieldId, _uiState.value.selectedNutrientId, _uiState.value.selectedTimeRange)
    }

    fun selectNutrient(nutrientId: String) {
        _uiState.update { it.copy(selectedNutrientId = nutrientId) }
        loadHistory(_uiState.value.selectedFieldId, nutrientId, _uiState.value.selectedTimeRange)
    }

    fun selectTimeRange(timeRange: String) {
        _uiState.update { it.copy(selectedTimeRange = timeRange) }
        loadHistory(_uiState.value.selectedFieldId, _uiState.value.selectedNutrientId, timeRange)
    }

    private fun loadHistory(fieldId: String, nutrientId: String, timeRange: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHistory = true) }
            val points = repository.getHistoricalSoilData(fieldId, nutrientId, timeRange)
            _uiState.update {
                it.copy(
                    historicalData = points,
                    isLoadingHistory = false
                )
            }
        }
    }
}

package com.example.features.telemetry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.*
import com.example.data.repository.DevelopmentTelemetryRepository
import com.example.data.repository.TelemetryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TelemetryScreenUiState(
    val telemetry: Esp32NodeTelemetry = Esp32NodeTelemetry(),
    val selectedMetricId: String = "soil_moisture",
    val selectedTimeRange: TelemetryTimeRange = TelemetryTimeRange.HOURS_24,
    val historicalData: List<TelemetryChartDataPoint> = emptyList(),
    val isRefreshing: Boolean = false
)

class TelemetryViewModel(
    private val repository: TelemetryRepository = DevelopmentTelemetryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TelemetryScreenUiState())
    val uiState: StateFlow<TelemetryScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getTelemetryStream().collect { liveData ->
                _uiState.update { it.copy(telemetry = liveData) }
                loadHistoricalData(_uiState.value.selectedMetricId, _uiState.value.selectedTimeRange)
            }
        }
    }

    fun selectMetric(metricId: String) {
        _uiState.update { it.copy(selectedMetricId = metricId) }
        loadHistoricalData(metricId, _uiState.value.selectedTimeRange)
    }

    fun selectTimeRange(range: TelemetryTimeRange) {
        _uiState.update { it.copy(selectedTimeRange = range) }
        loadHistoricalData(_uiState.value.selectedMetricId, range)
    }

    fun setConnectionMode(state: DataConnectionState) {
        repository.setConnectionState(state)
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            repository.refreshTelemetry()
            loadHistoricalData(_uiState.value.selectedMetricId, _uiState.value.selectedTimeRange)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun loadHistoricalData(metricId: String, timeRange: TelemetryTimeRange) {
        viewModelScope.launch {
            val points = repository.getHistoricalTelemetry(metricId, timeRange)
            _uiState.update { it.copy(historicalData = points) }
        }
    }
}

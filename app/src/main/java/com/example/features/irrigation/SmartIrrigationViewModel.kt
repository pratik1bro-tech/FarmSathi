package com.example.features.irrigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.IrrigationHistoryEvent
import com.example.data.models.SmartIrrigationFieldState
import com.example.data.repository.DevelopmentSmartIrrigationRepository
import com.example.data.repository.SmartIrrigationRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SmartIrrigationScreenUiState(
    val fieldState: SmartIrrigationFieldState = SmartIrrigationFieldState(),
    val history: List<IrrigationHistoryEvent> = emptyList(),
    val isActuatingValve: Boolean = false,
    val isRefreshing: Boolean = false
)

class SmartIrrigationViewModel(
    private val repository: SmartIrrigationRepository = DevelopmentSmartIrrigationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartIrrigationScreenUiState())
    val uiState: StateFlow<SmartIrrigationScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getIrrigationState().collect { state ->
                _uiState.update { it.copy(fieldState = state) }
            }
        }
        viewModelScope.launch {
            repository.getIrrigationHistory().collect { historyList ->
                _uiState.update { it.copy(history = historyList) }
            }
        }
    }

    fun toggleValve(open: Boolean) {
        _uiState.update { it.copy(isActuatingValve = true) }
        viewModelScope.launch {
            repository.toggleIrrigationValve(open)
            _uiState.update { it.copy(isActuatingValve = false) }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            repository.refreshIrrigationAdvisory()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}

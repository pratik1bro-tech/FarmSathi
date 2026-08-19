package com.example.features.farm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.CropField
import com.example.data.models.FarmerProfile
import com.example.data.repository.FarmRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FarmUiState(
    val fields: List<CropField> = emptyList(),
    val profile: FarmerProfile = FarmerProfile(),
    val totalAcres: Double = 5.2,
    val averageHealth: Int = 91,
    val showAddFieldDialog: Boolean = false
)

class FarmViewModel(
    private val repository: FarmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmUiState())
    val uiState: StateFlow<FarmUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getCropFields(),
                repository.getFarmerProfile()
            ) { fields, profile ->
                val avg = if (fields.isNotEmpty()) fields.map { it.healthScore }.average().toInt() else 0
                val total = fields.sumOf { it.areaAcres }
                FarmUiState(
                    fields = fields,
                    profile = profile,
                    totalAcres = total,
                    averageHealth = avg
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setAddFieldDialogVisible(show: Boolean) {
        _uiState.update { it.copy(showAddFieldDialog = show) }
    }
}

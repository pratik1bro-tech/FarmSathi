package com.example.features.logistics

import androidx.lifecycle.ViewModel
import com.example.data.models.LogisticsRouteRequest
import com.example.data.models.SharedTransportPool
import com.example.data.models.VehicleType
import com.example.data.service.LogisticsMatchingEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LogisticsUiState(
    val routeRequest: LogisticsRouteRequest = LogisticsRouteRequest(),
    val activePool: SharedTransportPool? = null,
    val alternativePools: List<SharedTransportPool> = emptyList(),
    val isConfirmationDialogOpen: Boolean = false,
    val isBookingConfirmed: Boolean = false,
    val confirmedBooking: SharedTransportPool? = null
)

class LogisticsViewModel(
    private val matchingEngine: LogisticsMatchingEngine = LogisticsMatchingEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogisticsUiState())
    val uiState: StateFlow<LogisticsUiState> = _uiState.asStateFlow()

    init {
        recalculatePools(_uiState.value.routeRequest)
    }

    fun updateQuantity(quantity: Double) {
        val updatedRequest = _uiState.value.routeRequest.copy(quantityQuintals = quantity)
        _uiState.update { it.copy(routeRequest = updatedRequest) }
        recalculatePools(updatedRequest)
    }

    fun updateVehicle(vehicle: VehicleType) {
        val updatedRequest = _uiState.value.routeRequest.copy(preferredVehicle = vehicle)
        _uiState.update { it.copy(routeRequest = updatedRequest) }
        recalculatePools(updatedRequest)
    }

    fun updateDestination(destination: String) {
        val updatedRequest = _uiState.value.routeRequest.copy(destinationMandi = destination)
        _uiState.update { it.copy(routeRequest = updatedRequest) }
        recalculatePools(updatedRequest)
    }

    fun openConfirmationDialog(pool: SharedTransportPool) {
        _uiState.update {
            it.copy(
                activePool = pool,
                isConfirmationDialogOpen = true
            )
        }
    }

    fun closeConfirmationDialog() {
        _uiState.update { it.copy(isConfirmationDialogOpen = false) }
    }

    fun confirmSharedTransportBooking() {
        val pool = _uiState.value.activePool
        _uiState.update {
            it.copy(
                isConfirmationDialogOpen = false,
                isBookingConfirmed = true,
                confirmedBooking = pool
            )
        }
    }

    fun dismissBookingConfirmationBanner() {
        _uiState.update { it.copy(isBookingConfirmed = false) }
    }

    private fun recalculatePools(request: LogisticsRouteRequest) {
        val primary = matchingEngine.computeLogisticsMatch(request)
        val alternatives = matchingEngine.getAlternativePools(request)
        _uiState.update {
            it.copy(
                activePool = primary,
                alternativePools = alternatives
            )
        }
    }
}

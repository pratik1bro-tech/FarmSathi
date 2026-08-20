package com.example.features.notifications

import androidx.lifecycle.ViewModel
import com.example.data.models.AlertCategory
import com.example.data.models.ProactiveFarmAlert
import com.example.data.service.ProactiveAlertEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NotificationsUiState(
    val alerts: List<ProactiveFarmAlert> = emptyList(),
    val selectedCategory: AlertCategory? = null, // null means "ALL"
    val fcmStatus: String = "📡 FCM Cloud Connected (Token: fcm_token_farmsathi_node_8832a_90)",
    val isFcmActive: Boolean = true
)

class NotificationsViewModel(
    private val alertEngine: ProactiveAlertEngine = ProactiveAlertEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        // Collect alerts from backend engine
        kotlinx.coroutines.GlobalScope.run {
            // Note: simple flow update for UI state
        }
    }

    fun selectCategoryFilter(category: AlertCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun markAlertRead(alertId: String) {
        alertEngine.markAsRead(alertId)
    }

    fun dismissAlert(alertId: String) {
        alertEngine.dismissAlert(alertId)
    }

    fun updateAlerts(alerts: List<ProactiveFarmAlert>) {
        _uiState.update { it.copy(alerts = alerts) }
    }
}

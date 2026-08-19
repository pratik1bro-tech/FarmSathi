package com.example.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.*
import com.example.data.repository.FarmRepository
import com.example.designsystem.theme.FarmSemanticStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class FarmTaskItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val isCompleted: Boolean = false,
    val actionRoute: String? = null
)

data class FarmAiAlertItem(
    val id: String,
    val icon: String,
    val text: String,
    val status: FarmSemanticStatus,
    val targetRoute: String
)

data class HomeUiState(
    val farmerProfile: FarmerProfile = FarmerProfile(),
    val cropFields: List<CropField> = emptyList(),
    val telemetry: TelemetryData = TelemetryData(),
    val soilHealth: SoilHealthReport = SoilHealthReport(),
    val weather: WeatherForecast = WeatherForecast(),
    val mandiPrices: List<MandiPriceItem> = emptyList(),
    val selectedFarm: String = "Patel Krishi Farm Block A • Indore, MP",
    val availableFarms: List<String> = listOf(
        "Patel Krishi Farm Block A • Indore, MP",
        "South Farm Block B • Khargone, MP",
        "East Polyhouse Plot • Sanwer, MP"
    ),
    val farmHealthScore: Int = 82,
    val farmHealthStatus: FarmSemanticStatus = FarmSemanticStatus.Healthy,
    val aiUpdates: List<FarmAiAlertItem> = listOf(
        FarmAiAlertItem(
            id = "ai_1",
            icon = "💧",
            text = "Soil moisture is low in Field 2 (32% moisture; drip valve ready).",
            status = FarmSemanticStatus.Warning,
            targetRoute = "smart_irrigation"
        ),
        FarmAiAlertItem(
            id = "ai_2",
            icon = "🛡️",
            text = "Disease risk is increasing (Fall Armyworm reported in nearby cluster).",
            status = FarmSemanticStatus.Critical,
            targetRoute = "disease_detection"
        ),
        FarmAiAlertItem(
            id = "ai_3",
            icon = "📈",
            text = "Soybean prices are rising (+₹230/Q in Indore Mandi; target ₹5,150).",
            status = FarmSemanticStatus.Healthy,
            targetRoute = "market"
        )
    ),
    val farmTasks: List<FarmTaskItem> = listOf(
        FarmTaskItem(
            id = "t_1",
            title = "1. Irrigate Field 2",
            subtitle = "South Cotton drip line needs 45-min moisture top-up",
            category = "Irrigation",
            isCompleted = false,
            actionRoute = "smart_irrigation"
        ),
        FarmTaskItem(
            id = "t_2",
            title = "2. Inspect soybean leaves",
            subtitle = "Verify early yellow mosaic control after Neem oil spray",
            category = "Crop Health",
            isCompleted = false,
            actionRoute = "disease_detection"
        ),
        FarmTaskItem(
            id = "t_3",
            title = "3. Monitor soybean prices",
            subtitle = "Indore Mandi rate at ₹4,850/Q; AI recommendation: HOLD",
            category = "Market",
            isCompleted = true,
            actionRoute = "market"
        )
    ),
    val unreadNotificationsCount: Int = 3,
    val isRefreshing: Boolean = false,
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val repository: FarmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                combine(
                    repository.getFarmerProfile(),
                    repository.getCropFields(),
                    repository.getTelemetryData(),
                    repository.getSoilHealthReport(),
                    repository.getWeatherForecast()
                ) { profile, fields, telemetry, soil, weather ->
                    val mandi = repository.getMandiPrices().first()
                    _uiState.update { current ->
                        current.copy(
                            farmerProfile = profile,
                            cropFields = fields,
                            telemetry = telemetry,
                            soilHealth = soil,
                            weather = weather,
                            mandiPrices = mandi,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null
                        )
                    }
                }.collect()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = "Unable to sync live farm telemetry: ${e.localizedMessage ?: "Network error"}"
                    )
                }
            }
        }
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            delay(800)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun selectFarm(farm: String) {
        _uiState.update { it.copy(selectedFarm = farm) }
    }

    fun toggleTask(taskId: String) {
        _uiState.update { state ->
            val updatedTasks = state.farmTasks.map { task ->
                if (task.id == taskId) task.copy(isCompleted = !task.isCompleted) else task
            }
            state.copy(farmTasks = updatedTasks)
        }
    }

    fun toggleOfflineSimulation() {
        _uiState.update { it.copy(isOffline = !it.isOffline) }
    }

    fun getGreetingTimeText(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }
    }
}

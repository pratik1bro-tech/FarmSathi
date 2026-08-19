package com.example.features.market

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.voice.AndroidTextToSpeechService
import com.example.core.voice.VoiceLanguage
import com.example.data.models.*
import com.example.data.repository.FarmRepository
import com.example.data.service.MarketDataService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MarketViewModel(
    application: Application,
    private val repository: FarmRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MarketIntelligenceState())
    val uiState: StateFlow<MarketIntelligenceState> = _uiState.asStateFlow()

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val ttsService by lazy { AndroidTextToSpeechService(application.applicationContext) }

    init {
        loadInitialMarketData()
    }

    fun loadInitialMarketData() {
        val crops = MarketDataService.getAvailableCrops()
        val defaultCrop = crops.firstOrNull() ?: CropMarketItem("soybean", "Soybean (Yellow JS-2034)", "सोयाबीन", "High Oil JS-2034", isPrimaryCrop = true)
        
        _uiState.update { 
            it.copy(
                availableCrops = crops,
                selectedCrop = defaultCrop
            ) 
        }
        refreshMarketData(cropId = defaultCrop.id, period = _uiState.value.selectedTimePeriod)
    }

    fun selectCrop(crop: CropMarketItem) {
        _uiState.update { it.copy(selectedCrop = crop) }
        refreshMarketData(cropId = crop.id, period = _uiState.value.selectedTimePeriod)
    }

    fun selectTimePeriod(period: MarketTimePeriod) {
        _uiState.update { it.copy(selectedTimePeriod = period) }
        val cropId = _uiState.value.selectedCrop.id
        val (points, stats) = MarketDataService.generateHistoricalTrend(cropId, period)
        _uiState.update {
            it.copy(
                historicalPoints = points,
                periodStats = stats
            )
        }
    }

    fun refreshMarketData(cropId: String = _uiState.value.selectedCrop.id, period: MarketTimePeriod = _uiState.value.selectedTimePeriod, isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isPullToRefresh,
                    isRefreshing = isPullToRefresh,
                    errorMessage = null
                )
            }

            if (isPullToRefresh) {
                delay(600) // Realistic APMC Agmarknet gateway latency
            }

            try {
                val primaryQuote = MarketDataService.fetchPrimaryMandiPrice(cropId)
                val nearbyQuotes = MarketDataService.fetchNearbyMandis(cropId)
                val (points, stats) = MarketDataService.generateHistoricalTrend(cropId, period)
                val forecast = MarketDataService.computeAiForecast(cropId)
                val syncTime = "Today, ${timeFormat.format(Date())}"

                _uiState.update {
                    it.copy(
                        currentMandiPrice = primaryQuote,
                        nearbyMandiQuotes = nearbyQuotes,
                        historicalPoints = points,
                        periodStats = stats,
                        aiForecast = forecast,
                        lastSyncTimestamp = syncTime,
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = "Failed to sync mandi quotes. Please retry."
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleOffline(isOffline: Boolean) {
        _uiState.update {
            it.copy(
                isOffline = isOffline,
                lastSyncTimestamp = if (isOffline) "Cached (Offline)" else "Today, ${timeFormat.format(Date())}"
            )
        }
        if (!isOffline) {
            refreshMarketData(isPullToRefresh = true)
        }
    }

    fun speakForecast(isHindi: Boolean) {
        val forecast = _uiState.value.aiForecast
        val text = if (isHindi) {
            "${forecast.cropName} के लिए मंडी पूर्वानुमान: ${forecast.recommendationSummaryHi}. ध्यान दें, यह AI अनुमान है और कीमतें गारंटीकृत नहीं हैं।"
        } else {
            "Mandi Intelligence Forecast for ${forecast.cropName}: ${forecast.recommendationSummary}. Note that these are probabilistic projections and not guaranteed prices."
        }

        val lang = if (isHindi) VoiceLanguage.HINDI else VoiceLanguage.ENGLISH
        ttsService.speak(text = text, language = lang)
    }

    fun stopSpeaking() {
        ttsService.stop()
    }

    override fun onCleared() {
        super.onCleared()
        ttsService.release()
    }
}

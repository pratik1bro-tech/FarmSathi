package com.example.features.weather

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.voice.AndroidTextToSpeechService
import com.example.core.voice.VoiceLanguage
import com.example.data.models.*
import com.example.data.repository.FarmRepository
import com.example.data.service.WeatherImpactEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WeatherViewModel(
    application: Application,
    private val repository: FarmRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(WeatherIntelligenceUiState())
    val uiState: StateFlow<WeatherIntelligenceUiState> = _uiState.asStateFlow()

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val ttsService by lazy { AndroidTextToSpeechService(application.applicationContext) }

    init {
        loadWeatherData()
        observeFarmerProfile()
    }

    private fun observeFarmerProfile() {
        viewModelScope.launch {
            repository.getFarmerProfile().collect { profile ->
                // Update selected zone according to farmer profile village
                val isHindi = profile.selectedLanguage == "hi"
                updateAdvisoryText(isHindi)
            }
        }
    }

    fun loadWeatherData(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = !isPullToRefresh,
                    isRefreshing = isPullToRefresh,
                    errorMessage = null
                ) 
            }

            try {
                if (isPullToRefresh) {
                    delay(700) // Realistic sensor/satellite sync latency
                }

                // If currently simulated offline, load cached snapshot
                if (_uiState.value.isOffline) {
                    val current = _uiState.value
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isCachedData = true,
                            lastUpdatedText = "Cached (Offline Mode)"
                        )
                    }
                    return@launch
                }

                // Generate full meteorology dataset and compute agronomic impact
                val currentTemp = 31.0
                val feelsLike = 34.5
                val humidity = 78
                val rainProb = 85
                val rainfallMm = 24.5
                val windSpeed = 18.2

                val todayHourly = WeatherImpactEngine.generateTodayHourly()
                val tomorrowHourly = WeatherImpactEngine.generateTomorrowHourly()
                val sevenDay = WeatherImpactEngine.generateSevenDayForecast()

                val todayImpacts = WeatherImpactEngine.evaluateTodayImpacts(
                    tempC = currentTemp,
                    humidityPercent = humidity,
                    rainProbPercent = rainProb,
                    rainfallMm = rainfallMm,
                    windSpeedKmh = windSpeed
                )

                val tomorrowImpacts = WeatherImpactEngine.evaluateTomorrowImpacts()
                val weeklyImpacts = WeatherImpactEngine.evaluateWeeklyImpacts()

                val matrix = WeatherImpactEngine.evaluateOperationsMatrix(
                    tempC = currentTemp,
                    humidityPercent = humidity,
                    rainProbPercent = rainProb,
                    rainfallMm = rainfallMm,
                    windSpeedKmh = windSpeed
                )

                val currentTimeStr = "Today, ${timeFormat.format(Date())}"

                _uiState.update {
                    val updated = it.copy(
                        currentTempC = currentTemp,
                        feelsLikeC = feelsLike,
                        tempMaxTodayC = 33,
                        tempMinTodayC = 22,
                        humidityPercent = humidity,
                        rainProbabilityPercent = rainProb,
                        rainfallExpectedMm = rainfallMm,
                        windSpeedKmh = windSpeed,
                        windDirection = "SW (South-West)",
                        windGustKmh = 26.5,
                        weatherCondition = "Heavy Monsoon Rain & High Humidity Expected",
                        conditionType = WeatherConditionType.HEAVY_RAIN,
                        dewPointC = 24.0,
                        uvIndex = 6,
                        airPressureHpa = 1008.2,
                        solarRadiationLux = 42000,
                        cloudCoverPercent = 85,
                        todayHourlyForecast = todayHourly,
                        tomorrowHourlyForecast = tomorrowHourly,
                        sevenDayForecast = sevenDay,
                        todayImpacts = todayImpacts,
                        tomorrowImpacts = tomorrowImpacts,
                        weeklyImpacts = weeklyImpacts,
                        operationsMatrix = matrix,
                        lastUpdatedText = currentTimeStr,
                        stationName = "IMD Indore Agro-Met & On-Farm ESP32 Node",
                        isLoading = false,
                        isRefreshing = false,
                        isCachedData = false,
                        errorMessage = null
                    )
                    val (enSummary, hiSummary) = WeatherImpactEngine.generateAdvisorySummary(updated)
                    updated.copy(
                        aiAdvisorySummaryEn = enSummary,
                        aiAdvisorySummaryHi = hiSummary
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = "Unable to connect to IMD weather server. Please check connection."
                    )
                }
            }
        }
    }

    fun selectTab(tab: WeatherViewTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectFarmZone(zone: String) {
        _uiState.update { current ->
            // Slight microclimate variation based on farm zone
            when {
                zone.contains("Polyhouse") -> {
                    current.copy(
                        selectedFarmZone = zone,
                        currentTempC = 28.5,
                        humidityPercent = 84,
                        windSpeedKmh = 4.2,
                        weatherCondition = "Protected Microclimate • High Humidity",
                        conditionType = WeatherConditionType.MIST_FOG
                    )
                }
                zone.contains("Cotton") -> {
                    current.copy(
                        selectedFarmZone = zone,
                        currentTempC = 31.8,
                        humidityPercent = 75,
                        windSpeedKmh = 19.5,
                        weatherCondition = "Breezy & Heavy Downpour Expected",
                        conditionType = WeatherConditionType.HEAVY_RAIN
                    )
                }
                else -> {
                    current.copy(
                        selectedFarmZone = zone,
                        currentTempC = 31.0,
                        humidityPercent = 78,
                        windSpeedKmh = 18.2,
                        weatherCondition = "Heavy Monsoon Rain & High Humidity",
                        conditionType = WeatherConditionType.HEAVY_RAIN
                    )
                }
            }
        }
    }

    fun toggleOfflineMode(forceOffline: Boolean) {
        _uiState.update {
            it.copy(
                isOffline = forceOffline,
                isCachedData = forceOffline,
                lastUpdatedText = if (forceOffline) "Cached (${timeFormat.format(Date())})" else "Today, ${timeFormat.format(Date())}"
            )
        }
        if (!forceOffline) {
            loadWeatherData(isPullToRefresh = true)
        }
    }

    fun generateAiAgronomicAdvisory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingAiAdvisory = true) }
            delay(1000) // AI Agronomic synthesis computation

            val current = _uiState.value
            val aiEn = "AI Agronomic Synthesis for ${current.selectedFarmZone}: Heavy precipitation (${current.rainfallExpectedMm}mm) over the next 24-48h will cause critical nitrate leaching if fertilizers are applied now. Postpone all Urea top-dressing until Saturday. High ambient humidity (${current.humidityPercent}%) elevates fungal spore germination risk on Soybean (JS-2034) pods and Cotton squares. Prepare prophylactic bio-fungicide (Trichoderma viride @ 5g/L) for the upcoming dry Saturday window. Hold drip irrigation to prevent root rot."
            
            val aiHi = "${current.selectedFarmZone} के लिए AI कृषि विश्लेषण: अगले 24-48 घंटों में भारी वर्षा (${current.rainfallExpectedMm} मिमी) के कारण यूरिया या डीएपी खाद बहकर नष्ट हो सकती है, इसलिए शनिवार तक खाद का प्रयोग पूरी तरह टालें। हवा में अधिक नमी (${current.humidityPercent}%) से सोयाबीन और कपास में फफूंद व पत्ती धब्बा रोग का खतरा है। शनिवार की सुबह ट्राइकोडर्मा (5 ग्राम/लीटर) के सुरक्षात्मक स्प्रे की तैयारी रखें। जलभराव से बचने हेतु सिंचाई रोकें।"

            _uiState.update {
                it.copy(
                    isGeneratingAiAdvisory = false,
                    aiAdvisorySummaryEn = aiEn,
                    aiAdvisorySummaryHi = aiHi
                )
            }
        }
    }

    fun speakAdvisory(isHindi: Boolean) {
        val textToSpeak = if (isHindi) {
            _uiState.value.aiAdvisorySummaryHi.ifEmpty { _uiState.value.aiAdvisorySummaryEn }
        } else {
            _uiState.value.aiAdvisorySummaryEn.ifEmpty { _uiState.value.aiAdvisorySummaryHi }
        }

        if (textToSpeak.isBlank()) return

        _uiState.update { it.copy(isSpeakingAdvisory = true) }
        val lang = if (isHindi) VoiceLanguage.HINDI else VoiceLanguage.ENGLISH

        ttsService.speak(
            text = textToSpeak,
            language = lang,
            onStart = {
                _uiState.update { it.copy(isSpeakingAdvisory = true) }
            },
            onDone = {
                _uiState.update { it.copy(isSpeakingAdvisory = false) }
            },
            onError = {
                _uiState.update { it.copy(isSpeakingAdvisory = false) }
            }
        )
    }

    fun stopSpeaking() {
        ttsService.stop()
        _uiState.update { it.copy(isSpeakingAdvisory = false) }
    }

    private fun updateAdvisoryText(isHindi: Boolean) {
        val (en, hi) = WeatherImpactEngine.generateAdvisorySummary(_uiState.value)
        _uiState.update {
            it.copy(
                aiAdvisorySummaryEn = it.aiAdvisorySummaryEn.ifBlank { en },
                aiAdvisorySummaryHi = it.aiAdvisorySummaryHi.ifBlank { hi }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsService.release()
    }
}

package com.example.data.models

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

/**
 * Weather condition classification with visual styling and agricultural implications.
 */
enum class WeatherConditionType(
    val titleEn: String,
    val titleHi: String,
    val iconName: String,
    val isRainy: Boolean = false,
    val isHot: Boolean = false,
    val isHumid: Boolean = false,
    val isWindy: Boolean = false
) {
    SUNNY("Sunny & Clear", "धूप और साफ", "sunny"),
    HOT_DRY("Hot & Dry Wave", "गर्म और शुष्क", "hot", isHot = true),
    PARTLY_CLOUDY("Partly Cloudy", "आंशिक रूप से बादल", "partly_cloudy"),
    CLOUDY("Overcast Clouds", "बादल छाए रहेंगे", "cloudy"),
    LIGHT_RAIN("Light Showers", "हल्की बारिश", "light_rain", isRainy = true),
    HEAVY_RAIN("Heavy Downpour", "भारी बारिश", "heavy_rain", isRainy = true, isHumid = true),
    THUNDERSTORM("Thunderstorms & Lightning", "आंधी और गरज", "thunderstorm", isRainy = true, isWindy = true),
    WINDY("Strong Gusty Winds", "तेज हवाएं", "windy", isWindy = true),
    MIST_FOG("Dense Fog & Dew", "कोहरा और ओस", "fog", isHumid = true)
}

/**
 * Tabs for Weather Intelligence navigation.
 */
enum class WeatherViewTab(val labelEn: String, val labelHi: String) {
    TODAY("Today", "आज"),
    TOMORROW("Tomorrow", "कल"),
    SEVEN_DAYS("7 Days", "7 दिन")
}

/**
 * Severity level for Agricultural Impact advisories.
 */
enum class ImpactSeverity(val labelEn: String, val labelHi: String) {
    CRITICAL("Action Required", "कार्रवाई आवश्यक"),
    WARNING("High Risk Alert", "सतर्कता चेतावनी"),
    ADVISORY("Agronomic Advisory", "कृषि परामर्श"),
    FAVORABLE("Optimal Window", "अनुकूल समय")
}

/**
 * Category of agricultural field operation affected by weather.
 */
enum class AgriImpactCategory(val titleEn: String, val titleHi: String) {
    FERTILIZER("Fertilizer & Nutrition", "उर्वरक एवं पोषण"),
    DISEASE_RISK("Disease & Pest Vulnerability", "रोग एवं कीट जोखिम"),
    IRRIGATION("Irrigation & Water Demand", "सिंचाई एवं जल मांग"),
    SPRAYING("Pesticide Spray Window", "कीटनाशक स्प्रे समय"),
    HARVEST_STORAGE("Harvest & Post-Harvest", "कटाई एवं भंडारण"),
    HEAT_STRESS("Canopy & Heat Stress", "गर्मी एवं फसल सुरक्षा")
}

/**
 * Feasibility rating for field operations.
 */
enum class AgriActivityStatus(val labelEn: String, val labelHi: String, val isAllowed: Boolean) {
    OPTIMAL("Optimal Window", "उत्तम समय", true),
    MARGINAL("Proceed With Caution", "सावधानीपूर्वक करें", true),
    AVOID("Avoid / Postpone", "बचें / टालें", false),
    RESTRICTED("Restricted", "प्रतिबंधित", false)
}

/**
 * Hourly weather data point for Today and Tomorrow forecasts.
 */
data class HourlyWeatherForecast(
    val timeLabel: String,
    val tempC: Double,
    val feelsLikeC: Double,
    val humidityPercent: Int,
    val rainProbabilityPercent: Int,
    val rainfallExpectedMm: Double,
    val windSpeedKmh: Double,
    val condition: String,
    val conditionType: WeatherConditionType,
    val isFavorableForSpraying: Boolean,
    val sprayStatusReason: String
)

/**
 * Detailed 7-day daily weather forecast item.
 */
data class DailyWeatherForecast(
    val dayLabel: String, // "Today", "Tomorrow", "Fri", "Sat", etc.
    val dateLabel: String, // "19 Aug", "20 Aug", etc.
    val tempMaxC: Int,
    val tempMinC: Int,
    val condition: String,
    val conditionType: WeatherConditionType,
    val rainProbabilityPercent: Int,
    val rainfallExpectedMm: Double,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val windDirection: String,
    val uvIndex: Int,
    val primaryAgriImpact: String,
    val primaryAgriImpactHi: String,
    val fertilizerStatus: AgriActivityStatus,
    val sprayingStatus: AgriActivityStatus,
    val irrigationStatus: AgriActivityStatus,
    val harvestingStatus: AgriActivityStatus,
    val farmSuitabilityScore: Int // 0 - 100%
)

/**
 * Specific Agriculture Impact recommendation item backed by agronomic science.
 */
data class AgricultureImpactItem(
    val id: String,
    val category: AgriImpactCategory,
    val title: String,
    val titleHi: String,
    val weatherTrigger: String,
    val weatherTriggerHi: String,
    val scientificRationale: String,
    val scientificRationaleHi: String,
    val farmerAction: String,
    val farmerActionHi: String,
    val severity: ImpactSeverity,
    val affectedCrops: List<String>,
    val timingRecommendation: String,
    val timingRecommendationHi: String
)

/**
 * Farm operations feasibility summary for a given day.
 */
data class FarmOperationsMatrix(
    val sprayingSuitability: AgriActivityStatus,
    val sprayingTiming: String,
    val fertilizerSuitability: AgriActivityStatus,
    val fertilizerTiming: String,
    val irrigationSuitability: AgriActivityStatus,
    val irrigationTiming: String,
    val harvestingSuitability: AgriActivityStatus,
    val harvestingTiming: String
)

/**
 * Complete UI State for Weather Intelligence screen.
 */
data class WeatherIntelligenceUiState(
    val selectedTab: WeatherViewTab = WeatherViewTab.TODAY,
    val selectedFarmZone: String = "Zone 1: North Soybean (2.5 Acres)",
    val availableFarmZones: List<String> = listOf(
        "Zone 1: North Soybean (2.5 Acres)",
        "Zone 2: South Cotton (1.8 Acres)",
        "Zone 3: East Tomato Polyhouse (0.9 Acres)"
    ),
    // Current live metrics
    val currentTempC: Double = 31.0,
    val feelsLikeC: Double = 34.5,
    val tempMaxTodayC: Int = 33,
    val tempMinTodayC: Int = 22,
    val humidityPercent: Int = 78,
    val rainProbabilityPercent: Int = 85,
    val rainfallExpectedMm: Double = 24.5,
    val windSpeedKmh: Double = 18.2,
    val windDirection: String = "SW (South-West)",
    val windGustKmh: Double = 26.5,
    val weatherCondition: String = "Heavy Monsoon Rain & High Humidity Expected",
    val conditionType: WeatherConditionType = WeatherConditionType.HEAVY_RAIN,
    val dewPointC: Double = 24.0,
    val uvIndex: Int = 6,
    val airPressureHpa: Double = 1008.2,
    val solarRadiationLux: Int = 42000,
    val cloudCoverPercent: Int = 85,
    
    // Forecast datasets
    val todayHourlyForecast: List<HourlyWeatherForecast> = emptyList(),
    val tomorrowHourlyForecast: List<HourlyWeatherForecast> = emptyList(),
    val sevenDayForecast: List<DailyWeatherForecast> = emptyList(),
    
    // Agriculture impacts
    val todayImpacts: List<AgricultureImpactItem> = emptyList(),
    val tomorrowImpacts: List<AgricultureImpactItem> = emptyList(),
    val weeklyImpacts: List<AgricultureImpactItem> = emptyList(),
    val operationsMatrix: FarmOperationsMatrix = FarmOperationsMatrix(
        sprayingSuitability = AgriActivityStatus.AVOID,
        sprayingTiming = "Unsafe due to wind gusts & rain wash-off",
        fertilizerSuitability = AgriActivityStatus.AVOID,
        fertilizerTiming = "Avoid top-dressing to prevent nutrient leaching",
        irrigationSuitability = AgriActivityStatus.AVOID,
        irrigationTiming = "Pause irrigation; 24mm rain incoming",
        harvestingSuitability = AgriActivityStatus.MARGINAL,
        harvestingTiming = "Move harvested produce indoors"
    ),
    
    // AI Synthesis & Voice
    val aiAdvisorySummaryEn: String = "",
    val aiAdvisorySummaryHi: String = "",
    val isGeneratingAiAdvisory: Boolean = false,
    val isSpeakingAdvisory: Boolean = false,
    
    // Metadata & Error/Offline states
    val lastUpdatedText: String = "Today, 10:32 AM",
    val stationName: String = "IMD Indore Agro-Met & On-Farm ESP32 Node",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val isCachedData: Boolean = false,
    val errorMessage: String? = null
)

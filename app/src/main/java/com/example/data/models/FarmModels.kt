package com.example.data.models

// 1. Farmer Profile
data class FarmerProfile(
    val id: String = "kisan_9082",
    val name: String = "Rameshwar Patel",
    val phone: String = "+91 98260 44123",
    val village: String = "Sanwer, Indore",
    val state: String = "Madhya Pradesh",
    val landSizeAcres: Double = 5.2,
    val soilType: String = "Black Clay Loam (Regur)",
    val kisanCreditCardLinked: Boolean = true,
    val selectedLanguage: String = "hi",
    val preferredMandi: String = "Indore APMC Yard",
    val activeSensorsCount: Int = 4
)

// 2. Crop & Field Models
data class CropField(
    val id: String,
    val name: String,
    val cropName: String,
    val variety: String,
    val areaAcres: Double,
    val sowingDate: String,
    val expectedHarvestDate: String,
    val growthStage: String, // Germination, Vegetative, Flowering, Grain Filling, Ripening
    val healthScore: Int, // 0 - 100%
    val soilMoisture: Int, // %
    val nitrogenLevel: String, // Low, Optimal, High
    val statusBadge: String = "Healthy"
)

// 3. IoT Farm Telemetry
data class TelemetryData(
    val deviceId: String = "ESP32_FIELD_NODE_01",
    val lastSyncTime: String = "Just now",
    val isOnline: Boolean = true,
    val batteryPercent: Int = 92,
    val soilMoistureTop10cm: Int = 38, // %
    val soilMoistureDeep30cm: Int = 45, // %
    val soilTemperature: Double = 24.5, // °C
    val ambientTemperature: Double = 29.2, // °C
    val ambientHumidity: Int = 64, // %
    val solarRadiationLux: Int = 48200,
    val leafWetnessPercent: Int = 18,
    val solarPumpRunning: Boolean = false,
    val waterFlowRateLpm: Double = 0.0
)

// 4. Soil NPK Analysis
data class SoilHealthReport(
    val sampleDate: String = "15 Aug 2026",
    val nitrogenPpm: Double = 210.0, // Target: 240-300
    val nitrogenStatus: String = "Slightly Low",
    val phosphorusPpm: Double = 28.5, // Target: 20-35
    val phosphorusStatus: String = "Optimal",
    val potassiumPpm: Double = 280.0, // Target: 200-300
    val potassiumStatus: String = "Optimal",
    val phLevel: Double = 6.8, // Target: 6.5 - 7.5 (Neutral)
    val organicCarbonPercent: Double = 0.65, // Target > 0.75%
    val electricalConductivity: Double = 0.42, // dS/m
    val aiRecommendation: String = "Apply 25kg/acre Urea split with bio-fertilizer Azotobacter before next irrigation cycle."
)

// 5. Crop Disease Detection
data class DiseaseDiagnosis(
    val id: String,
    val cropName: String,
    val diseaseName: String,
    val confidence: Double, // 0.0 to 1.0
    val severity: String, // Low, Moderate, High, Critical
    val affectedAreaPercent: Int,
    val symptoms: List<String>,
    val organicRemedy: String,
    val chemicalTreatment: String,
    val preventiveTip: String,
    val scannedAt: String
)

// 6. Smart Irrigation
data class IrrigationZone(
    val id: String,
    val zoneName: String,
    val crop: String,
    val moistureCurrent: Int,
    val moistureTarget: Int,
    val autoIrrigationEnabled: Boolean,
    val valveOpen: Boolean,
    val nextScheduledTime: String,
    val estimatedWaterSavedLitres: Int
)

// 7. Weather Intelligence
data class WeatherForecast(
    val currentTempC: Double = 31.0,
    val feelsLikeC: Double = 34.0,
    val condition: String = "Partly Cloudy with Breeze",
    val humidityPercent: Int = 62,
    val windSpeedKmh: Double = 14.2,
    val rainfallChancePercent: Int = 20,
    val sprayAdvisoryStatus: String = "Favorable (Safe to spray pesticides before 4 PM)",
    val dailyForecasts: List<DailyWeatherItem> = emptyList()
)

data class DailyWeatherItem(
    val day: String,
    val tempMax: Int,
    val tempMin: Int,
    val condition: String,
    val rainProb: Int
)

// 8. Mandi Market Intelligence
data class MandiPriceItem(
    val cropName: String,
    val mandiName: String,
    val currentPricePerQuintal: Int,
    val yesterdayPricePerQuintal: Int,
    val minPrice: Int,
    val maxPrice: Int,
    val priceTrend: String, // "UP", "DOWN", "STABLE"
    val priceChangePercent: Double,
    val sellRecommendation: String, // "SELL NOW", "HOLD / WAIT", "STRONG BUY DEMAND"
    val forecast7Days: Int,
    val confidenceScore: Int
)

// 9. Yield Forecast
data class YieldForecastData(
    val cropName: String,
    val areaAcres: Double,
    val estimatedYieldQuintals: Double,
    val yieldRangeMin: Double,
    val yieldRangeMax: Double,
    val estimatedRevenueInr: Double,
    val harvestReadinessDays: Int,
    val keyYieldDrivers: List<String>
)

// 10. Buyer Offer
data class BuyerOffer(
    val id: String,
    val buyerName: String,
    val buyerCompany: String,
    val rating: Double,
    val verified: Boolean,
    val cropRequired: String,
    val quantityQuintals: Double,
    val offeredPricePerQuintal: Int,
    val pickupLocation: String,
    val paymentTerms: String, // e.g. "Instant via UPI upon weighbridge clearance"
    val distanceKm: Double
)

// 11. Smart Logistics & Shared Transport
data class LogisticsTrip(
    val tripId: String,
    val truckType: String, // "Tata 407 (3 Ton)", "Mahindra Bolero Pickup (1.5 Ton)"
    val driverName: String,
    val driverPhone: String,
    val departureTime: String,
    val destinationMandi: String,
    val totalCapacityTons: Double,
    val availableSpaceTons: Double,
    val costPerQuintalInr: Int,
    val pooledFarmersCount: Int,
    val routeStops: List<String>
)

// 12. Community Outbreak Radar
data class OutbreakAlert(
    val id: String,
    val diseaseName: String,
    val crop: String,
    val distanceKm: Double,
    val reportedVillage: String,
    val affectedFarmsCount: Int,
    val riskLevel: String, // "HIGH RISK", "MODERATE", "WATCH"
    val spreadVelocity: String, // "Spreading Eastward with winds"
    val recommendedAction: String
)

// 13. Farm Digital Twin
data class DigitalTwinLayer(
    val layerName: String,
    val depthOrHeight: String,
    val status: String,
    val metricValue: String,
    val healthColorHex: Long
)

// 14. FarmSathi AI Chat Message
data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: String,
    val audioAvailable: Boolean = true,
    val actionSuggestion: String? = null,
    val sourceFeatureRoute: String? = null
)

// 15. Notification Item
data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val category: String, // "MARKET", "DISEASE", "WEATHER", "IOT", "IRRIGATION"
    val timestamp: String,
    val isUrgent: Boolean = false,
    val isRead: Boolean = false
)

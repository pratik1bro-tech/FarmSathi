package com.example.data.repository

import com.example.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface FarmRepository {
    fun getFarmerProfile(): Flow<FarmerProfile>
    fun updateLanguage(langCode: String)
    fun getCropFields(): Flow<List<CropField>>
    fun getTelemetryData(): Flow<TelemetryData>
    fun toggleSolarPump(turnOn: Boolean)
    fun getSoilHealthReport(): Flow<SoilHealthReport>
    fun getRecentDiagnoses(): Flow<List<DiseaseDiagnosis>>
    fun addDiagnosis(diagnosis: DiseaseDiagnosis)
    fun getIrrigationZones(): Flow<List<IrrigationZone>>
    fun toggleValve(zoneId: String, open: Boolean)
    fun getWeatherForecast(): Flow<WeatherForecast>
    fun getMandiPrices(): Flow<List<MandiPriceItem>>
    fun getYieldForecasts(): Flow<List<YieldForecastData>>
    fun getBuyerOffers(): Flow<List<BuyerOffer>>
    fun getLogisticsTrips(): Flow<List<LogisticsTrip>>
    fun getOutbreakAlerts(): Flow<List<OutbreakAlert>>
    fun getDigitalTwinLayers(): Flow<List<DigitalTwinLayer>>
    fun getAiChatHistory(): Flow<List<AiChatMessage>>
    fun sendAiMessage(userText: String)
    fun getNotifications(): Flow<List<AppNotification>>
    fun markNotificationRead(id: String)
}

class FarmRepositoryImpl : FarmRepository {

    private val _farmerProfile = MutableStateFlow(FarmerProfile())
    private val _cropFields = MutableStateFlow(
        listOf(
            CropField(
                id = "f_1",
                name = "North Field Block A",
                cropName = "Soybean (JS-2034)",
                variety = "Early Maturing High Oil",
                areaAcres = 2.5,
                sowingDate = "20 June 2026",
                expectedHarvestDate = "28 Sept 2026",
                growthStage = "Pod Formation & Filling",
                healthScore = 92,
                soilMoisture = 42,
                nitrogenLevel = "Optimal",
                statusBadge = "Optimal Growth"
            ),
            CropField(
                id = "f_2",
                name = "South Field Block B",
                cropName = "Cotton (Bt Hybrid RCH-659)",
                variety = "Bollgard II",
                areaAcres = 1.8,
                sowingDate = "10 May 2026",
                expectedHarvestDate = "15 Nov 2026",
                growthStage = "Boll Development",
                healthScore = 84,
                soilMoisture = 34,
                nitrogenLevel = "Slightly Low",
                statusBadge = "Needs Urea Top-Dress"
            ),
            CropField(
                id = "f_3",
                name = "East Plot Polyhouse",
                cropName = "Hybrid Tomato (Abhinav)",
                variety = "Determinate Fresh Market",
                areaAcres = 0.9,
                sowingDate = "15 July 2026",
                expectedHarvestDate = "10 Oct 2026",
                growthStage = "Fruit Setting",
                healthScore = 96,
                soilMoisture = 55,
                nitrogenLevel = "Optimal",
                statusBadge = "Prime Quality"
            )
        )
    )

    private val _telemetry = MutableStateFlow(
        TelemetryData(
            deviceId = "ESP32-AGRI-SN-0982",
            lastSyncTime = "Active (20s ago)",
            isOnline = true,
            batteryPercent = 94,
            soilMoistureTop10cm = 41,
            soilMoistureDeep30cm = 48,
            soilTemperature = 25.1,
            ambientTemperature = 30.4,
            ambientHumidity = 61,
            solarRadiationLux = 52400,
            leafWetnessPercent = 14,
            solarPumpRunning = false,
            waterFlowRateLpm = 0.0
        )
    )

    private val _soilHealth = MutableStateFlow(
        SoilHealthReport(
            sampleDate = "12 Aug 2026",
            nitrogenPpm = 215.0,
            nitrogenStatus = "Moderate (215 kg/ha)",
            phosphorusPpm = 32.0,
            phosphorusStatus = "High (32 kg/ha)",
            potassiumPpm = 295.0,
            potassiumStatus = "High (295 kg/ha)",
            phLevel = 6.9,
            organicCarbonPercent = 0.68,
            electricalConductivity = 0.38,
            aiRecommendation = "Soil pH is excellent. Apply 20kg Urea per acre during next irrigation to reach optimal Nitrogen levels. Phosphorus & Potassium are rich."
        )
    )

    private val _diagnoses = MutableStateFlow(
        listOf(
            DiseaseDiagnosis(
                id = "d_1",
                cropName = "Soybean",
                diseaseName = "Yellow Mosaic Virus (Early Phase)",
                confidence = 0.94,
                severity = "Moderate",
                affectedAreaPercent = 12,
                symptoms = listOf(
                    "Mottled yellow & green patches on leaves",
                    "Slight curling of terminal leaves",
                    "Presence of whiteflies (Bemisia tabaci) under foliage"
                ),
                organicRemedy = "Spray 5% Neem Seed Kernel Extract (NSKE) or Neem Oil 3000 PPM (5ml/L) to control vector whiteflies.",
                chemicalTreatment = "Spray Acetamiprid 20% SP @ 0.4g/L water or Thiamethoxam 25% WG @ 0.3g/L during early morning.",
                preventiveTip = "Install yellow sticky traps (15 traps/acre) across field boundaries to trap whiteflies.",
                scannedAt = "Yesterday, 04:30 PM"
            ),
            DiseaseDiagnosis(
                id = "d_2",
                cropName = "Tomato",
                diseaseName = "Early Blight (Alternaria solani)",
                confidence = 0.97,
                severity = "Low",
                affectedAreaPercent = 5,
                symptoms = listOf(
                    "Concentric ring brown spots on lower leaves (target board pattern)",
                    "Lower leaf yellowing"
                ),
                organicRemedy = "Spray Trichoderma viride bio-fungicide @ 5g/L with cow urine solution.",
                chemicalTreatment = "Foliar spray of Mancozeb 75% WP @ 2.5g/L or Azoxystrobin @ 1ml/L.",
                preventiveTip = "Avoid sprinkler irrigation wetting leaves; ensure drip irrigation only.",
                scannedAt = "14 Aug 2026"
            )
        )
    )

    private val _irrigationZones = MutableStateFlow(
        listOf(
            IrrigationZone(
                id = "z_1",
                zoneName = "Zone 1: North Soybean",
                crop = "Soybean",
                moistureCurrent = 38,
                moistureTarget = 50,
                autoIrrigationEnabled = true,
                valveOpen = false,
                nextScheduledTime = "Today, 06:00 PM (Sunset)",
                estimatedWaterSavedLitres = 4200
            ),
            IrrigationZone(
                id = "z_2",
                zoneName = "Zone 2: South Cotton",
                crop = "Cotton",
                moistureCurrent = 32,
                moistureTarget = 45,
                autoIrrigationEnabled = true,
                valveOpen = true,
                nextScheduledTime = "Currently Irrigating",
                estimatedWaterSavedLitres = 2800
            ),
            IrrigationZone(
                id = "z_3",
                zoneName = "Zone 3: East Tomato Drip",
                crop = "Tomato Polyhouse",
                moistureCurrent = 58,
                moistureTarget = 60,
                autoIrrigationEnabled = true,
                valveOpen = false,
                nextScheduledTime = "Tomorrow, 07:00 AM",
                estimatedWaterSavedLitres = 1900
            )
        )
    )

    private val _weather = MutableStateFlow(
        WeatherForecast(
            currentTempC = 30.5,
            feelsLikeC = 33.0,
            condition = "Partly Cloudy • Gentle Breeze",
            humidityPercent = 64,
            windSpeedKmh = 12.8,
            rainfallChancePercent = 15,
            sprayAdvisoryStatus = "Excellent Window: Low wind speed (<15 km/h). Safe for foliar spray today.",
            dailyForecasts = listOf(
                DailyWeatherItem("Today", 32, 22, "Partly Cloudy", 15),
                DailyWeatherItem("Wed", 33, 23, "Sunny & Warm", 10),
                DailyWeatherItem("Thu", 29, 21, "Scattered Showers", 65),
                DailyWeatherItem("Fri", 28, 20, "Moderate Rain", 80),
                DailyWeatherItem("Sat", 30, 21, "Clearing Up", 25),
                DailyWeatherItem("Sun", 31, 22, "Clear Skies", 5),
                DailyWeatherItem("Mon", 32, 23, "Sunny", 10)
            )
        )
    )

    private val _mandiPrices = MutableStateFlow(
        listOf(
            MandiPriceItem(
                cropName = "Soybean (Yellow)",
                mandiName = "Indore APMC Yard",
                currentPricePerQuintal = 4850,
                yesterdayPricePerQuintal = 4620,
                minPrice = 4400,
                maxPrice = 5020,
                priceTrend = "UP",
                priceChangePercent = +4.97,
                sellRecommendation = "HOLD / WAIT (Price projected to touch ₹5,150 next week due to high solvent mill demand)",
                forecast7Days = 5150,
                confidenceScore = 91
            ),
            MandiPriceItem(
                cropName = "Cotton (Medium Staple)",
                mandiName = "Khargone Mandi",
                currentPricePerQuintal = 7250,
                yesterdayPricePerQuintal = 7300,
                minPrice = 6900,
                maxPrice = 7450,
                priceTrend = "STABLE",
                priceChangePercent = -0.68,
                sellRecommendation = "SELL NOW (Spinning mill procurement peak; prices likely to soften as arrivals surge)",
                forecast7Days = 7100,
                confidenceScore = 88
            ),
            MandiPriceItem(
                cropName = "Tomato (Hybrid Fresh)",
                mandiName = "Indore Choithram Mandi",
                currentPricePerQuintal = 2400,
                yesterdayPricePerQuintal = 2150,
                minPrice = 1800,
                maxPrice = 2650,
                priceTrend = "UP",
                priceChangePercent = +11.6,
                sellRecommendation = "SELL NOW (High spot demand from Delhi & Mumbai buyers)",
                forecast7Days = 2200,
                confidenceScore = 85
            ),
            MandiPriceItem(
                cropName = "Wheat (Lokwan / Sharbati)",
                mandiName = "Dewas Mandi",
                currentPricePerQuintal = 2780,
                yesterdayPricePerQuintal = 2750,
                minPrice = 2500,
                maxPrice = 2900,
                priceTrend = "STABLE",
                priceChangePercent = +1.09,
                sellRecommendation = "HOLD / WAIT",
                forecast7Days = 2850,
                confidenceScore = 93
            )
        )
    )

    private val _yieldForecasts = MutableStateFlow(
        listOf(
            YieldForecastData(
                cropName = "Soybean (JS-2034)",
                areaAcres = 2.5,
                estimatedYieldQuintals = 26.5,
                yieldRangeMin = 24.0,
                yieldRangeMax = 29.0,
                estimatedRevenueInr = 136475.0,
                harvestReadinessDays = 38,
                keyYieldDrivers = listOf(
                    "NDVI Canopy Greenness: 0.78 (Above average)",
                    "Soil Moisture Stability: High",
                    "Pollination Weather Factor: Favorable"
                )
            ),
            YieldForecastData(
                cropName = "Cotton (Hybrid)",
                areaAcres = 1.8,
                estimatedYieldQuintals = 16.2,
                yieldRangeMin = 14.5,
                yieldRangeMax = 18.0,
                estimatedRevenueInr = 117450.0,
                harvestReadinessDays = 85,
                keyYieldDrivers = listOf(
                    "Boll Count: Average 38 bolls/plant",
                    "Nitrogen supplementation required for max boll size"
                )
            )
        )
    )

    private val _buyerOffers = MutableStateFlow(
        listOf(
            BuyerOffer(
                id = "b_1",
                buyerName = "Anil Agrawal",
                buyerCompany = "Malwa Agro Solvent Extraction Ltd",
                rating = 4.9,
                verified = true,
                cropRequired = "Soybean (Clean & Dry)",
                quantityQuintals = 100.0,
                offeredPricePerQuintal = 4950,
                pickupLocation = "Farm Gate Pickup Available",
                paymentTerms = "Direct Bank Transfer on spot weighment",
                distanceKm = 12.4
            ),
            BuyerOffer(
                id = "b_2",
                buyerName = "Suresh Jain",
                buyerCompany = "Narmada Cotton Ginning Mills",
                rating = 4.8,
                verified = true,
                cropRequired = "Raw Seed Cotton (Kapas)",
                quantityQuintals = 50.0,
                offeredPricePerQuintal = 7350,
                pickupLocation = "Mandi Delivery / Shared Truck",
                paymentTerms = "Instant UPI / NEFT",
                distanceKm = 24.0
            ),
            BuyerOffer(
                id = "b_3",
                buyerName = "KisanFresh Supply Chain",
                buyerCompany = "OrganoDirect Retail Network",
                rating = 4.95,
                verified = true,
                cropRequired = "Fresh Grade-A Tomatoes",
                quantityQuintals = 25.0,
                offeredPricePerQuintal = 2550,
                pickupLocation = "Refrigerated Van at Farm Gate",
                paymentTerms = "Instant Payment within 1 hour",
                distanceKm = 8.5
            )
        )
    )

    private val _logisticsTrips = MutableStateFlow(
        listOf(
            LogisticsTrip(
                tripId = "TRIP-INDORE-882",
                truckType = "Tata 407 (3.5 Ton Capacity)",
                driverName = "Manoj Singh (Verified Driver)",
                driverPhone = "+91 94250 88712",
                departureTime = "Tomorrow, 05:30 AM",
                destinationMandi = "Indore APMC Mandi Yard",
                totalCapacityTons = 3.5,
                availableSpaceTons = 1.2,
                costPerQuintalInr = 45,
                pooledFarmersCount = 3,
                routeStops = listOf("Sanwer Village", "Bhanwar Kua Hub", "Indore Mandi Gate 2")
            ),
            LogisticsTrip(
                tripId = "TRIP-KHARGONE-104",
                truckType = "Mahindra Bolero Maxi Truck (1.8 Ton)",
                driverName = "Devendra Parmar",
                driverPhone = "+91 98271 22934",
                departureTime = "Thursday, 06:00 AM",
                destinationMandi = "Khargone Cotton Market",
                totalCapacityTons = 1.8,
                availableSpaceTons = 0.8,
                costPerQuintalInr = 55,
                pooledFarmersCount = 2,
                routeStops = listOf("Sanwer Outskirts", "Gogawan", "Khargone Yard")
            )
        )
    )

    private val _outbreakAlerts = MutableStateFlow(
        listOf(
            OutbreakAlert(
                id = "out_1",
                diseaseName = "Fall Armyworm (Spodoptera frugiperda)",
                crop = "Maize & Sorghum",
                distanceKm = 6.4,
                reportedVillage = "Dharampuri Village Cluster",
                affectedFarmsCount = 14,
                riskLevel = "HIGH RISK",
                spreadVelocity = "Moving Northeast with monsoon winds (15km/day)",
                recommendedAction = "Inspect whorls of plants immediately. Set pheromone traps (5/acre). If egg masses found, spray Emamectin Benzoate 5% SG @ 0.4g/L."
            ),
            OutbreakAlert(
                id = "out_2",
                diseaseName = "Yellow Mosaic Virus",
                crop = "Soybean",
                distanceKm = 11.2,
                reportedVillage = "Ajnod Sector",
                affectedFarmsCount = 8,
                riskLevel = "MODERATE",
                spreadVelocity = "Whitefly Vector migration observed",
                recommendedAction = "Maintain field sanitation and apply prophylactic Neem seed extract spray."
            )
        )
    )

    private val _digitalTwin = MutableStateFlow(
        listOf(
            DigitalTwinLayer("Canopy & Leaf Health", "+1.2m Height", "High NDVI (0.78)", "Optimal Photosynthesis", 0xFF2E7D32),
            DigitalTwinLayer("Ambient Microclimate", "+0.5m Surface", "30.4°C • 61% RH", "Gentle Aeration", 0xFF00897B),
            DigitalTwinLayer("Top Soil Zone (0-10cm)", "-5cm Depth", "38% Moisture • 25°C", "Good Root Oxygenation", 0xFF43A047),
            DigitalTwinLayer("Deep Root Zone (10-30cm)", "-25cm Depth", "45% Moisture", "Rich Organic Reserve", 0xFF6D4C41),
            DigitalTwinLayer("Bedrock & Water Table", "-2.5m Depth", "Water Table Healthy", "No Salinity Risk", 0xFF0288D1)
        )
    )

    private val _aiChat = MutableStateFlow(
        listOf(
            AiChatMessage(
                id = "ai_msg_1",
                isUser = false,
                text = "नमस्ते रामेश्वर जी! मैं आपका FarmSathi AI साथी हूँ।\n\nआज आपके सोयाबीन खेत (Block A) में मिट्टी की नमी 41% है और मौसम साफ है। कल शाम बारिश की 65% संभावना है, इसलिए आज कीटनाशक या खाद का छिड़काव करने का सबसे सही समय है।\n\nइंदौर मंडी में सोयाबीन का भाव ₹4,850 प्रति क्विंटल चल रहा है। क्या आप आज की योजना बनाना चाहते हैं?",
                timestamp = "08:15 AM",
                actionSuggestion = "आज का मौसम और छिड़काव एडवाइजरी देखें",
                sourceFeatureRoute = "weather_intelligence"
            )
        )
    )

    private val _notifications = MutableStateFlow(
        listOf(
            AppNotification(
                id = "notif_1",
                title = "Mandi Price Surge: Soybean +₹230/Q",
                message = "Soybean rates in Indore APMC crossed ₹4,850/Q today. AI recommends holding for another 5 days for ₹5,150 target.",
                category = "MARKET",
                timestamp = "30m ago",
                isUrgent = false
            ),
            AppNotification(
                id = "notif_2",
                title = "Outbreak Alert: Fall Armyworm within 6.4 km",
                message = "14 farms in nearby Dharampuri reported Fall Armyworm. Check your crop whorls and install pheromone traps.",
                category = "DISEASE",
                timestamp = "2h ago",
                isUrgent = true
            ),
            AppNotification(
                id = "notif_3",
                title = "Irrigation Cycle Complete: Zone 2",
                message = "South Cotton drip completed 45-minute cycle. 2,800 Litres water saved vs flood irrigation.",
                category = "IRRIGATION",
                timestamp = "4h ago",
                isUrgent = false
            )
        )
    )

    override fun getFarmerProfile(): Flow<FarmerProfile> = _farmerProfile.asStateFlow()

    override fun updateLanguage(langCode: String) {
        _farmerProfile.update { it.copy(selectedLanguage = langCode) }
    }

    override fun getCropFields(): Flow<List<CropField>> = _cropFields.asStateFlow()

    override fun getTelemetryData(): Flow<TelemetryData> = _telemetry.asStateFlow()

    override fun toggleSolarPump(turnOn: Boolean) {
        _telemetry.update {
            it.copy(
                solarPumpRunning = turnOn,
                waterFlowRateLpm = if (turnOn) 48.5 else 0.0
            )
        }
    }

    override fun getSoilHealthReport(): Flow<SoilHealthReport> = _soilHealth.asStateFlow()

    override fun getRecentDiagnoses(): Flow<List<DiseaseDiagnosis>> = _diagnoses.asStateFlow()

    override fun addDiagnosis(diagnosis: DiseaseDiagnosis) {
        _diagnoses.update { listOf(diagnosis) + it }
    }

    override fun getIrrigationZones(): Flow<List<IrrigationZone>> = _irrigationZones.asStateFlow()

    override fun toggleValve(zoneId: String, open: Boolean) {
        _irrigationZones.update { list ->
            list.map { if (it.id == zoneId) it.copy(valveOpen = open) else it }
        }
    }

    override fun getWeatherForecast(): Flow<WeatherForecast> = _weather.asStateFlow()

    override fun getMandiPrices(): Flow<List<MandiPriceItem>> = _mandiPrices.asStateFlow()

    override fun getYieldForecasts(): Flow<List<YieldForecastData>> = _yieldForecasts.asStateFlow()

    override fun getBuyerOffers(): Flow<List<BuyerOffer>> = _buyerOffers.asStateFlow()

    override fun getLogisticsTrips(): Flow<List<LogisticsTrip>> = _logisticsTrips.asStateFlow()

    override fun getOutbreakAlerts(): Flow<List<OutbreakAlert>> = _outbreakAlerts.asStateFlow()

    override fun getDigitalTwinLayers(): Flow<List<DigitalTwinLayer>> = _digitalTwin.asStateFlow()

    override fun getAiChatHistory(): Flow<List<AiChatMessage>> = _aiChat.asStateFlow()

    override fun sendAiMessage(userText: String) {
        val userMsg = AiChatMessage(
            id = "u_${System.currentTimeMillis()}",
            isUser = true,
            text = userText,
            timestamp = "Just now"
        )
        
        // Smart AI Farm Companion response generation based on context
        val lower = userText.lowercase()
        val aiResponseText = when {
            lower.contains("disease") || lower.contains("रोग") || lower.contains("leaf") || lower.contains("पत्ती") -> {
                "मैंने आपके खेत की स्थिति का विश्लेषण किया है। आपके ब्लॉक A में येलो मोज़ेक वायरस का प्रारंभिक लक्षण 12% क्षेत्र में देखा गया है। अनुशंसित उपाय: 5% नीम तेल (NSKE) 5ml/लीटर या थायमेथोक्सम 25% WG का 0.3g/लीटर सुबह के समय स्प्रे करें। क्या आप AI लीफ स्कैनर खोलना चाहते हैं?"
            }
            lower.contains("mandi") || lower.contains("भाव") || lower.contains("price") || lower.contains("bech") || lower.contains("sell") -> {
                "इंदौर मंडी में सोयाबीन का आज का भाव ₹4,850/क्विंटल है (+4.97% बढ़त)। AI विश्लेषण के अनुसार अगले 7 दिनों में भाव ₹5,150 तक पहुँचने का 91% अनुमान है। मेरी सलाह है कि आप 4-5 दिन प्रतीक्षा (HOLD) करें। पास के 2 सत्यापित खरीदार भी ₹4,950 में फार्म-गेट पिकअप का ऑफर दे रहे हैं।"
            }
            lower.contains("irrigation") || lower.contains("water") || lower.contains("पानी") || lower.contains("सिंचाई") -> {
                "आपके साउथ कॉटन फील्ड (Zone 2) में मिट्टी की नमी 32% थी, इसलिए स्मार्ट ड्रिप वॉल्व चालू कर दिया गया है। नॉर्थ सोयाबीन (Zone 1) में नमी 38% पर्याप्त है। कल शाम बारिश होने की संभावना है, जिससे 4,200 लीटर पानी की बचत होगी।"
            }
            lower.contains("soil") || lower.contains("खाद") || lower.contains("npk") || lower.contains("मिट्टी") -> {
                "आपकी मिट्टी की जांच रिपोर्ट: नाइट्रोजन (N) 215 kg/ha (हल्का कम), फॉस्फोरस (P) 32 kg/ha (उत्तम), पोटाश (K) 295 kg/ha (उत्तम), और pH 6.9 आदर्श है। बेहतर पैदावार के लिए प्रति एकड़ 20 किग्रा यूरिया और एजोटोबैक्टर का उपयोग करें।"
            }
            lower.contains("weather") || lower.contains("मौसम") || lower.contains("rain") || lower.contains("बारिश") -> {
                "आज तापमान 31°C और हवा 13 किमी/घंटा है - कीटनाशक छिड़काव के लिए बहुत अनुकूल है। गुरुवार और शुक्रवार को 65-80% भारी बारिश का पूर्वानुमान है। खेत की जल निकासी नालियां खुली रखें।"
            }
            else -> {
                "नमस्ते! मैं आपके फार्म के ESP32 सेंसर, मिट्टी की NPK रिपोर्ट, उपग्रह मौसम और मंडी भावों को लाइव ट्रैक कर रहा हूँ। आप मुझसे फसल रोग पहचान, स्मार्ट सिंचाई, मंडी भाव सलाह, या शेयरिंग ट्रक बुकिंग के बारे में कुछ भी पूछ सकते हैं।"
            }
        }

        val aiMsg = AiChatMessage(
            id = "ai_${System.currentTimeMillis()}",
            isUser = false,
            text = aiResponseText,
            timestamp = "Just now",
            actionSuggestion = if (lower.contains("disease") || lower.contains("रोग")) "AI लीफ स्कैन शुरू करें" else "मंडी भाव विश्लेषण देखें",
            sourceFeatureRoute = if (lower.contains("disease") || lower.contains("रोग")) "disease_detection" else "market"
        )

        _aiChat.update { it + listOf(userMsg, aiMsg) }
    }

    override fun getNotifications(): Flow<List<AppNotification>> = _notifications.asStateFlow()

    override fun markNotificationRead(id: String) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }
}

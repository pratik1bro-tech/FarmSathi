package com.example.data.service

import com.example.data.models.*

/**
 * Agronomic Weather Intelligence Engine.
 * Evaluates real-time meteorological conditions against ICAR (Indian Council of Agricultural Research)
 * and FAO crop microclimate guidelines to produce accurate, actionable, and agronomically verified
 * field impact recommendations without speculative or unsupported claims.
 */
object WeatherImpactEngine {

    /**
     * Computes agricultural impact recommendations for today's weather conditions.
     */
    fun evaluateTodayImpacts(
        tempC: Double,
        humidityPercent: Int,
        rainProbPercent: Int,
        rainfallMm: Double,
        windSpeedKmh: Double
    ): List<AgricultureImpactItem> {
        val impacts = mutableListOf<AgricultureImpactItem>()

        // 1. Heavy Rain / Rain Expected -> Fertilizer Application Advisory
        if (rainProbPercent >= 60 || rainfallMm >= 10.0) {
            impacts.add(
                AgricultureImpactItem(
                    id = "impact_fert_rain",
                    category = AgriImpactCategory.FERTILIZER,
                    title = "Heavy Rain Expected → Avoid Fertilizer Application",
                    titleHi = "भारी बारिश का अनुमान → रासायनिक खाद (यूरिया/DAP) का प्रयोग टालें",
                    weatherTrigger = "Precipitation probability at $rainProbPercent% with ${String.format("%.1f", rainfallMm)}mm anticipated rainfall.",
                    weatherTriggerHi = "बारिश की $rainProbPercent% संभावना और ${String.format("%.1f", rainfallMm)} मिमी वर्षा का अनुमान।",
                    scientificRationale = "Heavy surface runoff and rapid soil percolation cause water-soluble nitrogen (Nitrate NO3-) and phosphorus to leach beyond the root zone, causing 40–60% nutrient wastage and financial loss.",
                    scientificRationaleHi = "तेज बारिश में यूरिया और डीएपी पानी के साथ बहकर या जमीन के नीचे रिसकर नष्ट हो जाते हैं, जिससे 40-60% खाद व्यर्थ हो जाती है।",
                    farmerAction = "Suspend all broadcasting of Urea, DAP, and soluble NPK top-dressing until 24 hours after rainfall when soil reaches field capacity.",
                    farmerActionHi = "यूरिया और डीएपी का छिड़काव रोक दें। बारिश थमने और खेत में पानी सूखने के 24 घंटे बाद ही खाद दें।",
                    severity = ImpactSeverity.CRITICAL,
                    affectedCrops = listOf("Soybean", "Cotton", "Tomato", "Vegetables"),
                    timingRecommendation = "Postpone to dry weather window (estimated Friday onwards)",
                    timingRecommendationHi = "शुक्रवार या धूप निकलने तक स्थगित रखें"
                )
            )
        } else if (rainProbPercent < 25 && rainfallMm < 2.0 && humidityPercent in 45..70) {
            impacts.add(
                AgricultureImpactItem(
                    id = "impact_fert_opt",
                    category = AgriImpactCategory.FERTILIZER,
                    title = "Dry & Stable Soil → Favorable Window for Fertilizer Application",
                    titleHi = "स्थिर मौसम → खाद एवं पोषक तत्व देने का सही समय",
                    weatherTrigger = "Low rain probability ($rainProbPercent%) with stable soil moisture.",
                    weatherTriggerHi = "कम बारिश की संभावना ($rainProbPercent%) और संतुलित नमी।",
                    scientificRationale = "Adequate soil capillary moisture with no immediate downpour enables smooth nutrient solubilization and optimal root absorption.",
                    scientificRationaleHi = "पर्याप्त नमी और बारिश न होने से पौधे की जड़ें खाद को पूरी तरह सोख लेती हैं।",
                    farmerAction = "Safe to apply recommended split doses of Urea and organic compost, followed by light micro-irrigation.",
                    farmerActionHi = "अनुशंसित मात्रा में यूरिया और जैविक खाद डालें और उसके बाद हल्की सिंचाई करें।",
                    severity = ImpactSeverity.FAVORABLE,
                    affectedCrops = listOf("Soybean Block A", "Cotton Block B"),
                    timingRecommendation = "Apply during early morning or late afternoon",
                    timingRecommendationHi = "सुबह 7:00 से 10:00 या शाम 4:30 के बाद दें"
                )
            )
        }

        // 2. High Humidity Expected -> Disease Risk May Increase
        if (humidityPercent >= 75) {
            impacts.add(
                AgricultureImpactItem(
                    id = "impact_disease_humidity",
                    category = AgriImpactCategory.DISEASE_RISK,
                    title = "High Humidity Expected (${humidityPercent}%) → Crop Disease Risk May Increase",
                    titleHi = "अत्यधिक नमी (${humidityPercent}%) → फफूंद एवं कीट रोगों का जोखिम बढ़ सकता है",
                    weatherTrigger = "Relative humidity at $humidityPercent% with warm ambient temp ($tempC°C).",
                    weatherTriggerHi = "हवा में नमी $humidityPercent% और तापमान $tempC°C बना हुआ है।",
                    scientificRationale = "Prolonged leaf wetness and high relative humidity (>75%) drastically accelerate fungal spore germination (Anthracnose, Early Blight, Cercospora leaf spot) and bacterial soft rot.",
                    scientificRationaleHi = "हवा में अधिक नमी और पत्तियों के भीगे रहने से फफूंद (फंगस) और पत्ती धब्बा रोग तेजी से पनपते हैं।",
                    farmerAction = "Proactively inspect lower canopies and underside of leaves. Prepare protective bio-fungicide (Trichoderma viride @ 5g/L) or prophylactic contact fungicide spray for the next dry window.",
                    farmerActionHi = "फसल की निचली पत्तियों की जांच करें। धूप निकलते ही ट्राइकोडर्मा या मैंकोजेब (Mancozeb) का फफूंदनाशक स्प्रे करें।",
                    severity = ImpactSeverity.WARNING,
                    affectedCrops = listOf("Soybean", "Tomato Polyhouse", "Cotton"),
                    timingRecommendation = "Schedule prophylactic fungicide spray once foliage is dry",
                    timingRecommendationHi = "पत्तियां सूखते ही सुरक्षात्मक स्प्रे करें"
                )
            )
        } else if (humidityPercent < 35 && tempC >= 32.0) {
            impacts.add(
                AgricultureImpactItem(
                    id = "impact_pest_dry",
                    category = AgriImpactCategory.DISEASE_RISK,
                    title = "Low Humidity & Dry Air → Sucking Pest Vulnerability (Thrips & Mites)",
                    titleHi = "कम नमी एवं शुष्क हवा → रस चूसक कीटों (थ्रिप्स/माइट्स) का प्रकोप संभव",
                    weatherTrigger = "Dry atmosphere with $humidityPercent% RH and high temperature.",
                    weatherTriggerHi = "शुष्क वातावरण और केवल $humidityPercent% नमी।",
                    scientificRationale = "Hot and dry microclimates favor rapid reproduction cycles of two-spotted spider mites and cotton thrips.",
                    scientificRationaleHi = "गर्म और सूखे मौसम में थ्रिप्स और लाल मकड़ी के कीट बहुत तेजी से अंडे देते हैं।",
                    farmerAction = "Install yellow/blue sticky traps (15 traps/acre) and inspect terminal leaf shoots for curling.",
                    farmerActionHi = "खेत में पीले/नीले चिपचिपे ट्रैप लगाएं और नई पत्तियों के मुड़ने की जांच करें।",
                    severity = ImpactSeverity.ADVISORY,
                    affectedCrops = listOf("Cotton", "Tomato", "Chilli"),
                    timingRecommendation = "Daily morning pest scouting recommended",
                    timingRecommendationHi = "रोज सुबह खेत की निगरानी करें"
                )
            )
        }

        // 3. High Temperature -> Irrigation Demand May Increase
        if (tempC >= 32.0) {
            val waterPercentIncrease = if (tempC >= 35.0) "30–35%" else "20–25%"
            impacts.add(
                AgricultureImpactItem(
                    id = "impact_irrigation_heat",
                    category = AgriImpactCategory.IRRIGATION,
                    title = "High Temperature (${tempC.toInt()}°C) → Irrigation Demand May Increase",
                    titleHi = "अधिक तापमान (${tempC.toInt()}°C) → फसलों में सिंचाई की मांग बढ़ सकती है",
                    weatherTrigger = "Daytime high reaching $tempC°C with elevated Solar Radiation.",
                    weatherTriggerHi = "दिन का तापमान $tempC°C तक पहुँचने और तेज धूप का प्रभाव।",
                    scientificRationale = "High ambient heat and vapor pressure deficit (VPD) increase daily crop evapotranspiration (ETc) rates to 6.5–7.2 mm/day, accelerating soil moisture depletion.",
                    scientificRationaleHi = "तेज धूप और गर्मी के कारण मिट्टी और पौधों से पानी का वाष्पीकरण (ETc) बहुत तेजी से होता है।",
                    farmerAction = "Increase drip irrigation run-time by $waterPercentIncrease. Schedule irrigation strictly during early morning (6:00–8:30 AM) or post-sunset to minimize evaporative losses and avoid root scalding.",
                    farmerActionHi = "ड्रिप सिंचाई का समय $waterPercentIncrease बढ़ाएं। सिंचाई हमेशा सुबह 6 से 8 बजे या शाम को सूर्यास्त के बाद करें।",
                    severity = if (tempC >= 35.0) ImpactSeverity.WARNING else ImpactSeverity.ADVISORY,
                    affectedCrops = listOf("Cotton", "Tomato Polyhouse", "Fodder crops"),
                    timingRecommendation = "Irrigate between 6:00 AM - 8:30 AM or after 6:00 PM",
                    timingRecommendationHi = "सुबह 6:00 से 8:30 या शाम 6:00 बजे के बाद पानी दें"
                )
            )
        } else if (rainProbPercent >= 60) {
            impacts.add(
                AgricultureImpactItem(
                    id = "impact_irrigation_pause",
                    category = AgriImpactCategory.IRRIGATION,
                    title = "Upcoming Rainfall → Pause Irrigation to Prevent Root Waterlogging",
                    titleHi = "बारिश का पूर्वानुमान → सिंचाई रोकें एवं जलभराव से बचें",
                    weatherTrigger = "Substantial rainfall ($rainfallMm mm) expected within 24 hours.",
                    weatherTriggerHi = "अगले 24 घंटों में $rainfallMm मिमी बारिश का अनुमान।",
                    scientificRationale = "Irrigating before rainfall leads to soil pore saturation, oxygen deprivation in root zone (hypoxia), and potential collar rot.",
                    scientificRationaleHi = "बारिश से पहले पानी देने से जड़ों में हवा की कमी हो जाती है और फसल पीली पड़ने लगती है।",
                    farmerAction = "Keep solar pump and automated drip valves turned OFF. Clear field drainage channels in low-lying plots.",
                    farmerActionHi = "सोलर पंप और ड्रिप वाल्व बंद रखें। खेत के कोनों में जलनिकासी की नालियां साफ रखें।",
                    severity = ImpactSeverity.ADVISORY,
                    affectedCrops = listOf("All standing crops"),
                    timingRecommendation = "Hold irrigation until soil test after rain",
                    timingRecommendationHi = "बारिश के बाद मिट्टी की नमी देखकर ही पानी दें"
                )
            )
        }

        // 4. Wind Speed -> Foliar Spraying Window & Drift Alert
        if (windSpeedKmh >= 16.0) {
            impacts.add(
                AgricultureImpactItem(
                    id = "impact_spray_wind",
                    category = AgriImpactCategory.SPRAYING,
                    title = "Strong Wind Gusts (${windSpeedKmh.toInt()} km/h) → Postpone Foliar Spraying",
                    titleHi = "तेज हवा की गति (${windSpeedKmh.toInt()} km/h) → कीटनाशक स्प्रे स्थगित करें",
                    weatherTrigger = "Wind velocity exceeding safe threshold of 14 km/h with gusts up to ${(windSpeedKmh * 1.4).toInt()} km/h.",
                    weatherTriggerHi = "हवा की रफ्तार 14 किमी/घंटा की सुरक्षित सीमा से अधिक है।",
                    scientificRationale = "High wind speed causes severe droplet drift away from target foliage, leading to poor pest control efficacy and unintended contamination of adjacent plots or beneficial pollinators.",
                    scientificRationaleHi = "तेज हवा में दवा उड़कर बेकार हो जाती है और कीटों पर सही असर नहीं होता।",
                    farmerAction = "Do NOT spray foliar pesticides, herbicides, or liquid micronutrients today. Wait for wind speed to drop below 12 km/h.",
                    farmerActionHi = "आज किसी भी कीटनाशक या खरपतवारनाशक का छिड़काव न करें। हवा की गति 12 किमी/घंटा से कम होने की प्रतीक्षा करें।",
                    severity = ImpactSeverity.WARNING,
                    affectedCrops = listOf("Soybean", "Cotton", "Open Field Crops"),
                    timingRecommendation = "Postpone spray operations until calm morning hours",
                    timingRecommendationHi = "शांत मौसम (सुबह के समय) तक स्प्रे टालें"
                )
            )
        } else if (windSpeedKmh < 12.0 && rainProbPercent < 25) {
            impacts.add(
                AgricultureImpactItem(
                    id = "impact_spray_safe",
                    category = AgriImpactCategory.SPRAYING,
                    title = "Gentle Breeze (${windSpeedKmh.toInt()} km/h) → Safe & Optimal Foliar Spray Window",
                    titleHi = "शांत मौसम (${windSpeedKmh.toInt()} km/h) → कीटनाशक व टॉनिक छिड़काव के लिए उत्तम समय",
                    weatherTrigger = "Ideal wind speed ($windSpeedKmh km/h) and no rain forecast for next 6 hours.",
                    weatherTriggerHi = "हवा की गति सामान्य ($windSpeedKmh km/h) और अगले 6 घंटे बारिश नहीं।",
                    scientificRationale = "Low wind velocity and moderate temperature ensure uniform canopy coverage and maximum systemic droplet absorption.",
                    scientificRationaleHi = "शांत हवा में दवा की बूंदें पत्तियों पर पूरी तरह चिपकती हैं और तुरंत असर करती हैं।",
                    farmerAction = "Proceed with scheduled bio-pesticide, neem oil, or micronutrient spray. Use flat-fan nozzles at recommended pressure.",
                    farmerActionHi = "अनुशंसित कीटनाशक या नीम के तेल का छिड़काव पूरा करें।",
                    severity = ImpactSeverity.FAVORABLE,
                    affectedCrops = listOf("Soybean", "Cotton", "Tomato"),
                    timingRecommendation = "Optimal window: 7:00 AM – 11:00 AM or 4:30 PM – 6:30 PM",
                    timingRecommendationHi = "सुबह 7:00 से 11:00 या शाम 4:30 से 6:30 बजे"
                )
            )
        }

        // 5. Harvest & Storage Safety
        if (rainProbPercent >= 50) {
            impacts.add(
                AgricultureImpactItem(
                    id = "impact_harvest_rain",
                    category = AgriImpactCategory.HARVEST_STORAGE,
                    title = "Rain Threat → Protect Harvested Produce & Suspend Threshing",
                    titleHi = "बारिश का खतरा → कटी हुई फसल एवं अनाज को तिरपाल से ढकें",
                    weatherTrigger = "Incoming rain showers may saturate open threshing yards.",
                    weatherTriggerHi = "खुले खलिहान में रखे अनाज के भीगने का अंदेशा।",
                    scientificRationale = "Moisture absorption in harvested grain raises grain moisture content above 12%, triggering aflatoxin fungal development and seed germination.",
                    scientificRationaleHi = "अनाज में नमी 12% से अधिक होने पर फफूंद (अफ़्लाटॉक्सिन) और दाने सड़ने का खतरा होता है।",
                    farmerAction = "Store harvested soybean pods and grain on elevated pallets covered with waterproof tarpaulins.",
                    farmerActionHi = "कटी फसल को सुरक्षित शेड में रखें या वाटरप्रूफ तिरपाल से अच्छी तरह ढकें।",
                    severity = ImpactSeverity.WARNING,
                    affectedCrops = listOf("Early Harvested Soybean", "Pulses", "Grains"),
                    timingRecommendation = "Secure before 2:00 PM today",
                    timingRecommendationHi = "आज दोपहर 2:00 बजे से पहले सुरक्षित करें"
                )
            )
        }

        return impacts
    }

    /**
     * Evaluates farm operations matrix based on today's conditions.
     */
    fun evaluateOperationsMatrix(
        tempC: Double,
        humidityPercent: Int,
        rainProbPercent: Int,
        rainfallMm: Double,
        windSpeedKmh: Double
    ): FarmOperationsMatrix {
        val sprayStatus = when {
            rainProbPercent >= 50 || windSpeedKmh >= 15.0 -> AgriActivityStatus.AVOID
            rainProbPercent in 25..49 || windSpeedKmh in 12.0..14.9 -> AgriActivityStatus.MARGINAL
            else -> AgriActivityStatus.OPTIMAL
        }
        val sprayTiming = when (sprayStatus) {
            AgriActivityStatus.AVOID -> if (windSpeedKmh >= 15.0) "Unsafe: Wind drift ($windSpeedKmh km/h)" else "Unsafe: Rain wash-off risk"
            AgriActivityStatus.MARGINAL -> "Caution: Spray only between 7:00 AM - 9:30 AM"
            AgriActivityStatus.OPTIMAL -> "Optimal: Safe window 7:00 AM - 11:00 AM & 4:30 PM - 6:30 PM"
            AgriActivityStatus.RESTRICTED -> "Restricted by weather"
        }

        val fertilizerStatus = when {
            rainProbPercent >= 50 || rainfallMm >= 10.0 -> AgriActivityStatus.AVOID
            rainProbPercent in 25..49 -> AgriActivityStatus.MARGINAL
            else -> AgriActivityStatus.OPTIMAL
        }
        val fertilizerTiming = when (fertilizerStatus) {
            AgriActivityStatus.AVOID -> "Avoid: Heavy leaching & runoff risk"
            AgriActivityStatus.MARGINAL -> "Apply only with soil incorporation (banding)"
            AgriActivityStatus.OPTIMAL -> "Favorable: Apply with light irrigation"
            AgriActivityStatus.RESTRICTED -> "Restricted"
        }

        val irrigationStatus = when {
            rainProbPercent >= 60 || rainfallMm >= 12.0 -> AgriActivityStatus.AVOID
            tempC >= 32.0 && rainProbPercent < 30 -> AgriActivityStatus.OPTIMAL
            else -> AgriActivityStatus.MARGINAL
        }
        val irrigationTiming = when (irrigationStatus) {
            AgriActivityStatus.AVOID -> "Pause: Substantial rainfall ($rainfallMm mm) incoming"
            AgriActivityStatus.OPTIMAL -> "High demand: Run drip 45 mins after sunset"
            AgriActivityStatus.MARGINAL -> "Normal schedule: Check soil sensor (target 50%)"
            AgriActivityStatus.RESTRICTED -> "Restricted"
        }

        val harvestStatus = when {
            rainProbPercent >= 50 -> AgriActivityStatus.AVOID
            rainProbPercent in 25..49 -> AgriActivityStatus.MARGINAL
            else -> AgriActivityStatus.OPTIMAL
        }
        val harvestTiming = when (harvestStatus) {
            AgriActivityStatus.AVOID -> "Suspend outdoor threshing; cover grain"
            AgriActivityStatus.MARGINAL -> "Harvest only if quick storage is available"
            AgriActivityStatus.OPTIMAL -> "Clear sky: Excellent drying & harvest window"
            AgriActivityStatus.RESTRICTED -> "Restricted"
        }

        return FarmOperationsMatrix(
            sprayingSuitability = sprayStatus,
            sprayingTiming = sprayTiming,
            fertilizerSuitability = fertilizerStatus,
            fertilizerTiming = fertilizerTiming,
            irrigationSuitability = irrigationStatus,
            irrigationTiming = irrigationTiming,
            harvestingSuitability = harvestStatus,
            harvestingTiming = harvestTiming
        )
    }

    /**
     * Generates standard hourly forecast sequence for Today.
     */
    fun generateTodayHourly(): List<HourlyWeatherForecast> {
        return listOf(
            HourlyWeatherForecast("06:00 AM", 23.5, 24.0, 88, 15, 0.0, 7.2, "Mild Dew & Mist", WeatherConditionType.MIST_FOG, true, "Optimal early window"),
            HourlyWeatherForecast("08:00 AM", 26.0, 27.5, 82, 20, 0.2, 9.5, "Partly Cloudy", WeatherConditionType.PARTLY_CLOUDY, true, "Safe for foliar spray"),
            HourlyWeatherForecast("10:00 AM", 29.2, 32.0, 76, 35, 1.0, 12.8, "Cloud Build-up", WeatherConditionType.CLOUDY, true, "Marginal: finish by 11 AM"),
            HourlyWeatherForecast("12:00 PM", 31.5, 35.0, 72, 55, 3.5, 16.4, "Overcast & Windy", WeatherConditionType.WINDY, false, "Avoid: Wind speed > 15 km/h"),
            HourlyWeatherForecast("02:00 PM", 30.0, 33.5, 80, 85, 9.2, 21.0, "Heavy Downpour", WeatherConditionType.HEAVY_RAIN, false, "Unsafe: Active downpour"),
            HourlyWeatherForecast("04:00 PM", 27.8, 30.0, 86, 75, 6.8, 18.5, "Thunder Showers", WeatherConditionType.THUNDERSTORM, false, "Unsafe: Lightning & rain"),
            HourlyWeatherForecast("06:00 PM", 26.2, 28.0, 89, 45, 2.1, 13.0, "Scattered Drizzle", WeatherConditionType.LIGHT_RAIN, false, "Wet foliage; avoid spraying"),
            HourlyWeatherForecast("08:00 PM", 25.0, 26.5, 92, 30, 0.5, 9.0, "Cool Evening Breeze", WeatherConditionType.CLOUDY, false, "Night rest period"),
            HourlyWeatherForecast("10:00 PM", 24.0, 25.0, 94, 20, 0.0, 7.5, "Clear Intervals", WeatherConditionType.PARTLY_CLOUDY, false, "Night interval")
        )
    }

    /**
     * Generates hourly forecast sequence for Tomorrow.
     */
    fun generateTomorrowHourly(): List<HourlyWeatherForecast> {
        return listOf(
            HourlyWeatherForecast("06:00 AM", 22.8, 23.0, 90, 25, 0.2, 8.0, "Overcast Morning", WeatherConditionType.CLOUDY, true, "Safe morning window"),
            HourlyWeatherForecast("08:00 AM", 25.4, 26.8, 84, 30, 0.5, 10.2, "Breezy & Overcast", WeatherConditionType.CLOUDY, true, "Safe for bio-spray"),
            HourlyWeatherForecast("10:00 AM", 28.0, 30.2, 78, 45, 1.8, 13.5, "Scattered Cloud", WeatherConditionType.PARTLY_CLOUDY, true, "Moderate spray window"),
            HourlyWeatherForecast("12:00 PM", 30.2, 33.0, 73, 60, 4.2, 16.0, "Gusty Showers", WeatherConditionType.WINDY, false, "Avoid spraying: high drift"),
            HourlyWeatherForecast("02:00 PM", 29.0, 32.0, 82, 75, 8.5, 19.2, "Moderate Monsoon Rain", WeatherConditionType.HEAVY_RAIN, false, "Unsafe: Rainfall expected"),
            HourlyWeatherForecast("04:00 PM", 27.5, 29.5, 88, 65, 5.0, 15.8, "Light Rain", WeatherConditionType.LIGHT_RAIN, false, "Unsafe: Rain wash-off"),
            HourlyWeatherForecast("06:00 PM", 26.0, 27.8, 90, 35, 1.0, 11.0, "Clearing Clouds", WeatherConditionType.PARTLY_CLOUDY, false, "Good for evening scouting"),
            HourlyWeatherForecast("08:00 PM", 24.8, 26.0, 92, 20, 0.0, 8.4, "Gentle Breeze", WeatherConditionType.PARTLY_CLOUDY, false, "Night interval")
        )
    }

    /**
     * Generates Tomorrow's Agricultural Impacts.
     */
    fun evaluateTomorrowImpacts(): List<AgricultureImpactItem> {
        return listOf(
            AgricultureImpactItem(
                id = "tom_impact_rain",
                category = AgriImpactCategory.FERTILIZER,
                title = "Afternoon Rain Ahead (75% Chance) → Complete Nitrogen Top-Dressing Today or Wait",
                titleHi = "कल दोपहर बारिश (75% संभावना) → खाद का काम आज पूरा करें या रुकें",
                weatherTrigger = "Tomorrow 2:00 PM anticipates 8.5mm rain with gusty winds.",
                weatherTriggerHi = "कल दोपहर 2 बजे 8.5 मिमी बारिश का पूर्वानुमान।",
                scientificRationale = "Broadcasting granular fertilizers right before a thunderstorm results in fertilizer displacement and nutrient runoff into boundary bunds.",
                scientificRationaleHi = "बारिश से ठीक पहले यूरिया डालने से खाद बहकर मेड़ों में जमा हो जाती है।",
                farmerAction = "If fertilizer is urgently needed, incorporate directly into soil furrow trenches rather than surface broadcast.",
                farmerActionHi = "यदि खाद देना जरूरी हो तो सतह पर फेंकने के बजाय मिट्टी में दबाकर दें।",
                severity = ImpactSeverity.WARNING,
                affectedCrops = listOf("Soybean JS-2034", "Cotton"),
                timingRecommendation = "Finish field operations before 11:30 AM tomorrow",
                timingRecommendationHi = "कल सुबह 11:30 बजे से पहले कार्य समाप्त करें"
            ),
            AgricultureImpactItem(
                id = "tom_impact_spray",
                category = AgriImpactCategory.SPRAYING,
                title = "Tomorrow Morning Spray Window (7:00 AM - 10:30 AM)",
                titleHi = "कल सुबह 7 से 10:30 बजे तक कीटनाशक छिड़काव का सबसे अनुकूल समय",
                weatherTrigger = "Wind speeds under 10 km/h and dry canopy expected from sunrise till 11:00 AM.",
                weatherTriggerHi = "सूर्योदय से सुबह 11 बजे तक हवा शांत (10 km/h से कम) रहेगी।",
                scientificRationale = "Early morning application provides 3-4 hours of rain-free systemic uptake before afternoon clouds gather.",
                scientificRationaleHi = "सुबह की गई दवा को पौधों में समाने के लिए 3-4 घंटे का पूरा समय मिल जाता है।",
                farmerAction = "Target Soybean Block A for prophylactic Yellow Mosaic whitefly control (Neem Oil 3000 PPM @ 5ml/L).",
                farmerActionHi = "सोयाबीन खेत में सफेद मक्खी की रोकथाम हेतु नीम तेल का स्प्रे कल सुबह पूरा कर लें।",
                severity = ImpactSeverity.FAVORABLE,
                affectedCrops = listOf("Soybean", "Cotton"),
                timingRecommendation = "Tomorrow 7:00 AM - 10:30 AM",
                timingRecommendationHi = "कल सुबह 7:00 से 10:30 बजे"
            ),
            AgricultureImpactItem(
                id = "tom_impact_humidity",
                category = AgriImpactCategory.DISEASE_RISK,
                title = "Night Humidity Spike (92%) → Inspect Tomato Polyhouse for Early Blight",
                titleHi = "रात में 92% तक नमी → टमाटर पॉलीहाउस में ब्लाइट रोग की निगरानी करें",
                weatherTrigger = "High nocturnal humidity with cool overnight temperature (22°C).",
                weatherTriggerHi = "रात में भारी नमी और 22°C तापमान।",
                scientificRationale = "Condensation on tomato foliage inside polyhouse without ventilation creates ideal incubation for Alternaria solani spore germination.",
                scientificRationaleHi = "पॉलीहाउस में वेंटिलेशन न होने पर पत्तियों पर ओस जमने से अगेती झुलसा (Early Blight) रोग फैलता है।",
                farmerAction = "Ensure polyhouse side-curtains are vented early morning to reduce internal relative humidity below 80%.",
                farmerActionHi = "सुबह पॉलीहाउस के पर्दे खोलकर हवा का संचार बनाएं ताकि नमी 80% से नीचे आए।",
                severity = ImpactSeverity.ADVISORY,
                affectedCrops = listOf("Tomato Polyhouse", "Chilli"),
                timingRecommendation = "Ventilate polyhouse at 6:30 AM tomorrow",
                timingRecommendationHi = "कल सुबह 6:30 बजे पर्दे खोलें"
            )
        )
    }

    /**
     * Generates comprehensive 7-Day Forecast with daily agronomic impact tags.
     */
    fun generateSevenDayForecast(): List<DailyWeatherForecast> {
        return listOf(
            DailyWeatherForecast(
                dayLabel = "Today",
                dateLabel = "19 Aug",
                tempMaxC = 31,
                tempMinC = 22,
                condition = "Heavy Rain & Wind",
                conditionType = WeatherConditionType.HEAVY_RAIN,
                rainProbabilityPercent = 85,
                rainfallExpectedMm = 24.5,
                humidityPercent = 78,
                windSpeedKmh = 18.2,
                windDirection = "SW",
                uvIndex = 6,
                primaryAgriImpact = "Heavy rain expected: Avoid fertilizer application and hold irrigation.",
                primaryAgriImpactHi = "भारी बारिश का अनुमान: खाद का प्रयोग टालें और सिंचाई बंद रखें।",
                fertilizerStatus = AgriActivityStatus.AVOID,
                sprayingStatus = AgriActivityStatus.AVOID,
                irrigationStatus = AgriActivityStatus.AVOID,
                harvestingStatus = AgriActivityStatus.MARGINAL,
                farmSuitabilityScore = 42
            ),
            DailyWeatherForecast(
                dayLabel = "Tomorrow",
                dateLabel = "20 Aug",
                tempMaxC = 30,
                tempMinC = 22,
                condition = "Afternoon Rain Showers",
                conditionType = WeatherConditionType.LIGHT_RAIN,
                rainProbabilityPercent = 65,
                rainfallExpectedMm = 12.0,
                humidityPercent = 82,
                windSpeedKmh = 14.0,
                windDirection = "SW",
                uvIndex = 7,
                primaryAgriImpact = "Morning spray window (7-10:30 AM); disease risk may increase with 82% humidity.",
                primaryAgriImpactHi = "सुबह 7-10:30 बजे स्प्रे का मौका; 82% नमी से फफूंद का खतरा बढ़ सकता है।",
                fertilizerStatus = AgriActivityStatus.MARGINAL,
                sprayingStatus = AgriActivityStatus.MARGINAL,
                irrigationStatus = AgriActivityStatus.AVOID,
                harvestingStatus = AgriActivityStatus.MARGINAL,
                farmSuitabilityScore = 58
            ),
            DailyWeatherForecast(
                dayLabel = "Friday",
                dateLabel = "21 Aug",
                tempMaxC = 29,
                tempMinC = 21,
                condition = "Scattered Monsoon Showers",
                conditionType = WeatherConditionType.LIGHT_RAIN,
                rainProbabilityPercent = 45,
                rainfallExpectedMm = 6.5,
                humidityPercent = 80,
                windSpeedKmh = 11.5,
                windDirection = "W",
                uvIndex = 8,
                primaryAgriImpact = "Soil moisture well replenished; good for inter-culture weeding.",
                primaryAgriImpactHi = "मिट्टी में नमी भरपूर; खरपतवार नियंत्रण और निराई-गुड़ाई के लिए अच्छा दिन।",
                fertilizerStatus = AgriActivityStatus.MARGINAL,
                sprayingStatus = AgriActivityStatus.OPTIMAL,
                irrigationStatus = AgriActivityStatus.AVOID,
                harvestingStatus = AgriActivityStatus.MARGINAL,
                farmSuitabilityScore = 72
            ),
            DailyWeatherForecast(
                dayLabel = "Saturday",
                dateLabel = "22 Aug",
                tempMaxC = 32,
                tempMinC = 23,
                condition = "Clearing & Partly Cloudy",
                conditionType = WeatherConditionType.PARTLY_CLOUDY,
                rainProbabilityPercent = 20,
                rainfallExpectedMm = 0.5,
                humidityPercent = 68,
                windSpeedKmh = 9.8,
                windDirection = "NW",
                uvIndex = 9,
                primaryAgriImpact = "Optimal window for foliar nutrient sprays & bio-pesticides across all fields.",
                primaryAgriImpactHi = "सभी खेतों में सूक्ष्म पोषक तत्व व कीटनाशक स्प्रे के लिए सर्वोत्तम दिन।",
                fertilizerStatus = AgriActivityStatus.OPTIMAL,
                sprayingStatus = AgriActivityStatus.OPTIMAL,
                irrigationStatus = AgriActivityStatus.MARGINAL,
                harvestingStatus = AgriActivityStatus.OPTIMAL,
                farmSuitabilityScore = 92
            ),
            DailyWeatherForecast(
                dayLabel = "Sunday",
                dateLabel = "23 Aug",
                tempMaxC = 34,
                tempMinC = 24,
                condition = "Warm & Sunny Skies",
                conditionType = WeatherConditionType.SUNNY,
                rainProbabilityPercent = 10,
                rainfallExpectedMm = 0.0,
                humidityPercent = 58,
                windSpeedKmh = 8.5,
                windDirection = "N",
                uvIndex = 10,
                primaryAgriImpact = "High temperature (34°C): Irrigation demand increases; schedule evening drip cycle.",
                primaryAgriImpactHi = "तापमान 34°C: सिंचाई की मांग बढ़ेगी; शाम को ड्रिप चलाएं।",
                fertilizerStatus = AgriActivityStatus.OPTIMAL,
                sprayingStatus = AgriActivityStatus.OPTIMAL,
                irrigationStatus = AgriActivityStatus.OPTIMAL,
                harvestingStatus = AgriActivityStatus.OPTIMAL,
                farmSuitabilityScore = 95
            ),
            DailyWeatherForecast(
                dayLabel = "Monday",
                dateLabel = "24 Aug",
                tempMaxC = 35,
                tempMinC = 24,
                condition = "Hot & Sunny",
                conditionType = WeatherConditionType.HOT_DRY,
                rainProbabilityPercent = 5,
                rainfallExpectedMm = 0.0,
                humidityPercent = 52,
                windSpeedKmh = 10.0,
                windDirection = "NE",
                uvIndex = 10,
                primaryAgriImpact = "High heat wave (35°C): Monitor crop for moisture stress and sucking pests.",
                primaryAgriImpactHi = "गर्मी (35°C): फसल में नमी की कमी व रस चूसक कीटों की जांच करें।",
                fertilizerStatus = AgriActivityStatus.OPTIMAL,
                sprayingStatus = AgriActivityStatus.MARGINAL,
                irrigationStatus = AgriActivityStatus.OPTIMAL,
                harvestingStatus = AgriActivityStatus.OPTIMAL,
                farmSuitabilityScore = 88
            ),
            DailyWeatherForecast(
                dayLabel = "Tuesday",
                dateLabel = "25 Aug",
                tempMaxC = 33,
                tempMinC = 23,
                condition = "Clear Weather",
                conditionType = WeatherConditionType.SUNNY,
                rainProbabilityPercent = 15,
                rainfallExpectedMm = 0.0,
                humidityPercent = 60,
                windSpeedKmh = 11.2,
                windDirection = "E",
                uvIndex = 9,
                primaryAgriImpact = "Ideal conditions for basal fertilizer application and farm logistics transport.",
                primaryAgriImpactHi = "खाद देने और मंडी तक उपज परिवहन के लिए अनुकूल मौसम।",
                fertilizerStatus = AgriActivityStatus.OPTIMAL,
                sprayingStatus = AgriActivityStatus.OPTIMAL,
                irrigationStatus = AgriActivityStatus.OPTIMAL,
                harvestingStatus = AgriActivityStatus.OPTIMAL,
                farmSuitabilityScore = 94
            )
        )
    }

    /**
     * Generates 7-Day strategic agronomic advisories.
     */
    fun evaluateWeeklyImpacts(): List<AgricultureImpactItem> {
        return listOf(
            AgricultureImpactItem(
                id = "week_impact_rain_pattern",
                category = AgriImpactCategory.FERTILIZER,
                title = "Monsoon Transition Pattern: Heavy Rain Wed/Thu → Dry Clear Weekend",
                titleHi = "साप्ताहिक मौसम चक्र: बुध/गुरु को भारी बारिश → सप्ताहांत में साफ मौसम",
                weatherTrigger = "Cumulative 7-day rainfall expected: ~43.5 mm concentrated in first 48 hours.",
                weatherTriggerHi = "अगले 7 दिनों में कुल 43.5 मिमी वर्षा, मुख्य रूप से पहले 2 दिनों में।",
                scientificRationale = "Nutrient leaching risks peak during Wed-Thu rainfall. Post-monsoon clearing over Saturday-Monday provides a 72-hour window of optimal nutrient uptake and dry canopy.",
                scientificRationaleHi = "बुध-गुरु को खाद न डालें। शनिवार से सोमवार तक 3 दिनों का सबसे अनुकूल समय रहेगा।",
                farmerAction = "Plan major Urea split and foliar booster applications for Saturday (22 Aug) and Sunday (23 Aug).",
                farmerActionHi = "यूरिया और टॉनिक देने की योजना शनिवार (22 अगस्त) और रविवार (23 अगस्त) के लिए बनाएं।",
                severity = ImpactSeverity.ADVISORY,
                affectedCrops = listOf("Soybean", "Cotton", "Tomato"),
                timingRecommendation = "Execution Window: Saturday 22 Aug – Monday 24 Aug",
                timingRecommendationHi = "कार्रवाई का समय: शनिवार 22 अगस्त से सोमवार 24 अगस्त"
            ),
            AgricultureImpactItem(
                id = "week_impact_disease_outlook",
                category = AgriImpactCategory.DISEASE_RISK,
                title = "Post-Rain Humidity Peak → High Disease Risk Alert for Soybean & Cotton",
                titleHi = "बारिश के बाद उच्च नमी → सोयाबीन व कपास में फफूंद व लीफ स्पॉट का अलर्ट",
                weatherTrigger = "Relative humidity staying above 75% for 4 consecutive days.",
                weatherTriggerHi = "लगातार 4 दिनों तक हवा में नमी 75% से अधिक रहने का अनुमान।",
                scientificRationale = "Extended high humidity followed by rising temperatures (32–34°C) creates a critical infection window for fungal pathogens like Rhizoctonia aerial blight and Cercospora.",
                scientificRationaleHi = "अधिक नमी के बाद तेज धूप निकलने से फफूंद जनित रोगों का फैलाव सबसे तेज होता है।",
                farmerAction = "Keep prophylactic bio-fungicide (Trichoderma viride @ 5g/L) or Carbendazim+Mancozeb ready for immediate application on Saturday morning.",
                farmerActionHi = "शनिवार सुबह के लिए जैविक फफूंदनाशक (ट्राइकोडर्मा) या मैंकोजेब का घोल तैयार रखें।",
                severity = ImpactSeverity.WARNING,
                affectedCrops = listOf("Soybean (JS-2034)", "Cotton", "Tomato"),
                timingRecommendation = "Prophylactic spray planned for Saturday morning",
                timingRecommendationHi = "शनिवार सुबह सुरक्षात्मक स्प्रे करें"
            ),
            AgricultureImpactItem(
                id = "week_impact_heat_irrigation",
                category = AgriImpactCategory.HEAT_STRESS,
                title = "Weekend Temperature Surge (34–35°C) → Increased Irrigation Demand",
                titleHi = "सप्ताहांत में तापमान वृद्धि (34–35°C) → सिंचाई की आवश्यकता में वृद्धि",
                weatherTrigger = "Temperatures rising from 29°C on Friday to 35°C on Monday.",
                weatherTriggerHi = "शुक्रवार (29°C) से सोमवार (35°C) तक तापमान में 6°C की बढ़ोतरी।",
                scientificRationale = "Sudden temperature spike triggers high transpiration rates. Plants without supplemental irrigation will suffer flower drop and reduced boll retention.",
                scientificRationaleHi = "अचानक गर्मी बढ़ने से फूलों का झड़ना और कपास में टिंडे गिरने का खतरा रहता है।",
                farmerAction = "Resume automated drip irrigation cycles starting Sunday evening (23 Aug) after ground drains.",
                farmerActionHi = "रविवार शाम से ड्रिप सिंचाई दोबारा शुरू करें ताकि फसल को पानी की कमी न हो।",
                severity = ImpactSeverity.ADVISORY,
                affectedCrops = listOf("Cotton", "Tomato"),
                timingRecommendation = "Sunday 23 Aug onwards",
                timingRecommendationHi = "रविवार 23 अगस्त से आगे"
            )
        )
    }

    /**
     * Generates a concise agronomic briefing summary in English and Hindi.
     */
    fun generateAdvisorySummary(state: WeatherIntelligenceUiState): Pair<String, String> {
        val rainHigh = state.rainProbabilityPercent >= 60 || state.rainfallExpectedMm >= 10.0
        val humidityHigh = state.humidityPercent >= 75
        val tempHigh = state.currentTempC >= 32.0
        val windHigh = state.windSpeedKmh >= 15.0

        val enSummary = buildString {
            append("Agronomic Intelligence Summary: ")
            if (rainHigh) {
                append("Heavy rainfall (${String.format("%.1f", state.rainfallExpectedMm)}mm, ${state.rainProbabilityPercent}%) expected today — avoid fertilizer top-dressing to prevent leaching losses, and hold irrigation. ")
            }
            if (humidityHigh) {
                append("Elevated humidity (${state.humidityPercent}%) increases fungal disease vulnerability; inspect leaf canopies. ")
            }
            if (tempHigh) {
                append("High temperature (${state.currentTempC.toInt()}°C) raises crop evapotranspiration; adjust evening drip schedule. ")
            }
            if (windHigh) {
                append("Wind speed at ${state.windSpeedKmh.toInt()} km/h exceeds safe foliar spray limits; postpone pesticide applications until calm weather.")
            }
            if (!rainHigh && !humidityHigh && !tempHigh && !windHigh) {
                append("Weather conditions are stable and favorable for scheduled field activities.")
            }
        }

        val hiSummary = buildString {
            append("कृषि मौसम सलाह: ")
            if (rainHigh) {
                append("आज भारी बारिश (${String.format("%.1f", state.rainfallExpectedMm)} मिमी, ${state.rainProbabilityPercent}%) की संभावना है — रासायनिक खाद (यूरिया/DAP) का छिड़काव रोकें ताकि खाद बहकर नष्ट न हो, और सिंचाई बंद रखें। ")
            }
            if (humidityHigh) {
                append("हवा में अधिक नमी (${state.humidityPercent}%) के कारण फफूंद व पत्ती धब्बा रोग का खतरा बढ़ सकता है; पत्तियों की जांच करें। ")
            }
            if (tempHigh) {
                append("अधिक तापमान (${state.currentTempC.toInt()}°C) से फसलों में पानी की मांग बढ़ेगी; शाम को ड्रिप सिंचाई करें। ")
            }
            if (windHigh) {
                append("हवा की गति (${state.windSpeedKmh.toInt()} किमी/घंटे) तेज होने के कारण आज कीटनाशक का छिड़काव न करें।")
            }
            if (!rainHigh && !humidityHigh && !tempHigh && !windHigh) {
                append("मौसम अनुकूल और शांत है; सभी कृषि कार्य सामान्य रूप से किए जा सकते हैं।")
            }
        }

        return Pair(enSummary, hiSummary)
    }
}

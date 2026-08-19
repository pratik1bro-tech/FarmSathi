package com.example.data.repository

import com.example.data.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

interface SoilHealthRepository {
    fun getAllFieldSoilReports(): Flow<List<FieldSoilReport>>
    fun getSoilReportForField(fieldId: String): Flow<FieldSoilReport?>
    suspend fun getHistoricalSoilData(fieldId: String, nutrientId: String, timeRange: String): List<SoilHistoryDataPoint>
}

class DevelopmentSoilHealthRepository : SoilHealthRepository {

    private val _reports = MutableStateFlow(
        listOf(
            FieldSoilReport(
                fieldId = "field_2",
                fieldName = "Field 2: South Cotton Block",
                crop = "Cotton (BT-RCH-659)",
                areaAcres = 3.2,
                soilType = "Deep Black Clay Loam (Vertisol)",
                healthScore = 72,
                healthGrade = "Grade B • Moderate Fertility",
                sampleDate = "15 Aug 2026",
                nitrogen = SoilNutrientMetric(
                    id = "nitrogen",
                    name = "Nitrogen (N)",
                    hindiName = "नाइट्रोजन (N)",
                    currentValue = 142.0,
                    unit = "mg/kg",
                    indicator = NutrientIndicator.OPTIMAL,
                    minOptimal = 120.0,
                    maxOptimal = 180.0,
                    statusMessage = "Adequate for vegetative branch growth"
                ),
                phosphorus = SoilNutrientMetric(
                    id = "phosphorus",
                    name = "Phosphorus (P)",
                    hindiName = "फास्फोरस (P)",
                    currentValue = 18.2,
                    unit = "mg/kg",
                    indicator = NutrientIndicator.LOW,
                    minOptimal = 25.0,
                    maxOptimal = 40.0,
                    statusMessage = "Deficit: Critical for flowering & root establishment"
                ),
                potassium = SoilNutrientMetric(
                    id = "potassium",
                    name = "Potassium (K)",
                    hindiName = "पोटेशियम (K)",
                    currentValue = 210.0,
                    unit = "mg/kg",
                    indicator = NutrientIndicator.OPTIMAL,
                    minOptimal = 180.0,
                    maxOptimal = 250.0,
                    statusMessage = "Optimal for boll formation and fiber strength"
                ),
                soilPh = SoilNutrientMetric(
                    id = "ph",
                    name = "Soil pH",
                    hindiName = "मिट्टी का पीएच",
                    currentValue = 6.8,
                    unit = "pH",
                    indicator = NutrientIndicator.OPTIMAL,
                    minOptimal = 6.5,
                    maxOptimal = 7.5,
                    statusMessage = "Ideal neutral range for nutrient bioavailability"
                ),
                moisture = SoilNutrientMetric(
                    id = "moisture",
                    name = "Soil Moisture",
                    hindiName = "मिट्टी की नमी",
                    currentValue = 38.0,
                    unit = "%",
                    indicator = NutrientIndicator.LOW,
                    minOptimal = 45.0,
                    maxOptimal = 65.0,
                    statusMessage = "Moisture deficit: Evening drip irrigation advised"
                ),
                organicCarbon = SoilNutrientMetric(
                    id = "organic_carbon",
                    name = "Organic Carbon",
                    hindiName = "जैविक कार्बन (OC)",
                    currentValue = 0.54,
                    unit = "%",
                    indicator = NutrientIndicator.OPTIMAL,
                    minOptimal = 0.50,
                    maxOptimal = 0.75,
                    statusMessage = "Medium carbon content; maintain with bio-humus"
                ),
                recommendation = SoilRecommendation(
                    whatIsWrong = "Available soil Phosphorus (18.2 mg/kg) and root-zone moisture (38%) are below critical thresholds for cotton flowering stage.",
                    why = "High clay binding capacity in Vertisol soils combined with prior harvest nutrient extraction has locked soluble phosphate into insoluble compounds.",
                    whatShouldFarmerDo = "1. Apply Single Super Phosphate (SSP) or Rock Phosphate combined with well-decomposed FYM/compost.\n2. Inoculate root-zone with Phosphate Solubilizing Bacteria (PSB) / Mycorrhiza to mobilize fixed phosphate.\n3. Execute evening drip irrigation to restore root-zone moisture to 55%.",
                    priority = "HIGH PRIORITY",
                    dosageValidationNote = "Nutrient guidance is validated against ICAR and state agricultural university soil fertility recommendations."
                )
            ),
            FieldSoilReport(
                fieldId = "field_1",
                fieldName = "Field 1: Soybean Main Block",
                crop = "Soybean (JS-2034)",
                areaAcres = 4.5,
                soilType = "Medium Black Malwa Loam",
                healthScore = 84,
                healthGrade = "Grade A • Highly Productive",
                sampleDate = "10 Aug 2026",
                nitrogen = SoilNutrientMetric(
                    id = "nitrogen",
                    name = "Nitrogen (N)",
                    hindiName = "नाइट्रोजन (N)",
                    currentValue = 165.0,
                    unit = "mg/kg",
                    indicator = NutrientIndicator.OPTIMAL,
                    minOptimal = 120.0,
                    maxOptimal = 180.0,
                    statusMessage = "Active Rhizobium nodulation maintaining nitrogen balance"
                ),
                phosphorus = SoilNutrientMetric(
                    id = "phosphorus",
                    name = "Phosphorus (P)",
                    hindiName = "फास्फोरस (P)",
                    currentValue = 32.0,
                    unit = "mg/kg",
                    indicator = NutrientIndicator.OPTIMAL,
                    minOptimal = 25.0,
                    maxOptimal = 40.0,
                    statusMessage = "Optimal pod development reserve"
                ),
                potassium = SoilNutrientMetric(
                    id = "potassium",
                    name = "Potassium (K)",
                    hindiName = "पोटेशियम (K)",
                    currentValue = 225.0,
                    unit = "mg/kg",
                    indicator = NutrientIndicator.OPTIMAL,
                    minOptimal = 180.0,
                    maxOptimal = 250.0,
                    statusMessage = "Excellent disease resistance and grain filling support"
                ),
                soilPh = SoilNutrientMetric(
                    id = "ph",
                    name = "Soil pH",
                    hindiName = "मिट्टी का पीएच",
                    currentValue = 6.9,
                    unit = "pH",
                    indicator = NutrientIndicator.OPTIMAL,
                    minOptimal = 6.5,
                    maxOptimal = 7.5,
                    statusMessage = "Balanced neutral pH"
                ),
                moisture = SoilNutrientMetric(
                    id = "moisture",
                    name = "Soil Moisture",
                    hindiName = "मिट्टी की नमी",
                    currentValue = 52.0,
                    unit = "%",
                    indicator = NutrientIndicator.OPTIMAL,
                    minOptimal = 45.0,
                    maxOptimal = 65.0,
                    statusMessage = "Ideal moisture for pod filling"
                ),
                organicCarbon = SoilNutrientMetric(
                    id = "organic_carbon",
                    name = "Organic Carbon",
                    hindiName = "जैविक कार्बन (OC)",
                    currentValue = 0.68,
                    unit = "%",
                    indicator = NutrientIndicator.OPTIMAL,
                    minOptimal = 0.50,
                    maxOptimal = 0.75,
                    statusMessage = "Rich organic humus layer"
                ),
                recommendation = SoilRecommendation(
                    whatIsWrong = "All primary macronutrients and moisture are in the optimal target range. No acute nutrient deficiencies detected.",
                    why = "Balanced crop rotation with legumes and timely organic compost application has sustained soil microbial biomass.",
                    whatShouldFarmerDo = "Continue scheduled routine moisture monitoring. No additional chemical top-dressing needed for this vegetative cycle.",
                    priority = "ADVISORY"
                )
            ),
            FieldSoilReport(
                fieldId = "field_3",
                fieldName = "Field 3: North Wheat & Gram Block",
                crop = "Chickpea / Gram (JG-16)",
                areaAcres = 2.8,
                soilType = "Sandy Clay Loam",
                healthScore = 88,
                healthGrade = "Grade A • Optimal Condition",
                sampleDate = "05 Aug 2026",
                nitrogen = SoilNutrientMetric(
                    id = "nitrogen",
                    name = "Nitrogen (N)",
                    hindiName = "नाइट्रोजन (N)",
                    currentValue = 178.0,
                    unit = "mg/kg",
                    indicator = NutrientIndicator.OPTIMAL,
                    minOptimal = 120.0,
                    maxOptimal = 180.0,
                    statusMessage = "Rich biological nitrogen fixing bed"
                ),
                phosphorus = SoilNutrientMetric(
                    id = "phosphorus",
                    name = "Phosphorus (P)",
                    hindiName = "फास्फोरस (P)",
                    currentValue = 28.0,
                    unit = "mg/kg",
                    indicator = NutrientIndicator.OPTIMAL,
                    minOptimal = 25.0,
                    maxOptimal = 40.0,
                    statusMessage = "Optimal for root branching"
                ),
                potassium = SoilNutrientMetric(
                    id = "potassium",
                    name = "Potassium (K)",
                    hindiName = "पोटेशियम (K)",
                    currentValue = 240.0,
                    unit = "mg/kg",
                    indicator = NutrientIndicator.OPTIMAL,
                    minOptimal = 180.0,
                    maxOptimal = 250.0,
                    statusMessage = "High potassium reserve"
                ),
                soilPh = SoilNutrientMetric(
                    id = "ph",
                    name = "Soil pH",
                    hindiName = "मिट्टी का पीएच",
                    currentValue = 7.2,
                    unit = "pH",
                    indicator = NutrientIndicator.OPTIMAL,
                    minOptimal = 6.5,
                    maxOptimal = 7.5,
                    statusMessage = "Slightly alkaline ideal for gram and wheat"
                ),
                moisture = SoilNutrientMetric(
                    id = "moisture",
                    name = "Soil Moisture",
                    hindiName = "मिट्टी की नमी",
                    currentValue = 48.0,
                    unit = "%",
                    indicator = NutrientIndicator.OPTIMAL,
                    minOptimal = 45.0,
                    maxOptimal = 65.0,
                    statusMessage = "Well balanced soil water matrix"
                ),
                organicCarbon = SoilNutrientMetric(
                    id = "organic_carbon",
                    name = "Organic Carbon",
                    hindiName = "जैविक कार्बन (OC)",
                    currentValue = 0.72,
                    unit = "%",
                    indicator = NutrientIndicator.OPTIMAL,
                    minOptimal = 0.50,
                    maxOptimal = 0.75,
                    statusMessage = "High humus and microbial enzyme activity"
                ),
                recommendation = SoilRecommendation(
                    whatIsWrong = "Soil health is excellent. Maintain protective mulching across field rows.",
                    why = "Favorable soil microbial structure and balanced organic carbon levels.",
                    whatShouldFarmerDo = "Prepare seedbed with minimum tillage to preserve soil moisture and beneficial earthworm tunnels.",
                    priority = "ADVISORY"
                )
            )
        )
    )

    override fun getAllFieldSoilReports(): Flow<List<FieldSoilReport>> = _reports.asStateFlow()

    override fun getSoilReportForField(fieldId: String): Flow<FieldSoilReport?> {
        return _reports.map { list -> list.find { it.fieldId == fieldId } ?: list.firstOrNull() }
    }

    override suspend fun getHistoricalSoilData(
        fieldId: String,
        nutrientId: String,
        timeRange: String
    ): List<SoilHistoryDataPoint> {
        delay(150)
        return when (nutrientId) {
            "phosphorus" -> listOf(
                SoilHistoryDataPoint("May", 26.0),
                SoilHistoryDataPoint("Jun", 24.5),
                SoilHistoryDataPoint("Jul", 21.0),
                SoilHistoryDataPoint("Aug", 18.2)
            )
            "nitrogen" -> listOf(
                SoilHistoryDataPoint("May", 155.0),
                SoilHistoryDataPoint("Jun", 150.0),
                SoilHistoryDataPoint("Jul", 146.0),
                SoilHistoryDataPoint("Aug", 142.0)
            )
            "potassium" -> listOf(
                SoilHistoryDataPoint("May", 215.0),
                SoilHistoryDataPoint("Jun", 212.0),
                SoilHistoryDataPoint("Jul", 210.0),
                SoilHistoryDataPoint("Aug", 210.0)
            )
            "ph" -> listOf(
                SoilHistoryDataPoint("May", 6.9),
                SoilHistoryDataPoint("Jun", 6.8),
                SoilHistoryDataPoint("Jul", 6.8),
                SoilHistoryDataPoint("Aug", 6.8)
            )
            "moisture" -> listOf(
                SoilHistoryDataPoint("May", 22.0),
                SoilHistoryDataPoint("Jun", 46.0),
                SoilHistoryDataPoint("Jul", 55.0),
                SoilHistoryDataPoint("Aug", 38.0)
            )
            else -> listOf(
                SoilHistoryDataPoint("May", 0.50),
                SoilHistoryDataPoint("Jun", 0.52),
                SoilHistoryDataPoint("Jul", 0.53),
                SoilHistoryDataPoint("Aug", 0.54)
            )
        }
    }
}

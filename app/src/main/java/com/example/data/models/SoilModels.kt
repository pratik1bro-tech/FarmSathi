package com.example.data.models

enum class NutrientIndicator(
    val label: String,
    val hindiLabel: String
) {
    LOW("LOW", "कम"),
    OPTIMAL("OPTIMAL", "संतुलित"),
    HIGH("HIGH", "अधिक")
}

data class SoilNutrientMetric(
    val id: String,
    val name: String,
    val hindiName: String,
    val currentValue: Double,
    val unit: String,
    val indicator: NutrientIndicator,
    val minOptimal: Double,
    val maxOptimal: Double,
    val statusMessage: String
)

data class SoilRecommendation(
    val whatIsWrong: String,
    val why: String,
    val whatShouldFarmerDo: String,
    val priority: String, // "HIGH PRIORITY", "MEDIUM PRIORITY", "ADVISORY"
    val dosageValidationNote: String = "All agronomic recommendations adhere to ICAR soil health protocol and state university crop fertility schedules."
)

data class SoilHistoryDataPoint(
    val label: String,
    val value: Double
)

data class FieldSoilReport(
    val fieldId: String,
    val fieldName: String,
    val crop: String,
    val areaAcres: Double,
    val soilType: String,
    val healthScore: Int, // 0 - 100
    val healthGrade: String,
    val sampleDate: String,
    val nitrogen: SoilNutrientMetric,
    val phosphorus: SoilNutrientMetric,
    val potassium: SoilNutrientMetric,
    val soilPh: SoilNutrientMetric,
    val moisture: SoilNutrientMetric,
    val organicCarbon: SoilNutrientMetric,
    val recommendation: SoilRecommendation
)

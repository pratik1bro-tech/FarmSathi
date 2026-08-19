package com.example.data.models

enum class CropType(
    val englishName: String,
    val hindiName: String,
    val emoji: String
) {
    SOYBEAN("Soybean", "सोयाबीन", "🌱"),
    COTTON("Cotton", "कपास", "🌾"),
    WHEAT("Wheat", "गेहूं", "🌾"),
    TOMATO("Tomato", "टमाटर", "🍅"),
    CHICKPEA("Chickpea (Gram)", "चना", "🫘"),
    MAIZE("Maize (Corn)", "मक्का", "🌽")
}

data class PredictionItem(
    val diseaseName: String,
    val confidencePercent: Int,
    val isTop: Boolean = false
)

data class CropDiseaseResult(
    val id: String,
    val cropName: String,
    val diseaseName: String,
    val pathogenScientificName: String? = null,
    val confidence: Int, // e.g. 94%
    val riskSeverity: String? = null, // "Low", "Medium", "High", "Critical", "Healthy"
    val topPredictions: List<PredictionItem> = emptyList(),
    val recommendation: String = "Inspect surrounding leaves and follow the agronomic treatment guidance provided by the backend.",
    val organicRemedy: String,
    val chemicalTreatment: String,
    val preventiveProtocol: String,
    val symptoms: List<String> = emptyList(),
    val imageUri: String? = null,
    val timestamp: String
)

sealed class DiseaseScanUiState {
    object CaptureMode : DiseaseScanUiState()
    data class ImagePreview(val imageUri: String?, val samplePresetName: String? = null) : DiseaseScanUiState()
    data class UploadingAndAnalyzing(
        val progressPercent: Int,
        val currentStageText: String,
        val imageUri: String?
    ) : DiseaseScanUiState()
    data class ScanSuccess(val result: CropDiseaseResult) : DiseaseScanUiState()
    data class ScanError(val message: String, val canRetry: Boolean = true) : DiseaseScanUiState()
}

package com.example.features.disease_detection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.CropDiseaseResult
import com.example.data.models.CropType
import com.example.data.models.DiseaseScanUiState
import com.example.data.repository.DevelopmentDiseaseDetectionRepository
import com.example.data.repository.DiseaseDetectionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DiseaseDetectionScreenUiState(
    val selectedCrop: CropType = CropType.SOYBEAN,
    val scanState: DiseaseScanUiState = DiseaseScanUiState.CaptureMode,
    val selectedImageUri: String? = null,
    val selectedPresetKey: String? = null,
    val recentScans: List<CropDiseaseResult> = emptyList(),
    val hasCameraPermission: Boolean = false,
    val flashEnabled: Boolean = false
)

class DiseaseDetectionViewModel(
    private val repository: DiseaseDetectionRepository = DevelopmentDiseaseDetectionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiseaseDetectionScreenUiState())
    val uiState: StateFlow<DiseaseDetectionScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getRecentScans().collect { scans ->
                _uiState.update { it.copy(recentScans = scans) }
            }
        }
    }

    fun selectCrop(crop: CropType) {
        _uiState.update { it.copy(selectedCrop = crop) }
    }

    fun setCameraPermission(isGranted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = isGranted) }
    }

    fun toggleFlash() {
        _uiState.update { it.copy(flashEnabled = !it.flashEnabled) }
    }

    fun onImageSelected(imageUri: String?, presetKey: String? = null, sampleLabel: String? = null) {
        _uiState.update {
            it.copy(
                selectedImageUri = imageUri,
                selectedPresetKey = presetKey,
                scanState = DiseaseScanUiState.ImagePreview(imageUri, sampleLabel)
            )
        }
    }

    fun capturePhoto() {
        // Simulates taking photo in viewfinder
        val dummyUri = "content://media/external/images/media/leaf_${System.currentTimeMillis()}"
        val preset = when (_uiState.value.selectedCrop) {
            CropType.SOYBEAN -> "soybean_rust"
            CropType.COTTON -> "cotton_curl"
            CropType.TOMATO -> null
            CropType.WHEAT -> null
            else -> null
        }
        onImageSelected(dummyUri, preset, "${_uiState.value.selectedCrop.englishName} Leaf Photo")
    }

    fun startAnalysis() {
        val current = _uiState.value
        _uiState.update {
            it.copy(
                scanState = DiseaseScanUiState.UploadingAndAnalyzing(
                    progressPercent = 0,
                    currentStageText = "Connecting to secure agronomist engine...",
                    imageUri = current.selectedImageUri
                )
            )
        }

        viewModelScope.launch {
            val result = repository.analyzeCropLeaf(
                imageUri = current.selectedImageUri,
                cropType = current.selectedCrop,
                presetKey = current.selectedPresetKey,
                onProgressUpdate = { progress, stage ->
                    _uiState.update {
                        it.copy(
                            scanState = DiseaseScanUiState.UploadingAndAnalyzing(
                                progressPercent = progress,
                                currentStageText = stage,
                                imageUri = current.selectedImageUri
                            )
                        )
                    }
                }
            )

            result.onSuccess { diseaseResult ->
                _uiState.update {
                    it.copy(scanState = DiseaseScanUiState.ScanSuccess(diseaseResult))
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        scanState = DiseaseScanUiState.ScanError(
                            message = error.localizedMessage ?: "Analysis failed. Please check connection and retry.",
                            canRetry = true
                        )
                    )
                }
            }
        }
    }

    fun resetToCaptureMode() {
        _uiState.update {
            it.copy(
                scanState = DiseaseScanUiState.CaptureMode,
                selectedImageUri = null,
                selectedPresetKey = null
            )
        }
    }

    fun viewPastScan(scan: CropDiseaseResult) {
        _uiState.update {
            it.copy(scanState = DiseaseScanUiState.ScanSuccess(scan))
        }
    }
}

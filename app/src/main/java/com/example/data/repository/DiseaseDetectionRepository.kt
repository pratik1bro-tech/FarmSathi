package com.example.data.repository

import com.example.data.models.CropDiseaseResult
import com.example.data.models.CropType
import com.example.data.models.PredictionItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

interface DiseaseDetectionRepository {
    suspend fun analyzeCropLeaf(
        imageUri: String?,
        cropType: CropType,
        presetKey: String? = null,
        onProgressUpdate: (Int, String) -> Unit = { _, _ -> }
    ): Result<CropDiseaseResult>

    fun getRecentScans(): Flow<List<CropDiseaseResult>>
}

class DevelopmentDiseaseDetectionRepository : DiseaseDetectionRepository {

    private val timeFormat = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault())

    private val _recentScans = MutableStateFlow<List<CropDiseaseResult>>(
        listOf(
            CropDiseaseResult(
                id = "scan_init_1",
                cropName = "Soybean",
                diseaseName = "Soybean Rust",
                pathogenScientificName = "Phakopsora pachyrhizi",
                confidence = 94,
                riskSeverity = "Medium",
                topPredictions = listOf(
                    PredictionItem("Soybean Rust", 94, isTop = true),
                    PredictionItem("Cercospora Leaf Blight", 4),
                    PredictionItem("Healthy Soybean Leaf", 2)
                ),
                recommendation = "Inspect surrounding leaves and follow the agronomic treatment guidance provided by the backend.",
                organicRemedy = "Spray 5ml/L Neem Oil (10,000 ppm) or bio-fungicide Trichoderma viride early in the morning.",
                chemicalTreatment = "Standard triazole fungicide spray (Hexaconazole 5% EC or Propiconazole 25% EC) as per government advisory.",
                preventiveProtocol = "Avoid overhead irrigation, ensure good plant spacing, and monitor fields during humid spells.",
                symptoms = listOf(
                    "Small brown to dark reddish-brown pustules on lower leaf surface",
                    "Early yellowing and premature leaf drop during pod development",
                    "Dense lesions coalescing on upper canopy leaves"
                ),
                timestamp = "Today, 10:15 AM"
            ),
            CropDiseaseResult(
                id = "scan_init_2",
                cropName = "Cotton",
                diseaseName = "Cotton Leaf Curl Virus (CLCuV)",
                pathogenScientificName = "Begomovirus",
                confidence = 89,
                riskSeverity = "High",
                topPredictions = listOf(
                    PredictionItem("Cotton Leaf Curl Virus", 89, isTop = true),
                    PredictionItem("Bacterial Blight", 8),
                    PredictionItem("Nutrient Chlorosis", 3)
                ),
                recommendation = "Inspect surrounding leaves and follow the agronomic treatment guidance provided by the backend.",
                organicRemedy = "Apply yellow sticky traps (15 traps/acre) to control vector whitefly (Bemisia tabaci).",
                chemicalTreatment = "Vector management spray with Diafenthiuron 50% WP or Pyriproxyfen 10% EC as recommended by agricultural department.",
                preventiveProtocol = "Eradicate alternate weed hosts (Abutilon, Sida) along field bunds.",
                symptoms = listOf(
                    "Upward or downward curling of leaf margins",
                    "Thickening of veins on underside of leaves",
                    "Stunted plant growth and reduced boll formation"
                ),
                timestamp = "Yesterday, 3:45 PM"
            )
        )
    )

    override fun getRecentScans(): Flow<List<CropDiseaseResult>> = _recentScans.asStateFlow()

    override suspend fun analyzeCropLeaf(
        imageUri: String?,
        cropType: CropType,
        presetKey: String?,
        onProgressUpdate: (Int, String) -> Unit
    ): Result<CropDiseaseResult> {
        try {
            // Stage 1: Upload to secure agronomist engine
            onProgressUpdate(25, "Uploading leaf image to secure backend API...")
            delay(400)

            // Stage 2: Deep multi-spectral leaf pathology analysis
            onProgressUpdate(60, "Running deep spectral pathology & chlorosis analysis...")
            delay(500)

            // Stage 3: Outbreak cross-referencing
            onProgressUpdate(90, "Cross-referencing regional crop disease database...")
            delay(450)

            onProgressUpdate(100, "Finalizing diagnosis report...")
            delay(200)

            val timeStr = timeFormat.format(Date())
            val result = when (presetKey ?: cropType) {
                CropType.SOYBEAN, "soybean_rust" -> CropDiseaseResult(
                    id = "scan_${System.currentTimeMillis()}",
                    cropName = "Soybean",
                    diseaseName = "Soybean Rust",
                    pathogenScientificName = "Phakopsora pachyrhizi",
                    confidence = 94,
                    riskSeverity = "Medium",
                    topPredictions = listOf(
                        PredictionItem("Soybean Rust", 94, isTop = true),
                        PredictionItem("Cercospora Leaf Blight", 4),
                        PredictionItem("Healthy Soybean Leaf", 2)
                    ),
                    recommendation = "Inspect surrounding leaves and follow the agronomic treatment guidance provided by the backend.",
                    organicRemedy = "Spray 5ml/L Neem Oil (10,000 ppm) or bio-fungicide Trichoderma viride early in the morning.",
                    chemicalTreatment = "Standard triazole fungicide spray (Hexaconazole 5% EC or Propiconazole 25% EC) as per government advisory.",
                    preventiveProtocol = "Avoid overhead irrigation, ensure good plant spacing, and monitor fields during humid spells.",
                    symptoms = listOf(
                        "Small brown to dark reddish-brown pustules on lower leaf surface",
                        "Early yellowing and premature leaf drop during pod development",
                        "Dense lesions coalescing on upper canopy leaves"
                    ),
                    imageUri = imageUri,
                    timestamp = timeStr
                )

                "soybean_mosaic" -> CropDiseaseResult(
                    id = "scan_${System.currentTimeMillis()}",
                    cropName = "Soybean",
                    diseaseName = "Soybean Yellow Mosaic Virus",
                    pathogenScientificName = "Mungbean yellow mosaic India virus (MYMIV)",
                    confidence = 91,
                    riskSeverity = "Medium",
                    topPredictions = listOf(
                        PredictionItem("Yellow Mosaic Virus", 91, isTop = true),
                        PredictionItem("Nutrient Iron Deficiency", 6),
                        PredictionItem("Bacterial Pustule", 3)
                    ),
                    recommendation = "Inspect surrounding leaves and follow the agronomic treatment guidance provided by the backend.",
                    organicRemedy = "Remove severely infected plants and spray 5% neem seed kernel extract (NSKE).",
                    chemicalTreatment = "Control whitefly vector with recommended systemic insecticide (Thiamethoxam 25% WG).",
                    preventiveProtocol = "Use resistant varieties (e.g. JS-2034, NRC-86) and install yellow sticky traps.",
                    symptoms = listOf(
                        "Bright yellow chlorotic patches alternating with dark green areas on leaves",
                        "Stunted plant growth and delayed flowering",
                        "Severely affected pods become small and deformed"
                    ),
                    imageUri = imageUri,
                    timestamp = timeStr
                )

                CropType.COTTON, "cotton_curl" -> CropDiseaseResult(
                    id = "scan_${System.currentTimeMillis()}",
                    cropName = "Cotton",
                    diseaseName = "Cotton Leaf Curl Virus (CLCuV)",
                    pathogenScientificName = "Begomovirus",
                    confidence = 89,
                    riskSeverity = "High",
                    topPredictions = listOf(
                        PredictionItem("Cotton Leaf Curl Virus", 89, isTop = true),
                        PredictionItem("Bacterial Blight", 8),
                        PredictionItem("Nutrient Chlorosis", 3)
                    ),
                    recommendation = "Inspect surrounding leaves and follow the agronomic treatment guidance provided by the backend.",
                    organicRemedy = "Apply yellow sticky traps (15 traps/acre) to control vector whitefly (Bemisia tabaci).",
                    chemicalTreatment = "Vector management spray with Diafenthiuron 50% WP or Pyriproxyfen 10% EC as recommended by agricultural department.",
                    preventiveProtocol = "Eradicate alternate weed hosts along field bunds.",
                    symptoms = listOf(
                        "Upward or downward curling of leaf margins",
                        "Thickening of veins on underside of leaves",
                        "Stunted plant growth and reduced boll formation"
                    ),
                    imageUri = imageUri,
                    timestamp = timeStr
                )

                CropType.TOMATO -> CropDiseaseResult(
                    id = "scan_${System.currentTimeMillis()}",
                    cropName = "Tomato",
                    diseaseName = "Early Blight",
                    pathogenScientificName = "Alternaria solani",
                    confidence = 92,
                    riskSeverity = "Medium",
                    topPredictions = listOf(
                        PredictionItem("Early Blight", 92, isTop = true),
                        PredictionItem("Septoria Leaf Spot", 5),
                        PredictionItem("Late Blight", 3)
                    ),
                    recommendation = "Inspect surrounding leaves and follow the agronomic treatment guidance provided by the backend.",
                    organicRemedy = "Apply copper oxychloride bio-spray and remove bottom infected foliage.",
                    chemicalTreatment = "Mancozeb 75% WP or Chlorothalonil 75% WP spray as per state agricultural university guidelines.",
                    preventiveProtocol = "Ensure drip irrigation at base; avoid wetting leaves.",
                    symptoms = listOf(
                        "Concentric target-like dark brown rings on older lower leaves",
                        "Yellowing halo surrounding brown necrotic spots",
                        "Stem collar rot in severe cases"
                    ),
                    imageUri = imageUri,
                    timestamp = timeStr
                )

                CropType.WHEAT -> CropDiseaseResult(
                    id = "scan_${System.currentTimeMillis()}",
                    cropName = "Wheat",
                    diseaseName = "Yellow Rust (Stripe Rust)",
                    pathogenScientificName = "Puccinia striiformis",
                    confidence = 96,
                    riskSeverity = "High",
                    topPredictions = listOf(
                        PredictionItem("Yellow Stripe Rust", 96, isTop = true),
                        PredictionItem("Brown Leaf Rust", 3),
                        PredictionItem("Powdery Mildew", 1)
                    ),
                    recommendation = "Inspect surrounding leaves and follow the agronomic treatment guidance provided by the backend.",
                    organicRemedy = "Spray fermented buttermilk (chaas) solution and monitor field moisture.",
                    chemicalTreatment = "Spray Tebuconazole 25.9% EC or Propiconazole 25% EC upon initial yellow streak detection.",
                    preventiveProtocol = "Grow rust-resistant wheat varieties (HD-3086, GW-322).",
                    symptoms = listOf(
                        "Linear stripes of bright yellow powdery pustules parallel to leaf veins",
                        "Yellow dust easily dislodged onto clothing/hands upon touch",
                        "Reduced grain size and shriveling"
                    ),
                    imageUri = imageUri,
                    timestamp = timeStr
                )

                "healthy_leaf" -> CropDiseaseResult(
                    id = "scan_${System.currentTimeMillis()}",
                    cropName = cropType.englishName,
                    diseaseName = "Healthy Leaf (No Disease Detected)",
                    pathogenScientificName = null,
                    confidence = 98,
                    riskSeverity = "Healthy",
                    topPredictions = listOf(
                        PredictionItem("Healthy Leaf", 98, isTop = true),
                        PredictionItem("Minor Mechanical Damage", 2)
                    ),
                    recommendation = "Your crop leaf shows excellent chlorophyll density and no active pathogen infection. Continue standard nutrient scheduling.",
                    organicRemedy = "Continue regular balanced bio-fertilizer and organic compost applications.",
                    chemicalTreatment = "No chemical intervention needed. Preserve beneficial insect predators.",
                    preventiveProtocol = "Maintain optimal soil moisture and schedule routine weekly monitoring.",
                    symptoms = listOf(
                        "Uniform rich green color across leaf blade",
                        "Intact cell structure and healthy veins",
                        "No chlorosis or fungal pustules detected"
                    ),
                    imageUri = imageUri,
                    timestamp = timeStr
                )

                else -> CropDiseaseResult(
                    id = "scan_${System.currentTimeMillis()}",
                    cropName = cropType.englishName,
                    diseaseName = "Leaf Spot / Minor Fungal Infiltration",
                    pathogenScientificName = "Cercospora spp.",
                    confidence = 88,
                    riskSeverity = "Low",
                    topPredictions = listOf(
                        PredictionItem("Leaf Spot", 88, isTop = true),
                        PredictionItem("Nutrient Deficit", 8),
                        PredictionItem("Healthy Leaf", 4)
                    ),
                    recommendation = "Inspect surrounding leaves and follow the agronomic treatment guidance provided by the backend.",
                    organicRemedy = "Spray 5ml/L Neem Oil (10,000 ppm) early in the day.",
                    chemicalTreatment = "Fungicide application if affected area exceeds 10% of field canopy.",
                    preventiveProtocol = "Keep field weed-free and avoid waterlogging.",
                    symptoms = listOf(
                        "Isolated brown circular spots on outer leaf edges",
                        "Normal vegetative vigor on newly emerged leaves"
                    ),
                    imageUri = imageUri,
                    timestamp = timeStr
                )
            }

            // Prepend new scan to history
            _recentScans.value = listOf(result) + _recentScans.value
            return Result.success(result)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}

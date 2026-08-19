package com.example.features.disease_detection

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.CropDiseaseResult
import com.example.data.models.CropType
import com.example.data.models.DiseaseScanUiState
import com.example.data.models.PredictionItem
import com.example.data.repository.FarmRepository
import com.example.designsystem.theme.FarmSathiDesign
import com.example.shared.components.FarmSathiTopBar
import com.example.ui.theme.*

@Composable
fun DiseaseDetectionScreen(
    repository: FarmRepository,
    onBack: () -> Unit,
    onNavigateToAi: (() -> Unit)? = null,
    viewModel: DiseaseDetectionViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Camera runtime permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setCameraPermission(isGranted)
        if (isGranted) {
            viewModel.capturePhoto()
        }
    }

    // Gallery upload launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onImageSelected(uri.toString(), null, "Uploaded Leaf Photo")
        }
    }

    val requestCameraAndCapture: () -> Unit = {
        val check = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (check == PackageManager.PERMISSION_GRANTED) {
            viewModel.setCameraPermission(true)
            viewModel.capturePhoto()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            FarmSathiTopBar(
                title = "AI Crop Doctor",
                subtitle = "Leaf Pathology & Disease Diagnostic",
                showBackButton = true,
                onBackClick = {
                    if (state.scanState !is DiseaseScanUiState.CaptureMode) {
                        viewModel.resetToCaptureMode()
                    } else {
                        onBack()
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val scanState = state.scanState) {
                is DiseaseScanUiState.CaptureMode -> {
                    CaptureModeView(
                        selectedCrop = state.selectedCrop,
                        flashEnabled = state.flashEnabled,
                        recentScans = state.recentScans,
                        onSelectCrop = { viewModel.selectCrop(it) },
                        onToggleFlash = { viewModel.toggleFlash() },
                        onCapture = requestCameraAndCapture,
                        onOpenGallery = { galleryLauncher.launch("image/*") },
                        onSelectPreset = { uri, preset, label ->
                            viewModel.onImageSelected(uri, preset, label)
                        },
                        onViewPastScan = { viewModel.viewPastScan(it) }
                    )
                }

                is DiseaseScanUiState.ImagePreview -> {
                    ImagePreviewView(
                        imageUri = scanState.imageUri,
                        sampleLabel = scanState.samplePresetName,
                        selectedCrop = state.selectedCrop,
                        onRetake = { viewModel.resetToCaptureMode() },
                        onAnalyze = { viewModel.startAnalysis() }
                    )
                }

                is DiseaseScanUiState.UploadingAndAnalyzing -> {
                    UploadingAndAnalyzingView(
                        progress = scanState.progressPercent,
                        stageText = scanState.currentStageText,
                        imageUri = scanState.imageUri,
                        selectedCrop = state.selectedCrop
                    )
                }

                is DiseaseScanUiState.ScanSuccess -> {
                    DiseaseResultView(
                        result = scanState.result,
                        onScanAgain = { viewModel.resetToCaptureMode() },
                        onAskAi = { onNavigateToAi?.invoke() ?: onBack() }
                    )
                }

                is DiseaseScanUiState.ScanError -> {
                    ScanErrorView(
                        message = scanState.message,
                        canRetry = scanState.canRetry,
                        onRetry = { viewModel.startAnalysis() },
                        onCancel = { viewModel.resetToCaptureMode() }
                    )
                }
            }
        }
    }
}

// ================= 1. CAPTURE & CROP SELECTOR VIEW =================

@Composable
private fun CaptureModeView(
    selectedCrop: CropType,
    flashEnabled: Boolean,
    recentScans: List<CropDiseaseResult>,
    onSelectCrop: (CropType) -> Unit,
    onToggleFlash: () -> Unit,
    onCapture: () -> Unit,
    onOpenGallery: () -> Unit,
    onSelectPreset: (String, String, String) -> Unit,
    onViewPastScan: (CropDiseaseResult) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step 1: Crop Selector
        item {
            CropSelectorCard(
                selectedCrop = selectedCrop,
                onCropSelected = onSelectCrop
            )
        }

        // Step 2: Camera Viewfinder with Leaf Target Overlay
        item {
            Card(
                shape = FarmSathiDesign.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141D17)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Viewfinder Custom Grid and Corner Reticle Lines
                    ViewfinderReticleCanvas(
                        modifier = Modifier.fillMaxSize()
                    )

                    // Top Toolbar (Flash + Leaf Guidance)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = FarmSathiDesign.shapes.pill,
                            color = Color.Black.copy(alpha = 0.55f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(FarmPrimaryGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TARGET: ${selectedCrop.englishName.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onToggleFlash,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (flashEnabled) FarmHarvestGold else Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Flash",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Center Guidance Watermark
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterCenterFocus,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Align infected leaf inside frame",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Ensure bright natural sunlight & sharp focus",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }

                    // Bottom Controls Row: Gallery Upload, Big Shutter Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gallery Button
                        IconButton(
                            onClick = onOpenGallery,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                .testTag("crop_disease_gallery_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Upload from Gallery",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Big Tactical Shutter Button
                        ShutterCaptureButton(
                            onClick = onCapture
                        )

                        // Placeholder for symmetry
                        Box(modifier = Modifier.size(48.dp))
                    }
                }
            }
        }

        // Step 3: Quick Diagnostic Leaf Presets (Instant Testing)
        item {
            Text(
                text = "SAMPLE LEAF TEST PRESETS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    SamplePresetCard(
                        title = "Soybean Rust",
                        crop = "Soybean (JS-2034)",
                        icon = "🍂",
                        onClick = {
                            onSelectCrop(CropType.SOYBEAN)
                            onSelectPreset("preset_soybean_rust", "soybean_rust", "Soybean Rust Leaf Sample")
                        }
                    )
                }
                item {
                    SamplePresetCard(
                        title = "Yellow Mosaic",
                        crop = "Soybean",
                        icon = "🍃",
                        onClick = {
                            onSelectCrop(CropType.SOYBEAN)
                            onSelectPreset("preset_soybean_mosaic", "soybean_mosaic", "Soybean Yellow Mosaic Sample")
                        }
                    )
                }
                item {
                    SamplePresetCard(
                        title = "Cotton Leaf Curl",
                        crop = "BT Cotton",
                        icon = "🌿",
                        onClick = {
                            onSelectCrop(CropType.COTTON)
                            onSelectPreset("preset_cotton_curl", "cotton_curl", "Cotton Leaf Curl Sample")
                        }
                    )
                }
                item {
                    SamplePresetCard(
                        title = "Healthy Leaf",
                        crop = "Soybean",
                        icon = "🌱",
                        onClick = {
                            onSelectCrop(CropType.SOYBEAN)
                            onSelectPreset("preset_healthy", "healthy_leaf", "Healthy Leaf Sample")
                        }
                    )
                }
            }
        }

        // Step 4: Recent Scans History
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "RECENT DIAGNOSTIC SCANS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                fontSize = 10.sp
            )
        }

        items(recentScans) { scan ->
            Card(
                onClick = { onViewPastScan(scan) },
                shape = FarmSathiDesign.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.medium)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = scan.diseaseName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${scan.cropName} • ${scan.timestamp}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = FarmSathiDesign.shapes.pill,
                            color = FarmPrimaryContainer
                        ) {
                            Text(
                                text = "${scan.confidence}% Match",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = FarmPrimaryGreen,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------- CROP SELECTOR CARD ----------------

@Composable
private fun CropSelectorCard(
    selectedCrop: CropType,
    onCropSelected: (CropType) -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.large)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "SELECT CROP TO SCAN / फसल चुनें",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CropType.entries) { crop ->
                    val isSelected = selectedCrop == crop
                    Surface(
                        shape = FarmSathiDesign.shapes.pill,
                        color = if (isSelected) FarmPrimaryGreen else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(FarmSathiDesign.shapes.pill)
                            .clickable { onCropSelected(crop) }
                            .border(
                                width = 1.dp,
                                color = if (isSelected) FarmPrimaryLight else MaterialTheme.colorScheme.outline,
                                shape = FarmSathiDesign.shapes.pill
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = crop.emoji, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = crop.englishName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- SHUTTER BUTTON ----------------

@Composable
private fun ShutterCaptureButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.25f))
            .clickable { onClick() }
            .testTag("crop_disease_capture_button"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, FarmPrimaryGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Capture Leaf",
                tint = FarmPrimaryGreen,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

// ---------------- VIEWFINDER RETICLE CANVAS ----------------

@Composable
private fun ViewfinderReticleCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val margin = 32.dp.toPx()
        val cornerLen = 28.dp.toPx()
        val color = Color(0xFF40916C)
        val strokeW = 3.dp.toPx()

        // Top-Left corner
        drawLine(color, Offset(margin, margin), Offset(margin + cornerLen, margin), strokeW)
        drawLine(color, Offset(margin, margin), Offset(margin, margin + cornerLen), strokeW)

        // Top-Right corner
        drawLine(color, Offset(w - margin, margin), Offset(w - margin - cornerLen, margin), strokeW)
        drawLine(color, Offset(w - margin, margin), Offset(w - margin, margin + cornerLen), strokeW)

        // Bottom-Left corner
        drawLine(color, Offset(margin, h - margin), Offset(margin + cornerLen, h - margin), strokeW)
        drawLine(color, Offset(margin, h - margin), Offset(margin, h - margin - cornerLen), strokeW)

        // Bottom-Right corner
        drawLine(color, Offset(w - margin, h - margin), Offset(w - margin - cornerLen, h - margin), strokeW)
        drawLine(color, Offset(w - margin, h - margin), Offset(w - margin, h - margin - cornerLen), strokeW)

        // Subtle center alignment crosshair
        val cx = w / 2f
        val cy = h / 2f
        val chLen = 14.dp.toPx()
        drawLine(color.copy(alpha = 0.4f), Offset(cx - chLen, cy), Offset(cx + chLen, cy), 1.5.dp.toPx())
        drawLine(color.copy(alpha = 0.4f), Offset(cx, cy - chLen), Offset(cx, cy + chLen), 1.5.dp.toPx())
    }
}

@Composable
private fun SamplePresetCard(
    title: String,
    crop: String,
    icon: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = FarmSathiDesign.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .width(135.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.medium)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = crop,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

// ================= 2. IMAGE PREVIEW VIEW =================

@Composable
private fun ImagePreviewView(
    imageUri: String?,
    sampleLabel: String?,
    selectedCrop: CropType,
    onRetake: () -> Unit,
    onAnalyze: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Leaf Preview Container
            Card(
                shape = FarmSathiDesign.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A22)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .border(2.dp, FarmPrimaryGreen, FarmSathiDesign.shapes.extraLarge)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(FarmPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = selectedCrop.emoji, fontSize = 46.sp)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = sampleLabel ?: "${selectedCrop.englishName} Leaf Capture",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = FarmSathiDesign.shapes.pill,
                            color = Color.Black.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "Ready for Secure AI Pathology Inference",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = FarmSathiDesign.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, FarmSathiDesign.shapes.medium)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = FarmPrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Image will be analyzed by standard agronomist pathology models via secure backend API proxy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Action Buttons: Retake vs Start AI Scan
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetake,
                shape = FarmSathiDesign.shapes.medium,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retake / दोबारा लें")
            }

            Button(
                onClick = onAnalyze,
                shape = FarmSathiDesign.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = FarmPrimaryGreen),
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("start_ai_disease_analysis_button")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Run AI Diagnosis", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ================= 3. UPLOADING & AI SCANNING ANIMATION VIEW =================

@Composable
private fun UploadingAndAnalyzingView(
    progress: Int,
    stageText: String,
    imageUri: String?,
    selectedCrop: CropType
) {
    val infiniteTransition = rememberInfiniteTransition(label = "laser_scan")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Holographic Scanning Leaf Preview Box
        Card(
            shape = FarmSathiDesign.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101913)),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .border(2.dp, FarmTechBlue, FarmSathiDesign.shapes.extraLarge)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(FarmPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = selectedCrop.emoji, fontSize = 42.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Analyzing ${selectedCrop.englishName} Leaf",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Laser Scanning Line Animation
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val y = size.height * laserY
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, FarmTechBlueLight, Color.White, FarmTechBlueLight, Color.Transparent)
                        ),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Linear Progress Bar & Percent Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stageText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$progress%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = FarmTechBlue
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = FarmTechBlue,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = FarmSathiDesign.shapes.pill,
            color = FarmTechBlueContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    color = FarmTechBlue,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Running multi-spectral agronomic inference...",
                    style = MaterialTheme.typography.labelSmall,
                    color = FarmTechBlueText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ================= 4. DISEASE RESULT VIEW =================

@Composable
private fun DiseaseResultView(
    result: CropDiseaseResult,
    onScanAgain: () -> Unit,
    onAskAi: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Diagnosis Summary Card
        item {
            Card(
                shape = FarmSathiDesign.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header: Crop Name & Risk Severity Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CROP DIAGNOSIS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = result.cropName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Risk Severity Badge (Only when supported by model)
                        if (!result.riskSeverity.isNullOrBlank()) {
                            RiskSeverityBadge(severity = result.riskSeverity)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Disease Title & Scientific Name
                    Text(
                        text = result.diseaseName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!result.pathogenScientificName.isNullOrBlank()) {
                        Text(
                            text = "Pathogen: ${result.pathogenScientificName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Confidence Metric Banner
                    Surface(
                        shape = FarmSathiDesign.shapes.medium,
                        color = FarmPrimaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Confidence Score",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FarmOnContainerGreen,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "${result.confidence}%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmPrimaryGreen
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = FarmPrimaryGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // Recommendation Box (Standardized Agronomic Guidance)
        item {
            Card(
                shape = FarmSathiDesign.shapes.large,
                colors = CardDefaults.cardColors(containerColor = FarmHarvestGoldContainer.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FarmHarvestGold.copy(alpha = 0.4f), FarmSathiDesign.shapes.large)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = FarmHarvestGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RECOMMENDED ACTION / मुख्य सलाह",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = FarmHarvestGold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Top Predictions List Card
        if (result.topPredictions.isNotEmpty()) {
            item {
                Card(
                    shape = FarmSathiDesign.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.large)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TOP PREDICTIONS / संभावित रोग",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            result.topPredictions.forEach { pred ->
                                TopPredictionRow(item = pred)
                            }
                        }
                    }
                }
            }
        }

        // Identified Symptoms
        if (result.symptoms.isNotEmpty()) {
            item {
                Card(
                    shape = FarmSathiDesign.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.large)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "OBSERVED SYMPTOMS / लक्षण",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        result.symptoms.forEach { symptom ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "• ",
                                    color = FarmPrimaryGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = symptom,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Treatment Protocols (Organic & Chemical standard advisories)
        item {
            Card(
                shape = FarmSathiDesign.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.large)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Organic Treatment
                    Surface(
                        color = FarmGreenContainer,
                        shape = FarmSathiDesign.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Spa, contentDescription = null, tint = FarmPrimaryGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🌿 Organic / Biological Protocol",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmOnContainerGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = result.organicRemedy,
                                style = MaterialTheme.typography.bodySmall,
                                color = FarmOnContainerGreen,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Chemical Protocol
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = FarmSathiDesign.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Science, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🧪 Chemical Fungicide Protocol",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = result.chemicalTreatment,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons: Scan Another Leaf vs Ask FarmSathi AI
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onScanAgain,
                    shape = FarmSathiDesign.shapes.medium,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan Another Leaf")
                }

                Button(
                    onClick = onAskAi,
                    shape = FarmSathiDesign.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = FarmTechBlue),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ask FarmSathi AI", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------- TOP PREDICTION ROW ----------------

@Composable
private fun TopPredictionRow(item: PredictionItem) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.isTop) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = FarmPrimaryGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = item.diseaseName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (item.isTop) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${item.confidencePercent}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (item.isTop) FarmPrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { item.confidencePercent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(CircleShape),
            color = if (item.isTop) FarmPrimaryGreen else FarmTechBlueLight,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

// ---------------- RISK SEVERITY BADGE ----------------

@Composable
private fun RiskSeverityBadge(severity: String) {
    val (bgColor, textColor, label) = when (severity.lowercase()) {
        "high", "critical" -> Triple(FarmCriticalRedContainer, FarmCriticalRed, "HIGH RISK")
        "medium", "moderate" -> Triple(FarmHarvestGoldContainer, FarmHarvestGold, "MEDIUM RISK")
        "healthy", "low" -> Triple(FarmSuccessGreenContainer, FarmSuccessGreen, "LOW / HEALTHY")
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, severity.uppercase())
    }

    Surface(
        shape = FarmSathiDesign.shapes.pill,
        color = bgColor
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ---------------- ERROR VIEW ----------------

@Composable
private fun ScanErrorView(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(FarmCriticalRedContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = FarmCriticalRed,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Diagnosis Error",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel) {
                Text("Back")
            }
            if (canRetry) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = FarmPrimaryGreen)
                ) {
                    Text("Retry Diagnosis")
                }
            }
        }
    }
}

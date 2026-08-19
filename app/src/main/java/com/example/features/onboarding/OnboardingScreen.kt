package com.example.features.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FarmPreferences
import com.example.designsystem.components.*
import com.example.designsystem.theme.FarmSathiDesign
import com.example.designsystem.theme.FarmSemanticStatus
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val description: String,
    val badge: String,
    val badgeStatus: FarmSemanticStatus
)

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit
) {
    val context = LocalContext.current
    val farmPreferences = remember { FarmPreferences(context) }
    val coroutineScope = rememberCoroutineScope()

    val pages = remember {
        listOf(
            OnboardingPageData(
                title = "Understand Your Farm",
                description = "Monitor your crops, soil, weather, and farm health from one place.",
                badge = "IoT Telemetry & Sensors",
                badgeStatus = FarmSemanticStatus.Live
            ),
            OnboardingPageData(
                title = "Your Personal AI Farm Assistant",
                description = "FarmSathi AI monitors your farm and provides personalized recommendations.",
                badge = "Context-Aware AI Companion",
                badgeStatus = FarmSemanticStatus.Cached
            ),
            OnboardingPageData(
                title = "Grow Better. Sell Smarter.",
                description = "Use AI-powered crop intelligence, market predictions, buyer matching, and logistics.",
                badge = "Mandi & Shared Freight",
                badgeStatus = FarmSemanticStatus.Healthy
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLastPage = pagerState.currentPage == pages.size - 1

    fun finishOnboarding() {
        farmPreferences.setOnboardingCompleted(true)
        onGetStarted()
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FarmSathi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Skip button
                TextButton(
                    onClick = { finishOnboarding() },
                    modifier = Modifier.testTag("onboarding_skip_button")
                ) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Horizontal Pager for the 3 Onboarding screens
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                OnboardingPageContent(pageIndex = pageIndex, data = pages[pageIndex])
            }

            // Bottom Navigation & Controls Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(if (isSelected) 28.dp else 8.dp)
                                .clip(FarmSathiDesign.shapes.pill)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                        )
                    }
                }

                // Next or Get Started Button
                if (isLastPage) {
                    FarmPrimaryButton(
                        text = "Get Started",
                        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                        onClick = { finishOnboarding() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_get_started_button")
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FarmSecondaryButton(
                            text = "Skip Tour",
                            onClick = { finishOnboarding() },
                            modifier = Modifier.weight(1f)
                        )
                        FarmPrimaryButton(
                            text = "Next",
                            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        pagerState.currentPage + 1,
                                        animationSpec = tween(400)
                                    )
                                }
                            },
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    pageIndex: Int,
    data: OnboardingPageData
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Visual Illustration Card for Each Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(FarmSathiDesign.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            when (pageIndex) {
                0 -> Screen1VisualCard()
                1 -> Screen2VisualCard()
                2 -> Screen3VisualCard()
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Status pill
        FarmStatusBadge(
            status = data.badgeStatus,
            customLabel = data.badge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Screen Title (Large & Bold)
        Text(
            text = data.title,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Screen Explanation
        Text(
            text = data.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

// SCREEN 1 Visual: Understand Your Farm
@Composable
private fun Screen1VisualCard() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(FarmSathiDesign.shapes.small)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Block A • Soybean",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ESP32 Node 01 Connected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
            FarmStatusBadge(status = FarmSemanticStatus.Healthy, customLabel = "92% Score")
        }

        // Sensor telemetry row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FarmSathiDesign.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            VisualMetricItem(icon = Icons.Default.WaterDrop, label = "MOISTURE", value = "42%")
            VisualMetricItem(icon = Icons.Default.Thermostat, label = "TEMP", value = "25.1°C")
            VisualMetricItem(icon = Icons.Default.WbSunny, label = "WEATHER", value = "Optimal")
        }

        // Live soil & irrigation line
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🌱 Soil pH 6.9 • Drip Valve Ready",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            FarmStatusBadge(status = FarmSemanticStatus.Live)
        }
    }
}

// SCREEN 2 Visual: Your Personal AI Farm Assistant
@Composable
private fun Screen2VisualCard() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(FarmTechBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "FarmSathi AI",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmTechBlueText
                    )
                    Text(
                        text = "Agronomy & Voice Intelligence",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
            FarmStatusBadge(status = FarmSemanticStatus.Cached, customLabel = "Hindi / Voice")
        }

        // AI Chat sample bubble
        Surface(
            color = FarmTechBlueContainer,
            shape = FarmSathiDesign.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, FarmTechBlueBorder, FarmSathiDesign.shapes.medium)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "“Weather is clear today. Safe window for foliar spray before tomorrow's monsoon showers.”",
                    style = MaterialTheme.typography.bodySmall,
                    color = FarmTechBlueText,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )
            }
        }

        // AI recommendation chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = FarmSathiDesign.shapes.small,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "🧪 NPK Advisory",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(6.dp),
                    textAlign = TextAlign.Center
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = FarmSathiDesign.shapes.small,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "🔍 Leaf Scan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(6.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// SCREEN 3 Visual: Grow Better. Sell Smarter.
@Composable
private fun Screen3VisualCard() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Indore APMC Mandi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Soybean • ₹4,850 / Quintal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            FarmStatusBadge(status = FarmSemanticStatus.Healthy, customLabel = "HOLD (+₹300/Q)")
        }

        // Market & Logistics cards row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Buyer Match
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = FarmSathiDesign.shapes.medium,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = FarmTechBlue, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Direct Buyer", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                    Text("Malwa Solvent", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text("₹4,950/Q Offer", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            // Shared Logistics
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = FarmSathiDesign.shapes.medium,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Shared Truck", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                    Text("Tata 407 (1.2T)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text("45% Cost Saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text(
            text = "📈 AI Forecast: 91% Confidence for ₹5,150/Q target",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun VisualMetricItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

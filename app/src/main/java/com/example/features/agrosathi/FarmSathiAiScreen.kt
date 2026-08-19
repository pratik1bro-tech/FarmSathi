package com.example.features.agrosathi

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.core.navigation.Screen
import com.example.core.voice.VoiceLanguage
import com.example.core.voice.VoiceSessionState
import com.example.data.models.FarmAiAction
import com.example.data.models.FarmAiMessage
import com.example.data.models.FarmAiPriority
import com.example.data.models.OrbState
import com.example.designsystem.theme.FarmSathiDesign
import com.example.features.agrosathi.components.AnimatedFarmAiOrb
import com.example.features.agrosathi.components.VoiceInteractionModal
import com.example.ui.theme.*

@Composable
fun FarmSathiAiScreen(
    viewModel: FarmSathiAiViewModel,
    onNavigate: (Screen) -> Unit,
    onOpenLanguageSelector: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Audio recording permission launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceSession(hasPermission = true)
        } else {
            viewModel.startVoiceSession(hasPermission = false)
        }
    }

    val requestVoiceWithPermission: () -> Unit = {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            viewModel.startVoiceSession(hasPermission = true)
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Auto-scroll on new message
    LaunchedEffect(state.messages.size, state.orbState) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size)
        }
    }

    Scaffold(
        topBar = {
            FarmSathiAiHeader(
                isOffline = state.isOffline,
                isMuted = state.isTtsMuted,
                selectedLanguage = state.selectedVoiceLanguage,
                onLanguageClick = onOpenLanguageSelector,
                onToggleMute = { viewModel.toggleTtsMute() },
                onClearClick = { viewModel.clearChatHistory() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Chat Stream + Animated AI Orb Hero Header
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("farm_sathi_ai_messages_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Item 0: Hero Animated AI Orb & Status Banner
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedFarmAiOrb(
                            state = state.orbState,
                            size = 140.dp,
                            onClick = requestVoiceWithPermission
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dynamic Orb State Caption
                        Surface(
                            shape = FarmSathiDesign.shapes.pill,
                            color = when (state.orbState) {
                                OrbState.IDLE -> FarmPrimaryContainer
                                OrbState.LISTENING -> FarmTechBlueContainer
                                OrbState.THINKING -> FarmHarvestGoldContainer
                                OrbState.SPEAKING -> FarmPrimaryContainer
                                OrbState.OFFLINE -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = when (state.orbState) {
                                        OrbState.IDLE -> FarmPrimaryLight.copy(alpha = 0.5f)
                                        OrbState.LISTENING -> FarmTechBlue
                                        OrbState.THINKING -> FarmHarvestGold
                                        OrbState.SPEAKING -> FarmPrimaryGreen
                                        OrbState.OFFLINE -> MaterialTheme.colorScheme.outline
                                    },
                                    shape = FarmSathiDesign.shapes.pill
                                )
                                .clickable { requestVoiceWithPermission() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (state.orbState) {
                                                OrbState.IDLE -> FarmSuccessGreen
                                                OrbState.LISTENING -> FarmTechBlue
                                                OrbState.THINKING -> FarmHarvestGold
                                                OrbState.SPEAKING -> FarmPrimaryGreen
                                                OrbState.OFFLINE -> FarmWarningAmber
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (state.orbState) {
                                        OrbState.IDLE -> "● बोलकर पूछें (Tap Orb to Speak)"
                                        OrbState.LISTENING -> "🎙️ Listening to voice query..."
                                        OrbState.THINKING -> "⚡ Synthesizing agronomist telemetry..."
                                        OrbState.SPEAKING -> "🔊 Speaking farm guidance..."
                                        OrbState.OFFLINE -> "⚠️ Offline Mode (Local Reasoning)"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Chat Messages
                items(state.messages, key = { it.id }) { message ->
                    if (message.isUser) {
                        UserChatBubble(message = message)
                    } else {
                        AssistantAiCard(
                            message = message,
                            isPlayingAudio = state.activePlayingMessageId == message.id,
                            onToggleAudio = { viewModel.toggleAudioPlayback(message.id) },
                            onActionClick = { action ->
                                if (action.targetRoute != null) {
                                    when (action.targetRoute) {
                                        Screen.SmartIrrigation.route -> onNavigate(Screen.SmartIrrigation)
                                        Screen.DiseaseDetection.route -> onNavigate(Screen.DiseaseDetection)
                                        Screen.SoilHealth.route -> onNavigate(Screen.SoilHealth)
                                        Screen.Market.route -> onNavigate(Screen.Market)
                                        Screen.Buyers.route -> onNavigate(Screen.Buyers)
                                        Screen.Logistics.route -> onNavigate(Screen.Logistics)
                                        Screen.WeatherIntelligence.route -> onNavigate(Screen.WeatherIntelligence)
                                        Screen.Forecasting.route -> onNavigate(Screen.Forecasting)
                                        Screen.OutbreakRadar.route -> onNavigate(Screen.OutbreakRadar)
                                        Screen.DigitalTwin.route -> onNavigate(Screen.DigitalTwin)
                                        Screen.Farm.route -> onNavigate(Screen.Farm)
                                        else -> onNavigate(Screen.Home)
                                    }
                                } else if (action.prompt != null) {
                                    viewModel.sendMessage(action.prompt)
                                }
                            }
                        )
                    }
                }

                // Thinking Indicator at end of stream
                if (state.orbState == OrbState.THINKING) {
                    item {
                        AssistantThinkingIndicator()
                    }
                }
            }

            // Suggested Questions Horizontal Carousel
            SuggestedQuestionsBar(
                questions = state.suggestedQuestions,
                onQuestionClick = { viewModel.sendMessage(it) }
            )

            // Bottom Input Bar (Camera, Text, Mic, Send)
            FarmSathiInputBar(
                inputText = state.inputText,
                isListening = state.orbState == OrbState.LISTENING || state.voiceSessionState !is VoiceSessionState.Idle,
                onTextChanged = { viewModel.onInputTextChanged(it) },
                onSend = { viewModel.sendMessage() },
                onMicClick = requestVoiceWithPermission,
                onCameraClick = { viewModel.scanCropWithCamera() }
            )
        }

        // Multilingual Voice Interaction Modal & Live Waveform
        VoiceInteractionModal(
            voiceState = state.voiceSessionState,
            selectedLanguage = state.selectedVoiceLanguage,
            isTtsMuted = state.isTtsMuted,
            onLanguageChange = { viewModel.setVoiceLanguage(it) },
            onStopListening = { viewModel.stopVoiceListening() },
            onCancel = { viewModel.cancelVoiceSession() },
            onRetry = { viewModel.retryVoiceSession() },
            onToggleMute = { viewModel.toggleTtsMute() }
        )
    }
}

// ---------------- HEADER COMPONENT ----------------

@Composable
private fun FarmSathiAiHeader(
    isOffline: Boolean,
    isMuted: Boolean,
    selectedLanguage: VoiceLanguage,
    onLanguageClick: () -> Unit,
    onToggleMute: () -> Unit,
    onClearClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(0.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(FarmSathiDesign.shapes.medium)
                        .background(FarmTechBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "FarmSathi AI",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "FarmSathi AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isOffline) FarmWarningAmber else FarmSuccessGreen)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isOffline) "Offline (Local Engine)" else "● Monitoring your farm",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Audio TTS Mute / Unmute Button
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isMuted) FarmAlertRedContainer else MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, if (isMuted) FarmAlertRed else MaterialTheme.colorScheme.outline, CircleShape)
                        .testTag("ai_header_toggle_mute")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Toggle Mute",
                        tint = if (isMuted) FarmAlertRed else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Language Selector Button
                IconButton(
                    onClick = onLanguageClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Language",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Reset Briefing Button
                IconButton(
                    onClick = onClearClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart Briefing",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ---------------- USER MESSAGE BUBBLE ----------------

@Composable
private fun UserChatBubble(message: FarmAiMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = 18.dp,
                bottomEnd = 4.dp
            ),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .widthIn(max = 290.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp))
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

// ---------------- ASSISTANT STRUCTURED AI CARD ----------------

@Composable
private fun AssistantAiCard(
    message: FarmAiMessage,
    isPlayingAudio: Boolean,
    onToggleAudio: () -> Unit,
    onActionClick: (FarmAiAction) -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Bot Avatar, Bot Name, Priority Badge, TTS Speaker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(FarmTechBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FarmSathi AI",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (message.priority != null) {
                        PriorityBadge(priority = message.priority)
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // TTS Audio Speaker Button
                    IconButton(
                        onClick = onToggleAudio,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isPlayingAudio) FarmTechBlue else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = if (isPlayingAudio) Icons.Default.GraphicEq else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak",
                            tint = if (isPlayingAudio) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Core Response Text
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            // Reasoning Section Box
            if (!message.reason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = FarmSathiDesign.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, FarmSathiDesign.shapes.medium)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Reason",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "UNDERLYING TELEMETRY & REASON",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = message.reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Recommended Actions Pills
            if (message.recommendedActions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "RECOMMENDED ACTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    message.recommendedActions.forEach { action ->
                        ActionPillButton(
                            action = action,
                            onClick = { onActionClick(action) }
                        )
                    }
                }
            }

            // Timestamp
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun PriorityBadge(priority: FarmAiPriority) {
    val (bgColor, textColor, label) = when (priority) {
        FarmAiPriority.CRITICAL -> Triple(FarmCriticalRedContainer, FarmCriticalRed, "CRITICAL")
        FarmAiPriority.HIGH -> Triple(FarmHarvestGoldContainer, FarmHarvestGold, "HIGH")
        FarmAiPriority.MEDIUM -> Triple(FarmTechBlueContainer, FarmTechBlue, "ADVISORY")
        FarmAiPriority.OPTIMAL -> Triple(FarmSuccessGreenContainer, FarmSuccessGreen, "OPTIMAL")
        FarmAiPriority.INFO -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "INFO")
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun ActionPillButton(
    action: FarmAiAction,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = FarmSathiDesign.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), FarmSathiDesign.shapes.medium)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = when (action.iconName) {
                        "water", "valve" -> Icons.Default.WaterDrop
                        "camera" -> Icons.Default.CameraAlt
                        "market" -> Icons.Default.TrendingUp
                        "buyer" -> Icons.Default.Handshake
                        "truck" -> Icons.Default.LocalShipping
                        "soil" -> Icons.Default.Spa
                        "twin" -> Icons.Default.Layers
                        "radar" -> Icons.Default.Radar
                        "weather" -> Icons.Default.CloudQueue
                        "farm" -> Icons.Default.Agriculture
                        else -> Icons.Default.AutoAwesome
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = action.label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun AssistantThinkingIndicator() {
    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.large)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = FarmTechBlue,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "FarmSathi AI is analyzing IoT sensors, weather, and mandi trends...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

// ---------------- SUGGESTED QUESTIONS CAROUSEL ----------------

@Composable
private fun SuggestedQuestionsBar(
    questions: List<String>,
    onQuestionClick: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(0.dp))
            .padding(vertical = 8.dp)
    ) {
        Column {
            Text(
                text = "SUGGESTED QUESTIONS / पूछे जाने वाले प्रश्न",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(questions) { question ->
                    Surface(
                        shape = FarmSathiDesign.shapes.pill,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(FarmSathiDesign.shapes.pill)
                            .clickable { onQuestionClick(question) }
                            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.pill)
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// ---------------- BOTTOM INPUT BAR ----------------

@Composable
private fun FarmSathiInputBar(
    inputText: String,
    isListening: Boolean,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    onMicClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(0.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Camera Button (Instant Leaf & Farm Scan)
            IconButton(
                onClick = onCameraClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .testTag("ai_camera_scan_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Scan Crop Leaf",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Text Input Field
            OutlinedTextField(
                value = inputText,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        text = if (isListening) "Listening to speech..." else "Ask in Hindi, Marathi, English...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background
                ),
                shape = FarmSathiDesign.shapes.large,
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_text_input_field"),
                maxLines = 3
            )

            // Voice Mic Button (With visual pulse when listening)
            IconButton(
                onClick = onMicClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isListening) FarmTechBlue else MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, if (isListening) FarmTechBlueLight else MaterialTheme.colorScheme.outline, CircleShape)
                    .testTag("ai_mic_voice_button")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = if (isListening) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Send Button
            IconButton(
                onClick = onSend,
                enabled = inputText.isNotBlank(),
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .testTag("ai_send_message_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

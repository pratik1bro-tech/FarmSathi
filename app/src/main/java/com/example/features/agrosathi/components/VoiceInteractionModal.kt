package com.example.features.agrosathi.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.voice.VoiceErrorCode
import com.example.core.voice.VoiceLanguage
import com.example.core.voice.VoiceSessionState
import com.example.designsystem.theme.FarmSathiDesign
import com.example.ui.theme.*
import kotlin.random.Random

@Composable
fun VoiceInteractionModal(
    voiceState: VoiceSessionState,
    selectedLanguage: VoiceLanguage,
    isTtsMuted: Boolean,
    onLanguageChange: (VoiceLanguage) -> Unit,
    onStopListening: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onToggleMute: () -> Unit
) {
    if (voiceState is VoiceSessionState.Idle) return

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .clip(FarmSathiDesign.shapes.extraLarge)
                .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
                .testTag("voice_interaction_modal"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Modal Header: Language & Mute Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(FarmTechBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FarmSathi Voice",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onToggleMute,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isTtsMuted) FarmAlertRedContainer else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = if (isTtsMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Toggle Mute",
                                tint = if (isTtsMuted) FarmAlertRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onCancel,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Language Selection Chips
                LanguageSelectorChips(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelect = onLanguageChange
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Center Dynamic Content depending on VoiceState
                when (voiceState) {
                    is VoiceSessionState.Listening -> {
                        ListeningView(
                            partialText = voiceState.partialText,
                            rmsDb = voiceState.rmsDb,
                            language = voiceState.language
                        )
                    }
                    is VoiceSessionState.Processing -> {
                        ProcessingView(text = voiceState.text)
                    }
                    is VoiceSessionState.Speaking -> {
                        SpeakingView(
                            text = voiceState.text,
                            isMuted = isTtsMuted,
                            onToggleMute = onToggleMute
                        )
                    }
                    is VoiceSessionState.Error -> {
                        ErrorView(
                            code = voiceState.code,
                            message = voiceState.message,
                            canRetry = voiceState.canRetry,
                            onRetry = onRetry,
                            language = selectedLanguage
                        )
                    }
                    is VoiceSessionState.RequestingPermission -> {
                        PermissionView()
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Action Controls: Stop / Cancel / Retry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (voiceState is VoiceSessionState.Listening) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f),
                            shape = FarmSathiDesign.shapes.medium
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cancel / रद्द करें")
                        }

                        Button(
                            onClick = onStopListening,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("voice_stop_listening_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = FarmPrimaryGreen),
                            shape = FarmSathiDesign.shapes.medium
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Done / पूछें", fontWeight = FontWeight.Bold)
                        }
                    } else if (voiceState is VoiceSessionState.Error) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f),
                            shape = FarmSathiDesign.shapes.medium
                        ) {
                            Text("Close / बंद करें")
                        }

                        Button(
                            onClick = onRetry,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("voice_retry_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = FarmTechBlue),
                            shape = FarmSathiDesign.shapes.medium
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry / दोबारा बोलें", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onCancel,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = FarmPrimaryGreen),
                            shape = FarmSathiDesign.shapes.medium
                        ) {
                            Text("Done / पूर्ण", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- LISTENING VIEW WITH LIVE WAVEFORM ----------------

@Composable
private fun ListeningView(
    partialText: String,
    rmsDb: Float,
    language: VoiceLanguage
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Live Voice Waveform
        VoiceWaveformVisualizer(
            rmsDb = rmsDb,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // State Title & Subtitle
        Text(
            text = if (language == VoiceLanguage.HINDI) "सुन रहे हैं... बोलिए" else "Listening... Please speak",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Recognized partial speech text
        Surface(
            shape = FarmSathiDesign.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, FarmSathiDesign.shapes.large)
                .padding(4.dp)
        ) {
            Text(
                text = if (partialText.isNotBlank()) "\"$partialText\"" else if (language == VoiceLanguage.HINDI) "\"मेरी फसल की हालत कैसी है?\"" else "\"How is my farm health?\"",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (partialText.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                color = if (partialText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

// ---------------- PROCESSING VIEW ----------------

@Composable
private fun ProcessingView(text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(46.dp),
            color = FarmTechBlue,
            strokeWidth = 3.dp
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Analyzing farm telemetry...",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "\"$text\"",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ---------------- SPEAKING VIEW ----------------

@Composable
private fun SpeakingView(
    text: String,
    isMuted: Boolean,
    onToggleMute: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "speaking_wave")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speaking_scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(FarmPrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = FarmPrimaryGreen,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isMuted) "Audio Muted / आवाज बंद है" else "Speaking Farm Advisory / बोल रहे हैं...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = FarmSathiDesign.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(12.dp),
                lineHeight = 18.sp
            )
        }
    }
}

// ---------------- ERROR VIEW ----------------

@Composable
private fun ErrorView(
    code: VoiceErrorCode,
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    language: VoiceLanguage
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(FarmAlertRedContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MicOff,
                contentDescription = null,
                tint = FarmAlertRed,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (language == VoiceLanguage.HINDI) code.userFriendlyHindi else code.userFriendlyEnglish,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = FarmAlertRed,
            textAlign = TextAlign.Center
        )

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------------- PERMISSION VIEW ----------------

@Composable
private fun PermissionView() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = FarmTechBlue, modifier = Modifier.size(36.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Requesting microphone permission...",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ---------------- LIVE MULTI-BAR VOICE WAVEFORM ----------------

@Composable
private fun VoiceWaveformVisualizer(
    rmsDb: Float,
    modifier: Modifier = Modifier
) {
    val barCount = 18
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val barWidth = 4.dp.toPx()
        val spacing = (totalWidth - (barCount * barWidth)) / (barCount - 1)
        val maxHeight = size.height * 0.9f
        val minHeight = 6.dp.toPx()

        for (i in 0 until barCount) {
            val progress = i.toFloat() / (barCount - 1)
            val sineFactor = (kotlin.math.sin(phase + (progress * 4f)) + 1f) / 2f
            val amplitudeFactor = (rmsDb / 10f).coerceIn(0.1f, 1.0f)

            val currentHeight = minHeight + (maxHeight - minHeight) * sineFactor * amplitudeFactor
            val left = i * (barWidth + spacing)
            val top = (size.height - currentHeight) / 2f

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(FarmTechBlueLight, FarmPrimaryLight, FarmPrimaryGreen)
                ),
                topLeft = Offset(left, top),
                size = Size(barWidth, currentHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

// ---------------- LANGUAGE SELECTOR CHIPS ----------------

@Composable
private fun LanguageSelectorChips(
    selectedLanguage: VoiceLanguage,
    onLanguageSelect: (VoiceLanguage) -> Unit
) {
    val supportedLanguages = listOf(
        VoiceLanguage.HINDI,
        VoiceLanguage.ENGLISH,
        VoiceLanguage.MARATHI,
        VoiceLanguage.GUJARATI,
        VoiceLanguage.PUNJABI,
        VoiceLanguage.TELUGU
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(supportedLanguages) { lang ->
            val isSelected = selectedLanguage == lang
            Surface(
                shape = FarmSathiDesign.shapes.pill,
                color = if (isSelected) FarmPrimaryGreen else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clip(FarmSathiDesign.shapes.pill)
                    .clickable { onLanguageSelect(lang) }
                    .border(
                        width = 1.dp,
                        color = if (isSelected) FarmPrimaryLight else MaterialTheme.colorScheme.outline,
                        shape = FarmSathiDesign.shapes.pill
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = lang.flagEmoji, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = lang.nativeName,
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

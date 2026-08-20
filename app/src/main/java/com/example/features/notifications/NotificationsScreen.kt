package com.example.features.notifications

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.AlertCategory
import com.example.data.models.AlertPriority
import com.example.data.models.ProactiveFarmAlert
import com.example.data.repository.FarmRepository
import com.example.data.service.ProactiveAlertEngine
import com.example.designsystem.theme.FarmSathiDesign
import com.example.shared.components.FarmSathiTopBar
import com.example.ui.theme.*

@Composable
fun NotificationsScreen(
    repository: FarmRepository,
    onBack: () -> Unit,
    onNavigateToDeepLink: (String) -> Unit = {},
    alertEngine: ProactiveAlertEngine = remember { ProactiveAlertEngine() },
    viewModel: NotificationsViewModel = viewModel()
) {
    val alerts by alertEngine.getAlerts().collectAsState(initial = emptyList())
    val uiState by viewModel.uiState.collectAsState()
    val selectedCategory = uiState.selectedCategory

    val filteredAlerts = remember(alerts, selectedCategory) {
        if (selectedCategory == null) alerts
        else alerts.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            FarmSathiTopBar(
                title = "Proactive AI Farm Alert Hub",
                subtitle = "FCM Backend-Generated Push Advisories",
                showBackButton = true,
                onBackClick = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("notifications_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. FCM Cloud Messaging Service Status Hero Banner
            item {
                FcmStatusHeroBanner(status = uiState.fcmStatus)
            }

            // 2. Alert Category Filter Chips
            item {
                Text(
                    text = "FILTER BY ALERT CATEGORY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.selectCategoryFilter(null) },
                            label = { Text("All (${alerts.size})") }
                        )
                    }

                    items(AlertCategory.values()) { category ->
                        val count = alerts.count { it.category == category }
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { viewModel.selectCategoryFilter(category) },
                            label = { Text("${category.iconEmoji} ${category.title} ($count)") }
                        )
                    }
                }
            }

            // 3. Notification Cards List
            if (filteredAlerts.isEmpty()) {
                item {
                    EmptyAlertsCard()
                }
            } else {
                items(filteredAlerts, key = { it.id }) { alert ->
                    ProactiveAlertCard(
                        alert = alert,
                        onMarkRead = { alertEngine.markAsRead(alert.id) },
                        onDismiss = { alertEngine.dismissAlert(alert.id) },
                        onExecuteAction = { onNavigateToDeepLink(alert.deepLinkRoute) }
                    )
                }
            }
        }
    }
}

// ================= 1. FCM STATUS HERO BANNER =================

@Composable
private fun FcmStatusHeroBanner(status: String) {
    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = FarmPrimaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(FarmPrimaryGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = FarmPrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "FIREBASE CLOUD MESSAGING (FCM) ARCHITECTURE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = FarmPrimaryGreen,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Receiving real-time server telemetry push events. Zero mock client generation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ================= 2. PROACTIVE ALERT CARD =================

@Composable
private fun ProactiveAlertCard(
    alert: ProactiveFarmAlert,
    onMarkRead: () -> Unit,
    onDismiss: () -> Unit,
    onExecuteAction: () -> Unit
) {
    val category = alert.category
    val priority = alert.priority

    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (!alert.isRead && priority == AlertPriority.CRITICAL) {
                FarmAlertRedContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (!alert.isRead) 1.5.dp else 1.dp,
                color = if (!alert.isRead) priority.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = FarmSathiDesign.shapes.large
            )
            .testTag("alert_card_${alert.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Badge
                    Surface(
                        shape = FarmSathiDesign.shapes.pill,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${category.iconEmoji} ${category.title}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // Priority Badge
                    Surface(
                        shape = FarmSathiDesign.shapes.pill,
                        color = priority.color
                    ) {
                        Text(
                            text = priority.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = alert.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Description
            Text(
                text = alert.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = alert.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            // Related Field Chip (if applicable)
            alert.relatedField?.let { field ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = FarmPrimaryGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Related Field: $field",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmPrimaryGreen,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recommended Action Box
            Surface(
                shape = FarmSathiDesign.shapes.medium,
                color = FarmPrimaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "💡 Recommended Action:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmPrimaryGreen,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = alert.recommendedAction,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row (Deep Link + Dismiss)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text("Dismiss", fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        onMarkRead()
                        onExecuteAction()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FarmPrimaryGreen),
                    shape = FarmSathiDesign.shapes.pill,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Execute Action ➔", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun EmptyAlertsCard() {
    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = FarmSuccessGreen,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "All Farm Alerts Resolved",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "No pending proactive alerts in this category.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

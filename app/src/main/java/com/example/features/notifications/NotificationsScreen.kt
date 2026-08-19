package com.example.features.notifications

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AppNotification
import com.example.data.repository.FarmRepository
import com.example.shared.components.*
import com.example.ui.theme.*

@Composable
fun NotificationsScreen(
    repository: FarmRepository,
    onBack: () -> Unit
) {
    val notifications by repository.getNotifications().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            FarmSathiTopBar(
                title = "Farm Alert Center",
                subtitle = "Weather, Market, Outbreak & IoT Alerts",
                showBackButton = true,
                onBackClick = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notifications) { notif ->
                NotificationCard(
                    notification = notif,
                    onDismiss = { repository.markNotificationRead(notif.id) }
                )
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: AppNotification,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isUrgent) AlertRedContainer else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (notification.isUrgent) AlertRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusBadge(
                        text = notification.category,
                        type = if (notification.isUrgent) StatusType.DANGER else StatusType.INFO
                    )
                    Text(
                        text = notification.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (notification.isUrgent) Color(0xFF5C1010) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (notification.isUrgent) Color(0xFF330808) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

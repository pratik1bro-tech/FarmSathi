package com.example.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmSathiTopBar(
    title: String,
    subtitle: String? = null,
    onNotificationsClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    unreadNotificationCount: Int = 2,
    selectedLanguage: String = "हिन्दी",
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    userInitials: String = "RS"
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (showBackButton) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .testTag("topbar_back_button")
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, CleanMinBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = CleanMinPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = CleanMinPrimary,
                            letterSpacing = (-0.3).sp
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = CleanMinTextSecondary,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                // Action icons: Language Pill, Notification Bell, Clean Avatar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Language Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CleanMinContainer,
                        modifier = Modifier
                            .testTag("topbar_language_button")
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(onClick = onLanguageClick)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Translate,
                                contentDescription = "Change Language",
                                tint = CleanMinOnContainerDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedLanguage,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CleanMinOnContainerDark,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Notification Bell with Badge
                    Box {
                        IconButton(
                            onClick = onNotificationsClick,
                            modifier = Modifier
                                .testTag("topbar_notifications_button")
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CleanMinCard)
                                .border(1.dp, CleanMinBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = CleanMinTextDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (unreadNotificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(CleanMinRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$unreadNotificationCount",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Clean Minimalism Avatar badge (w-11 h-11 bg-[#D1E9D0] rounded-full border-2 border-[#2D6A4F])
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CleanMinAvatarBg)
                            .border(2.dp, CleanMinPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitials,
                            color = CleanMinOnContainerDark,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

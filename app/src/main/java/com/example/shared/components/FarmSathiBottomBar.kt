package com.example.shared.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.navigation.Screen
import com.example.ui.theme.*

sealed class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    object Home : BottomNavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home")
    object Farm : BottomNavItem(Screen.Farm, "Farm", Icons.Filled.Yard, Icons.Outlined.Yard, "nav_farm")
    object AgroSathiAi : BottomNavItem(Screen.AgroSathiAi, "AI", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_farmsathi_ai")
    object Market : BottomNavItem(Screen.Market, "Market", Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp, "nav_market")
    object Profile : BottomNavItem(Screen.Profile, "Profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Farm,
    BottomNavItem.AgroSathiAi,
    BottomNavItem.Market,
    BottomNavItem.Profile
)

@Composable
fun FarmSathiBottomBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    Surface(
        color = CleanMinCard,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .border(
                width = 1.dp,
                color = CleanMinDivider,
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.screen.route
                val isAiProminent = item is BottomNavItem.AgroSathiAi

                if (isAiProminent) {
                    ProminentAiBottomButton(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onNavigate(item.screen) }
                    )
                } else {
                    StandardBottomNavItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onNavigate(item.screen) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProminentAiBottomButton(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .testTag(item.testTag)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .scale(if (isSelected) 1.06f else pulseScale)
                .size(54.dp)
                .shadow(
                    elevation = if (isSelected) 8.dp else 4.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = CleanMinPrimary,
                    spotColor = CleanMinPrimary
                )
                .clip(RoundedCornerShape(18.dp))
                .background(CleanMinPrimary),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "FarmSathi AI",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "AI",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun StandardBottomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) CleanMinOnContainerDark else CleanMinTextMuted,
        label = "iconTint"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .testTag(item.testTag)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (isSelected) CleanMinContainer else Color.Transparent
                )
                .padding(horizontal = 16.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) CleanMinOnContainerDark else CleanMinTextMuted,
            fontSize = 10.sp
        )
    }
}

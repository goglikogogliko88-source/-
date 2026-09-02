package com.example.ui.components

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HuntingConditionScore
import com.example.ui.theme.*
import com.example.viewmodel.ActiveHuntSession
import com.example.viewmodel.AppDestination

@Composable
fun MonadireTopHeader(
    activeHunt: ActiveHuntSession?,
    unreadNotificationCount: Int,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onActiveHuntClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ForestDark, ForestBlack)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ForestSurfaceVariant)
                        .border(1.dp, AccentGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrackChanges,
                        contentDescription = "Monadire Logo",
                        tint = AccentGold,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column {
                    Text(
                        text = "MONADIRE",
                        color = AccentGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "ქართული სანადირო აპლიკაცია",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ForestSurface)
                        .testTag("header_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "ძიება",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ForestSurface)
                        .testTag("header_notification_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotificationCount > 0) {
                                Badge(
                                    containerColor = AccentGold,
                                    contentColor = ForestBlack
                                ) {
                                    Text(
                                        text = unreadNotificationCount.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "შეტყობინებები",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Active hunt banner if hunting session is live
        if (activeHunt != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onActiveHuntClick() }
                    .border(1.dp, HuntingGreenLight, RoundedCornerShape(12.dp)),
                color = HuntingGreenDark.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(AccentGold)
                        )
                        Column {
                            Text(
                                text = "მიმდინარე ნადირობა: ${activeHunt.targetSpecies}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${activeHunt.locationName} • ხანგრძლივობა: ${formatDuration(activeHunt.elapsedSeconds)}",
                                color = TextGold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "გადასვლა",
                        tint = AccentGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MonadireBottomNavBar(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = ForestDark,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                label = "მთავარი",
                icon = Icons.Default.Home,
                selectedIcon = Icons.Filled.Home,
                isSelected = currentDestination == AppDestination.HOME,
                testTag = "nav_home",
                onClick = { onNavigate(AppDestination.HOME) }
            )

            NavBarItem(
                label = "რუკა",
                icon = Icons.Default.Map,
                selectedIcon = Icons.Filled.Map,
                isSelected = currentDestination == AppDestination.MAP,
                testTag = "nav_map",
                onClick = { onNavigate(AppDestination.MAP) }
            )

            NavBarItem(
                label = "ნადირობა",
                icon = Icons.Default.TrackChanges,
                selectedIcon = Icons.Filled.TrackChanges,
                isSelected = currentDestination == AppDestination.ACTIVE_HUNT,
                testTag = "nav_active_hunt",
                isHighlight = true,
                onClick = { onNavigate(AppDestination.ACTIVE_HUNT) }
            )

            NavBarItem(
                label = "ჟურნალი",
                icon = Icons.Default.MenuBook,
                selectedIcon = Icons.Filled.MenuBook,
                isSelected = currentDestination == AppDestination.JOURNAL,
                testTag = "nav_journal",
                onClick = { onNavigate(AppDestination.JOURNAL) }
            )

            NavBarItem(
                label = "პროფილი",
                icon = Icons.Default.Person,
                selectedIcon = Icons.Filled.Person,
                isSelected = currentDestination == AppDestination.PROFILE,
                testTag = "nav_profile",
                onClick = { onNavigate(AppDestination.PROFILE) }
            )
        }
    }
}

@Composable
private fun NavBarItem(
    label: String,
    icon: ImageVector,
    selectedIcon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    isHighlight: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isHighlight) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) AccentGold else HuntingGreenPrimary
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) selectedIcon else icon,
                    contentDescription = label,
                    tint = ForestBlack,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Icon(
                imageVector = if (isSelected) selectedIcon else icon,
                contentDescription = label,
                tint = if (isSelected) AccentGold else TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AccentGold else TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ConditionScoreBadge(
    score: HuntingConditionScore,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (score) {
        HuntingConditionScore.VERY_GOOD -> Pair(HuntingGreenDark, HuntingGreenLight)
        HuntingConditionScore.GOOD -> Pair(ForestSurfaceVariant, AccentGoldLight)
        HuntingConditionScore.MODERATE -> Pair(ForestSurface, WarningOrange)
        HuntingConditionScore.POOR -> Pair(ForestSurface, AlertRed)
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = score.labelKa,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun formatDuration(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format(java.util.Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format(java.util.Locale.US, "%02d:%02d", mins, secs)
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationEntity
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination
import com.example.viewmodel.MonadireViewModel

@Composable
fun ProfileScreen(
    viewModel: MonadireViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val allNotifications by viewModel.allNotifications.collectAsState()
    val allTrips by viewModel.allTrips.collectAsState()
    val allSpots by viewModel.allSpots.collectAsState()
    val allEquipment by viewModel.allEquipment.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ForestBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // Hunter Profile Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ForestSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(ForestSurfaceVariant)
                            .border(2.dp, AccentGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Hunter Avatar",
                            tint = AccentGold,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = userProfile.name,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = userProfile.email,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ForestSurfaceVariant
                    ) {
                        Text(
                            text = "მონადირის მოწმობა: ${userProfile.hunterIdNumber}",
                            color = AccentGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${allTrips.size}", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("ნადირობა", color = TextSecondary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${allSpots.size}", color = AccentGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("წერტილი", color = TextSecondary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${allEquipment.size}", color = HuntingGreenLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("იარაღი", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showEditProfileDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForestSurfaceVariant,
                            contentColor = AccentGold
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("პროფილის რედაქტირება", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Offline Architecture Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ForestSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = HuntingGreenLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ოფლაინ მონაცემთა ბაზა (Room)",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ყველა წერტილი, ჟურნალი და ეკიპირება ხელმისაწვდომია მთაში ინტერნეტის გარეშე.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Quick Admin & Navigation Buttons
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ForestSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "სისტემური მენიუ",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ProfileMenuItem(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "ადმინ პანელი (სეზონები და მონაცემები)",
                        subtitle = if (userProfile.isAdmin) "აქტიურია (ადმინისტრატორი)" else "წვდომის გახსნა",
                        onClick = { viewModel.navigateTo(AppDestination.ADMIN_PANEL) }
                    )

                    ProfileMenuItem(
                        icon = Icons.Default.Shield,
                        title = "აღჭურვილობის ინვენტარი",
                        subtitle = "იარაღის და ეკიპირების მართვა",
                        onClick = { viewModel.navigateTo(AppDestination.EQUIPMENT_INVENTORY) }
                    )

                    ProfileMenuItem(
                        icon = Icons.Default.Gavel,
                        title = "წესები, სეზონები და უსაფრთხოება",
                        subtitle = "ოფიციალური კანონმდებლობა და 112",
                        onClick = { viewModel.navigateTo(AppDestination.SAFETY_AND_RULES) }
                    )
                }
            }
        }

        // Notifications Center Section
        item {
            Text(
                text = "შეტყობინებების ცენტრი",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (allNotifications.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.NotificationsNone,
                    title = "შეტყობინებები არ არის",
                    description = "სეზონური და ამინდის შეტყობინებები აქ გამოჩნდება."
                )
            }
        } else {
            items(allNotifications, key = { it.id }) { notif ->
                NotificationCardItem(
                    notification = notif,
                    onRead = { viewModel.markNotificationRead(notif.id) },
                    onDelete = { viewModel.deleteNotification(notif.id) }
                )
            }
        }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            current = userProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, email, pref, fav ->
                viewModel.updateUserProfile(name, email, pref, fav)
                showEditProfileDialog = false
            }
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ForestSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextSecondary, fontSize = 11.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun NotificationCardItem(
    notification: NotificationEntity,
    onRead: () -> Unit,
    onDelete: () -> Unit
) {
    val icon = when (notification.type) {
        "სეზონი" -> Icons.Default.DateRange
        "ამინდი" -> Icons.Default.WbSunny
        "მოვლა" -> Icons.Default.Build
        else -> Icons.Default.Notifications
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onRead),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) ForestSurface else ForestSurfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (!notification.isRead) AccentGold.copy(alpha = 0.5f) else ForestCardBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (!notification.isRead) AccentGold.copy(alpha = 0.2f) else ForestDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = if (!notification.isRead) AccentGold else TextSecondary, modifier = Modifier.size(18.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(notification.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(notification.timestamp)),
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(notification.message, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "წაშლა", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun EditProfileDialog(
    current: UserProfile,
    onDismiss: () -> Unit,
    onSave: (name: String, email: String, pref: String, fav: String) -> Unit
) {
    var name by remember { mutableStateOf(current.name) }
    var email by remember { mutableStateOf(current.email) }
    var pref by remember { mutableStateOf(current.huntingTypePreference) }
    var fav by remember { mutableStateOf(current.favoriteSpecies) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ForestDark,
        title = {
            Text("პროფილის რედაქტირება", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("მონადირის სახელი") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = ForestCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("ელ-ფოსტა") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = ForestCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pref,
                    onValueChange = { pref = it },
                    label = { Text("სანადირო მიმართულება") },
                    placeholder = { Text("ფრინველზე, ნადირზე") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = ForestCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fav,
                    onValueChange = { fav = it },
                    label = { Text("რჩეული სახეობა") },
                    placeholder = { Text("მწყერი, ქედანი, ტყის ქათამი") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = ForestCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, email, pref, fav) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = ForestBlack),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("შენახვა", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("გაუქმება", color = TextSecondary)
            }
        }
    )
}

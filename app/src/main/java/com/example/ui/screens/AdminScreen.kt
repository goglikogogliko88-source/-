package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationEntity
import com.example.data.model.SpeciesEntity
import com.example.ui.theme.*
import com.example.viewmodel.MonadireViewModel

@Composable
fun AdminScreen(
    viewModel: MonadireViewModel,
    modifier: Modifier = Modifier
) {
    val allSpecies by viewModel.allSpecies.collectAsState()
    val allTrips by viewModel.allTrips.collectAsState()
    val allSpots by viewModel.allSpots.collectAsState()
    val allEquipment by viewModel.allEquipment.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var showAddSpeciesDialog by remember { mutableStateOf(false) }
    var showBroadcastDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ForestBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // Admin Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ForestSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AccentGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = AccentGold, modifier = Modifier.size(24.dp))
                            }
                            Column {
                                Text("ადმინ პანელი", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                Text("სისტემური მართვა და მონაცემები", color = TextSecondary, fontSize = 11.sp)
                            }
                        }

                        Switch(
                            checked = userProfile.isAdmin,
                            onCheckedChange = { viewModel.toggleAdminMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentGold,
                                checkedTrackColor = HuntingGreenDark
                            )
                        )
                    }
                }
            }
        }

        // Database Metrics Grid
        item {
            Text("მონაცემთა ბაზის მეტრიკები", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricAdminCard("სახეობები", "${allSpecies.size}", Icons.Default.Pets, Modifier.weight(1f))
                MetricAdminCard("ნადირობები", "${allTrips.size}", Icons.Default.MenuBook, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricAdminCard("სანადირო წერტილები", "${allSpots.size}", Icons.Default.Place, Modifier.weight(1f))
                MetricAdminCard("ეკიპირება", "${allEquipment.size}", Icons.Default.Shield, Modifier.weight(1f))
            }
        }

        // Actions
        item {
            Text("ადმინისტრატორის ქმედებები", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { showAddSpeciesDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = ForestBlack),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(46.dp).testTag("admin_add_species")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ახალი სახეობა", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showBroadcastDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestSurfaceVariant, contentColor = TextPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(46.dp).testTag("admin_broadcast")
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("შეტყობინება", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Species Management List
        item {
            Text("სახეობების კატალოგი (რედაქტირება/წაშლა)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        items(allSpecies, key = { it.id }) { sp ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ForestSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(sp.nameKa, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("${sp.nameLatin} • ${sp.category}", color = TextSecondary, fontSize = 11.sp)
                    }

                    IconButton(onClick = { viewModel.deleteSpecies(sp) }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "წაშლა", tint = AlertRed, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }

    if (showAddSpeciesDialog) {
        AddSpeciesDialog(
            onDismiss = { showAddSpeciesDialog = false },
            onAdd = { species ->
                viewModel.addSpecies(species)
                showAddSpeciesDialog = false
            }
        )
    }

    if (showBroadcastDialog) {
        BroadcastNotificationDialog(
            onDismiss = { showBroadcastDialog = false },
            onSend = { title, msg, type ->
                val notif = NotificationEntity(
                    title = title,
                    message = msg,
                    type = type
                )
                viewModel.addNotification(notif)
                showBroadcastDialog = false
            }
        )
    }
}

@Composable
private fun MetricAdminCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ForestSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ForestSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(value, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Text(label, color = TextSecondary, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun AddSpeciesDialog(
    onDismiss: () -> Unit,
    onAdd: (SpeciesEntity) -> Unit
) {
    var nameGeo by remember { mutableStateOf("") }
    var scientificName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("გადამფრენი ფრინველი") }
    var dailyLimit by remember { mutableStateOf("10") }
    var seasonDates by remember { mutableStateOf("აგვისტოს მე-3 შაბათი – 15 თებერვალი") }
    var status by remember { mutableStateOf("დაშვებულია") }
    var isSeasonOpen by remember { mutableStateOf(true) }
    var prohibitedMethodsText by remember { mutableStateOf("ელექტრონული მანოკები, მანქანიდან ნადირობა") }
    var description by remember { mutableStateOf("საქართველოში გავრცელებული სანადირო სახეობა.") }
    var habitat by remember { mutableStateOf("მინდვრები, ტყისპირები, ჭალები") }
    var huntingTips by remember { mutableStateOf("დილით ადრე მეძებარი ძაღლით") }
    var legalStatus by remember { mutableStateOf("სავალდებულოა დღიური ლიმიტის დაცვა") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ForestDark,
        title = {
            Text("ახალი სახეობის დამატება ბაზაში", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nameGeo,
                    onValueChange = { nameGeo = it },
                    label = { Text("ქართული სახელი") },
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
                    value = scientificName,
                    onValueChange = { scientificName = it },
                    label = { Text("სამეცნიერო სახელი (ლათინური)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = ForestCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("კატეგორია") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGold,
                            unfocusedBorderColor = ForestCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = dailyLimit,
                        onValueChange = { dailyLimit = it },
                        label = { Text("დღიური ლიმიტი") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGold,
                            unfocusedBorderColor = ForestCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(0.8f)
                    )
                }

                OutlinedTextField(
                    value = seasonDates,
                    onValueChange = { seasonDates = it },
                    label = { Text("სეზონის პერიოდი") },
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
                    value = prohibitedMethodsText,
                    onValueChange = { prohibitedMethodsText = it },
                    label = { Text("აკრძალული მეთოდები (მძიმით)") },
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
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("აღწერა") },
                    maxLines = 2,
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
                onClick = {
                    if (nameGeo.isNotBlank()) {
                        val parsedProhibited = prohibitedMethodsText
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

                        val sp = SpeciesEntity(
                            nameGeo = nameGeo,
                            scientificName = scientificName.ifBlank { nameGeo },
                            category = category,
                            isSeasonOpen = isSeasonOpen,
                            seasonDates = seasonDates,
                            dailyLimit = dailyLimit.toIntOrNull() ?: 0,
                            status = status,
                            prohibitedMethods = parsedProhibited,
                            description = description,
                            habitat = habitat,
                            huntingTips = huntingTips,
                            identification = "$nameGeo ამოცნობის ნიშნები",
                            legalStatus = legalStatus
                        )
                        onAdd(sp)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = ForestBlack),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("დამატება", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("გაუქმება", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun BroadcastNotificationDialog(
    onDismiss: () -> Unit,
    onSend: (title: String, msg: String, type: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("სეზონი") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ForestDark,
        title = {
            Text("სისტემური შეტყობინების გაგზავნა", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("სათაური") },
                    placeholder = { Text("მაგ. ახალი სეზონის გახსნა") },
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
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("შეტყობინების ტექსტი") },
                    maxLines = 3,
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
                onClick = {
                    if (title.isNotBlank() && message.isNotBlank()) {
                        onSend(title, message, type)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = ForestBlack),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("გაგზავნა", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("გაუქმება", color = TextSecondary)
            }
        }
    )
}

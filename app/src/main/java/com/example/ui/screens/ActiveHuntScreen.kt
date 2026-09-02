package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeatherInfo
import com.example.ui.components.formatDuration
import com.example.ui.theme.*
import com.example.viewmodel.ActiveHuntSession
import com.example.viewmodel.MonadireViewModel

@Composable
fun ActiveHuntScreen(
    viewModel: MonadireViewModel,
    modifier: Modifier = Modifier
) {
    val activeHunt by viewModel.activeHunt.collectAsState()
    val currentWeather by viewModel.currentWeather.collectAsState()

    var showFinishDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ForestBlack)
    ) {
        if (activeHunt == null) {
            // Idle / Start Hunt Screen
            IdleStartHuntView(
                currentWeather = currentWeather,
                onStart = { species, location, count, eq ->
                    viewModel.startQuickHunt(
                        targetSpecies = species,
                        locationName = location,
                        hunterCount = count,
                        equipment = eq
                    )
                }
            )
        } else {
            // Live Active Hunting Session Screen
            ActiveHuntingSessionHUD(
                session = activeHunt!!,
                onIncrementHarvest = {
                    viewModel.updateActiveHarvestCount(activeHunt!!.harvestCount + 1)
                },
                onDecrementHarvest = {
                    if (activeHunt!!.harvestCount > 0) {
                        viewModel.updateActiveHarvestCount(activeHunt!!.harvestCount - 1)
                    }
                },
                onFinishClick = { showFinishDialog = true },
                onCancelClick = { viewModel.cancelActiveHunt() },
                onDropWaypoint = {
                    viewModel.addSpot(
                        name = "წერტილი #${System.currentTimeMillis() % 1000}",
                        category = "სანადირო ადგილი",
                        lat = activeHunt!!.latitude,
                        lng = activeHunt!!.longitude,
                        elevation = 720,
                        notes = "მონიშნულია ${activeHunt!!.targetSpecies}ზე ნადირობისას"
                    )
                }
            )
        }

        if (showFinishDialog && activeHunt != null) {
            FinishHuntDialog(
                session = activeHunt!!,
                onDismiss = { showFinishDialog = false },
                onConfirm = { isSuccess, count, details, notes, ammo ->
                    viewModel.finishActiveHunt(
                        isSuccessful = isSuccess,
                        harvestCount = count,
                        harvestDetails = details,
                        notes = notes,
                        ammoUsed = ammo
                    )
                    showFinishDialog = false
                }
            )
        }
    }
}

@Composable
private fun IdleStartHuntView(
    currentWeather: WeatherInfo,
    onStart: (species: String, location: String, count: Int, eq: String) -> Unit
) {
    var selectedSpecies by remember { mutableStateOf("მწყერი") }
    var location by remember { mutableStateOf(currentWeather.locationName) }
    var hunterCount by remember { mutableIntStateOf(1) }
    var selectedEquipment by remember { mutableStateOf("ორლულიანი თოფი (12/76)") }

    val commonSpecies = listOf("მწყერი", "ქედანი", "ტყის ქათამი", "გარეული იხვი", "ხოხობი", "გარეული ღორი", "კურდღელი")
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ForestSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentGold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrackChanges,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "ახალი ნადირობის დაწყება",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "დაიწყეთ გასვლა 10 წამზე ნაკლებ დროში",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Target Species Picker
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ForestSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "სამიზნე სახეობა",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(commonSpecies) { sp ->
                        val isSelected = selectedSpecies == sp
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSpecies = sp },
                            label = { Text(sp, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentGold,
                                selectedLabelColor = ForestBlack,
                                containerColor = ForestSurfaceVariant,
                                labelColor = TextPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) AccentGold else ForestCardBorder
                            )
                        )
                    }
                }
            }
        }

        // Location & Equipment
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ForestSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("სანადირო ლოკაცია") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = AccentGold) },
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
                    value = selectedEquipment,
                    onValueChange = { selectedEquipment = it },
                    label = { Text("ძირითადი იარაღი / აღჭურვილობა") },
                    leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, tint = HuntingGreenLight) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = ForestCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "მონადირეთა რაოდენობა ჯგუფში",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { if (hunterCount > 1) hunterCount-- },
                            modifier = Modifier.size(34.dp).background(ForestSurfaceVariant, CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = TextPrimary)
                        }
                        Text(hunterCount.toString(), color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        IconButton(
                            onClick = { hunterCount++ },
                            modifier = Modifier.size(34.dp).background(ForestSurfaceVariant, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = TextPrimary)
                        }
                    }
                }
            }
        }

        // Live Environment Context Pill
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ForestSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.CloudQueue, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
                Column {
                    Text(
                        text = "ავტომატური მეტეო-სინქრონიზაცია:",
                        color = AccentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${currentWeather.condition} • ${currentWeather.temperatureC}°C • ქარი ${currentWeather.windKmh} კმ/სთ",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Big Prominent Start Button
        Button(
            onClick = { onStart(selectedSpecies, location, hunterCount, selectedEquipment) },
            colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = ForestBlack),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("active_hunt_big_start_button")
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ნადირობის დაწყება",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun ActiveHuntingSessionHUD(
    session: ActiveHuntSession,
    onIncrementHarvest: () -> Unit,
    onDecrementHarvest: () -> Unit,
    onFinishClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDropWaypoint: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Session Pulsing Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = ForestDark),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(HuntingGreenLight)
                    )
                    Text(
                        text = "ნადირობა აქტიურია",
                        color = HuntingGreenLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Big Elapsed Duration Timer
                Text(
                    text = formatDuration(session.elapsedSeconds),
                    color = TextPrimary,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "დაწყების დრო: ${session.startTimeFormatted}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ForestCardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("სამიზნე", color = TextSecondary, fontSize = 11.sp)
                        Text(session.targetSpecies, color = AccentGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ლოკაცია", color = TextSecondary, fontSize = 11.sp)
                        Text(session.locationName.take(16), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("მონადირეები", color = TextSecondary, fontSize = 11.sp)
                        Text("${session.hunterCount} პირი", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Harvest Counter Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ForestSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "მოპოვებული ნადავლი (${session.targetSpecies})",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    IconButton(
                        onClick = onDecrementHarvest,
                        modifier = Modifier
                            .size(54.dp)
                            .background(ForestSurfaceVariant, CircleShape)
                            .border(1.dp, ForestCardBorder, CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "შემცირება", tint = TextPrimary, modifier = Modifier.size(24.dp))
                    }

                    Text(
                        text = session.harvestCount.toString(),
                        color = if (session.harvestCount > 0) AccentGold else TextPrimary,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black
                    )

                    IconButton(
                        onClick = onIncrementHarvest,
                        modifier = Modifier
                            .size(54.dp)
                            .background(AccentGold, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "მომატება", tint = ForestBlack, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }

        // Action Buttons Row (Drop Waypoint & Cancel)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onDropWaypoint,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, HuntingGreenLight),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(Icons.Default.AddLocation, contentDescription = null, tint = HuntingGreenLight, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("წერტილის მონიშვნა", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onCancelClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f)),
                modifier = Modifier.weight(0.7f).height(48.dp)
            ) {
                Text("შეწყვეტა", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Big Prominent Finish Hunt Button
        Button(
            onClick = onFinishClick,
            colors = ButtonDefaults.buttonColors(containerColor = HuntingGreenPrimary, contentColor = ForestBlack),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("active_hunt_finish_button")
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ნადირობის დასრულება",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun FinishHuntDialog(
    session: ActiveHuntSession,
    onDismiss: () -> Unit,
    onConfirm: (isSuccess: Boolean, count: Int, details: String, notes: String, ammo: String) -> Unit
) {
    var isSuccessful by remember { mutableStateOf(session.harvestCount > 0) }
    var harvestCount by remember { mutableIntStateOf(session.harvestCount) }
    var harvestDetails by remember {
        mutableStateOf(if (session.harvestCount > 0) "${session.harvestCount} ცალი ${session.targetSpecies}" else "")
    }
    var notes by remember { mutableStateOf("") }
    var ammoUsed by remember { mutableStateOf("12 ვაზნა") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ForestDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.DoneAll, contentDescription = null, tint = AccentGold)
                Text("ნადირობის შეჯამება", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "ხანგრძლივობა: ${formatDuration(session.elapsedSeconds)}",
                    color = AccentGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isSuccessful,
                        onClick = { isSuccessful = true },
                        label = { Text("წარმატებული") },
                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HuntingGreenPrimary,
                            selectedLabelColor = ForestBlack,
                            containerColor = ForestSurface,
                            labelColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = !isSuccessful,
                        onClick = {
                            isSuccessful = false
                            harvestCount = 0
                        },
                        label = { Text("უშედეგო") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestSurfaceVariant,
                            selectedLabelColor = TextPrimary,
                            containerColor = ForestSurface,
                            labelColor = TextSecondary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isSuccessful) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ნადავლის რაოდენობა:", color = TextSecondary, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(onClick = { if (harvestCount > 0) harvestCount-- }, modifier = Modifier.size(30.dp).background(ForestSurface, CircleShape)) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = TextPrimary)
                            }
                            Text(harvestCount.toString(), color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            IconButton(onClick = { harvestCount++ }, modifier = Modifier.size(30.dp).background(ForestSurface, CircleShape)) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = TextPrimary)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = harvestDetails,
                        onValueChange = { harvestDetails = it },
                        label = { Text("დეტალები") },
                        placeholder = { Text("მაგ. 4 მწყერი, 1 ქედანი") },
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

                OutlinedTextField(
                    value = ammoUsed,
                    onValueChange = { ammoUsed = it },
                    label = { Text("გახარჯული ვაზნები") },
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
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("შენიშვნები და ძაღლის მუშაობა") },
                    placeholder = { Text("მაგ. დილის 7-ზე კარგი ფრენა იყო, ძაღლი კარგად ეძებდა...") },
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
                    onConfirm(isSuccessful, harvestCount, harvestDetails, notes, ammoUsed)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = ForestBlack),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("დღიურში შენახვა", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("გაუქმება", color = TextSecondary)
            }
        }
    )
}

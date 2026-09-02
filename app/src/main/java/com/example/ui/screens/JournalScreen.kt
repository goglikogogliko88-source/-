package com.example.ui.screens

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HuntingTripEntity
import com.example.ui.theme.*
import com.example.viewmodel.MonadireViewModel

@Composable
fun JournalScreen(
    viewModel: MonadireViewModel,
    modifier: Modifier = Modifier
) {
    val allTrips by viewModel.allTrips.collectAsState()
    val allSpecies by viewModel.allSpecies.collectAsState()
    val tripFilter by viewModel.selectedTripFilter.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTripForDetail by remember { mutableStateOf<HuntingTripEntity?>(null) }
    var showAddManualTripDialog by remember { mutableStateOf(false) }
    var showStatsView by remember { mutableStateOf(false) }

    val filterOptions = listOf("ყველა", "წარმატებული", "უშედეგო", "მწყერი", "ქედანი", "ტყის ქათამი", "გარეული ღორი")

    val filteredTrips = remember(allTrips, tripFilter, searchQuery) {
        allTrips.filter { trip ->
            val matchesFilter = when (tripFilter) {
                "ყველა" -> true
                "წარმატებული" -> trip.isSuccessful
                "უშედეგო" -> !trip.isSuccessful
                else -> trip.targetSpecies == tripFilter
            }
            val matchesSearch = searchQuery.isBlank() ||
                    trip.title.contains(searchQuery, ignoreCase = true) ||
                    trip.locationName.contains(searchQuery, ignoreCase = true) ||
                    trip.targetSpecies.contains(searchQuery, ignoreCase = true) ||
                    trip.notes.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ForestBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddManualTripDialog = true },
                containerColor = AccentGold,
                contentColor = ForestBlack,
                modifier = Modifier
                    .padding(bottom = 70.dp)
                    .testTag("journal_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "+ ნადირობის დამატება")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
        ) {
            // Header Stats Toggle Card
            item {
                JournalHeaderStatsSummary(
                    trips = allTrips,
                    isStatsExpanded = showStatsView,
                    onToggleStats = { showStatsView = !showStatsView }
                )
            }

            // Expanded Stats Charts & Species Breakdown
            if (showStatsView && allTrips.isNotEmpty()) {
                item {
                    HuntingDetailedStatisticsCard(trips = allTrips)
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ძიება დღიურში (სახეობა, ლოკაცია, შენიშვნა)...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentGold) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "გასუფთავება", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = ForestCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = ForestSurface,
                        unfocusedContainerColor = ForestSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("journal_search_input")
                )
            }

            // Filter Chips Row
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterOptions) { filter ->
                        val isSelected = tripFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setTripFilter(filter) },
                            label = { Text(filter, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentGold,
                                selectedLabelColor = ForestBlack,
                                containerColor = ForestSurface,
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

            // Trips List or Empty State
            if (filteredTrips.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.MenuBook,
                        title = "ჩანაწერები არ მოიძებნა",
                        description = "შეცვალეთ ფილტრი ან დაამატეთ ახალი ნადირობა ქვედა ღილაკით (+)."
                    )
                }
            } else {
                items(filteredTrips, key = { it.id }) { trip ->
                    JournalTripCard(
                        trip = trip,
                        onClick = { selectedTripForDetail = trip },
                        onDelete = { viewModel.deleteTrip(trip.id) }
                    )
                }
            }
        }
    }

    if (selectedTripForDetail != null) {
        TripDetailDialog(
            trip = selectedTripForDetail!!,
            onDismiss = { selectedTripForDetail = null },
            onDelete = {
                viewModel.deleteTrip(selectedTripForDetail!!.id)
                selectedTripForDetail = null
            }
        )
    }

    if (showAddManualTripDialog) {
        AddManualTripDialog(
            allSpecies = allSpecies,
            onDismiss = { showAddManualTripDialog = false },
            onAdd = { trip ->
                viewModel.addTrip(trip)
                showAddManualTripDialog = false
            }
        )
    }
}

@Composable
private fun JournalHeaderStatsSummary(
    trips: List<HuntingTripEntity>,
    isStatsExpanded: Boolean,
    onToggleStats: () -> Unit
) {
    val totalTrips = trips.size
    val successfulTrips = trips.count { it.isSuccessful }
    val successRate = if (totalTrips > 0) ((successfulTrips.toDouble() / totalTrips) * 100).toInt() else 0
    val totalHarvest = trips.sumOf { it.harvestCount }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onToggleStats),
        colors = CardDefaults.cardColors(containerColor = ForestSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Leaderboard, contentDescription = null, tint = AccentGold)
                    Text(
                        text = "ნადირობის სტატისტიკა",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isStatsExpanded) "დახურვა" else "ანალიტიკა",
                        color = AccentGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = if (isStatsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatPill(label = "სულ გასვლა", value = "$totalTrips", color = TextPrimary)
                StatPill(label = "წარმატება", value = "$successRate%", color = HuntingGreenLight)
                StatPill(label = "ნადავლი", value = "$totalHarvest", color = AccentGold)
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun HuntingDetailedStatisticsCard(trips: List<HuntingTripEntity>) {
    val speciesMap = trips.groupBy { it.targetSpecies }
        .mapValues { entry -> entry.value.sumOf { it.harvestCount } }
        .toList()
        .sortedByDescending { it.second }

    val totalMinutes = trips.sumOf { it.durationMinutes }
    val avgDuration = if (trips.isNotEmpty()) totalMinutes / trips.size else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ForestSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ნადავლის განაწილება სახეობების მიხედვით",
                color = AccentGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            speciesMap.forEach { (species, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(species, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .width((count * 16).coerceIn(20, 120).dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AccentGold)
                        )
                        Text("$count ცალი", color = TextGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ForestCardBorder)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "საშუალო ხანგრძლივობა ერთ გასვლაზე: $avgDuration წუთი (${avgDuration / 60} სთ ${avgDuration % 60} წთ)",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun JournalTripCard(
    trip: HuntingTripEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = ForestSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (trip.isSuccessful) HuntingGreenDark else ForestSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (trip.isSuccessful) Icons.Default.CheckCircle else Icons.Default.RemoveCircleOutline,
                            contentDescription = null,
                            tint = if (trip.isSuccessful) HuntingGreenLight else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = trip.title,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${trip.date} • ${trip.locationName}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (trip.isSuccessful) HuntingGreenDark.copy(alpha = 0.5f) else ForestSurfaceVariant
                ) {
                    Text(
                        text = if (trip.isSuccessful) "${trip.harvestCount} მოპოვებული" else "უშედეგო",
                        color = if (trip.isSuccessful) HuntingGreenLight else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (trip.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = trip.notes,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ამინდი: ${trip.weatherSummary}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = "ხანგრძლივობა: ${trip.durationMinutes} წთ",
                    color = AccentGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TripDetailDialog(
    trip: HuntingTripEntity,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ForestDark,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(trip.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "წაშლა", tint = AlertRed)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow(label = "თარიღი", value = "${trip.date} (${trip.startTime} - ${trip.endTime})")
                DetailRow(label = "ლოკაცია", value = trip.locationName)
                DetailRow(label = "ხანგრძლივობა", value = "${trip.durationMinutes} წუთი")
                DetailRow(label = "სამიზნე", value = trip.targetSpecies)
                DetailRow(label = "შედეგი", value = if (trip.isSuccessful) trip.harvestDetails else "უშედეგო")
                DetailRow(label = "ამინდი", value = "${trip.weatherSummary} (${trip.temperatureC}°C, ქარი ${trip.windKmh} კმ/სთ)")
                DetailRow(label = "იარაღი", value = trip.equipmentUsed)
                DetailRow(label = "ვაზნები", value = trip.ammoUsed)
                DetailRow(label = "მონადირეები", value = "${trip.hunterCount} პირი")

                if (trip.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("ჩანაწერები:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(trip.notes, color = TextPrimary, fontSize = 12.sp, lineHeight = 16.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = ForestBlack),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("დახურვა", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Text(value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AddManualTripDialog(
    allSpecies: List<com.example.data.model.SpeciesEntity>,
    onDismiss: () -> Unit,
    onAdd: (HuntingTripEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("საგარეჯოს მინდვრები") }
    var targetSpecies by remember { mutableStateOf("მწყერი") }
    var date by remember { mutableStateOf(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())) }
    var durationMinutes by remember { mutableStateOf("180") }
    var isSuccessful by remember { mutableStateOf(true) }
    var harvestCount by remember { mutableIntStateOf(4) }
    var harvestDetails by remember { mutableStateOf("4 ცალი") }
    var notes by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf("ორლულიანი 12 ყალიბი") }
    var ammo by remember { mutableStateOf("15 ვაზნა") }

    // Find species to get daily limit
    val matchedSpecies = remember(targetSpecies, allSpecies) {
        allSpecies.find {
            it.nameGeo.equals(targetSpecies.trim(), ignoreCase = true) ||
            it.nameKa.equals(targetSpecies.trim(), ignoreCase = true) ||
            targetSpecies.contains(it.nameGeo, ignoreCase = true)
        }
    }
    val dailyLimit = matchedSpecies?.dailyLimit ?: 0
    val isLimitExceeded = isSuccessful && dailyLimit > 0 && harvestCount > dailyLimit

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ForestDark,
        title = {
            Text("ნადირობის ჩაწერა დღიურში", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
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
                    placeholder = { Text("მაგ. მწყერზე საგარეჯოში") },
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
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("ლოკაცია") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = ForestCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Species Selector Chips
                Text("სანადირო სახეობა:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val quickSpecies = listOf("მწყერი", "ქედანი", "გარეული იხვი", "გვრიტი", "ტყის ქათამი (ვალდშნეპი)", "გარეული ბატი", "მელოტა (ლისუხა)", "გარეული ღორი (ტახი)")
                    items(quickSpecies) { spName ->
                        val isSelected = targetSpecies == spName
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                targetSpecies = spName
                                harvestDetails = "$harvestCount ცალი $spName"
                            },
                            label = { Text(spName, fontSize = 11.sp) },
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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetSpecies,
                        onValueChange = { targetSpecies = it },
                        label = { Text("სახეობის სახელი") },
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
                        value = durationMinutes,
                        onValueChange = { durationMinutes = it },
                        label = { Text("წუთი") },
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("შედეგიანი იყო?", color = TextSecondary, fontSize = 12.sp)
                    Switch(
                        checked = isSuccessful,
                        onCheckedChange = { isSuccessful = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentGold,
                            checkedTrackColor = HuntingGreenDark
                        )
                    )
                }

                if (isSuccessful) {
                    // Harvest Count Counter with Legal Limit Indication
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLimitExceeded) AlertRed.copy(alpha = 0.12f) else ForestSurfaceVariant
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isLimitExceeded) AlertRed else ForestCardBorder
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "მოპოვებული რაოდენობა",
                                        color = if (isLimitExceeded) AlertRed else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (dailyLimit > 0) {
                                        Text(
                                            text = "კანონიერი დღიური ლიმიტი: $dailyLimit ცალი",
                                            color = if (isLimitExceeded) AlertRed else TextGold,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (harvestCount > 0) {
                                                harvestCount--
                                                harvestDetails = "$harvestCount ცალი $targetSpecies"
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "-", tint = AccentGold)
                                    }

                                    Text(
                                        text = "$harvestCount",
                                        color = if (isLimitExceeded) AlertRed else TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    IconButton(
                                        onClick = {
                                            harvestCount++
                                            harvestDetails = "$harvestCount ცალი $targetSpecies"
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.AddCircleOutline, contentDescription = "+", tint = AccentGold)
                                    }
                                }
                            }

                            // Limit Exceeded Warning Box
                            if (isLimitExceeded) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AlertRed.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = AlertRed, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "ყურადღება! გადააჭარბეთ კანონით დადგენილ დღიურ ლიმიტს (მაქს: $dailyLimit ცალი).",
                                            color = AlertRed,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = harvestDetails,
                        onValueChange = { harvestDetails = it },
                        label = { Text("ნადავლის აღწერა") },
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
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ჩანაწერები") },
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
                    val trip = HuntingTripEntity(
                        title = title.ifEmpty { "${targetSpecies}ზე ნადირობა ($location)" },
                        date = date,
                        startTime = "07:00",
                        endTime = "10:00",
                        durationMinutes = durationMinutes.toIntOrNull() ?: 180,
                        locationName = location,
                        weatherSummary = "მზიანი, 20°C",
                        targetSpecies = targetSpecies,
                        equipmentUsed = equipment,
                        ammoUsed = ammo,
                        isSuccessful = isSuccessful,
                        harvestCount = if (isSuccessful) harvestCount else 0,
                        harvestDetails = if (isSuccessful) harvestDetails else "უშედეგო",
                        notes = notes,
                        isSynced = true
                    )
                    onAdd(trip)
                },
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

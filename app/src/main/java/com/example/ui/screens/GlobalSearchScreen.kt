package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination
import com.example.viewmodel.MonadireViewModel

@Composable
fun GlobalSearchScreen(
    viewModel: MonadireViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val allSpecies by viewModel.allSpecies.collectAsState()
    val allTrips by viewModel.allTrips.collectAsState()
    val allSpots by viewModel.allSpots.collectAsState()
    val allEquipment by viewModel.allEquipment.collectAsState()

    val matchedSpecies = remember(allSpecies, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else allSpecies.filter {
            it.nameKa.contains(searchQuery, ignoreCase = true) ||
            it.nameLatin.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    val matchedTrips = remember(allTrips, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else allTrips.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.targetSpecies.contains(searchQuery, ignoreCase = true) ||
            it.locationName.contains(searchQuery, ignoreCase = true) ||
            it.notes.contains(searchQuery, ignoreCase = true)
        }
    }

    val matchedSpots = remember(allSpots, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else allSpots.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true) ||
            it.notes.contains(searchQuery, ignoreCase = true)
        }
    }

    val matchedEquipment = remember(allEquipment, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else allEquipment.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.brand.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    val hasResults = matchedSpecies.isNotEmpty() || matchedTrips.isNotEmpty() || matchedSpots.isNotEmpty() || matchedEquipment.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ForestBlack)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Search header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ForestSurface)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "უკან", tint = TextPrimary)
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("მოძებნეთ ყველაფერი (მწყერი, თოფი, რუკა...)", fontSize = 13.sp) },
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
                modifier = Modifier.weight(1f).testTag("global_search_input")
            )
        }

        if (searchQuery.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("ჩაწერეთ საძიებო სიტყვა", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else if (!hasResults) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("შედეგი არ მოიძებნა", color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (matchedSpecies.isNotEmpty()) {
                    item {
                        Text("სანადირო სახეობები (${matchedSpecies.size})", color = AccentGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    items(matchedSpecies) { sp ->
                        SearchResultItem(
                            title = sp.nameKa,
                            subtitle = "${sp.nameLatin} • ${sp.category}",
                            icon = Icons.Default.Pets,
                            onClick = { viewModel.navigateTo(AppDestination.SPECIES_CATALOG) }
                        )
                    }
                }

                if (matchedSpots.isNotEmpty()) {
                    item {
                        Text("სანადირო წერტილები & რუკა (${matchedSpots.size})", color = AccentGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    items(matchedSpots) { spot ->
                        SearchResultItem(
                            title = spot.name,
                            subtitle = "${spot.category} • ${spot.elevationMeters}მ ზ.დ.",
                            icon = Icons.Default.Place,
                            onClick = { viewModel.navigateTo(AppDestination.MAP) }
                        )
                    }
                }

                if (matchedTrips.isNotEmpty()) {
                    item {
                        Text("ნადირობის დღიური (${matchedTrips.size})", color = AccentGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    items(matchedTrips) { trip ->
                        SearchResultItem(
                            title = trip.title,
                            subtitle = "${trip.date} • ${trip.locationName}",
                            icon = Icons.Default.MenuBook,
                            onClick = { viewModel.navigateTo(AppDestination.JOURNAL) }
                        )
                    }
                }

                if (matchedEquipment.isNotEmpty()) {
                    item {
                        Text("აღჭურვილობა (${matchedEquipment.size})", color = AccentGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    items(matchedEquipment) { eq ->
                        SearchResultItem(
                            title = eq.name,
                            subtitle = "${eq.brand} ${eq.model} • ${eq.category}",
                            icon = Icons.Default.Shield,
                            onClick = { viewModel.navigateTo(AppDestination.EQUIPMENT_INVENTORY) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
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
                Icon(icon, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextSecondary, fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.HuntingConditionScore
import com.example.data.model.HuntingSpotEntity
import com.example.data.model.HuntingTripEntity
import com.example.data.model.WeatherInfo
import com.example.ui.components.ConditionScoreBadge
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination
import com.example.viewmodel.MonadireViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: MonadireViewModel,
    modifier: Modifier = Modifier
) {
    val currentWeather by viewModel.currentWeather.collectAsState()
    val huntingCondition by viewModel.huntingCondition.collectAsState()
    val allTrips by viewModel.allTrips.collectAsState()
    val favoriteSpots by viewModel.favoriteSpots.collectAsState()
    val activeHunt by viewModel.activeHunt.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var showQuickStartDialog by remember { mutableStateOf(false) }
    var showRegionWeatherDialog by remember { mutableStateOf(false) }

    val georgianDate = remember {
        val sdf = SimpleDateFormat("EEEE, d MMMM, yyyy", Locale("ka", "GE"))
        val dateStr = sdf.format(Date())
        // Capitalize first letter
        dateStr.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ForestBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // Hero Welcome Header Card
        item {
            HeroHeaderCard(
                userName = userProfile.name,
                todayDate = georgianDate,
                onStartHunt = {
                    if (activeHunt != null) {
                        viewModel.navigateTo(AppDestination.ACTIVE_HUNT)
                    } else {
                        showQuickStartDialog = true
                    }
                }
            )
        }

        // Weather and Hunting Conditions Dashboard
        item {
            WeatherConditionDashboard(
                weather = currentWeather,
                conditionScore = huntingCondition,
                onLocationClick = { showRegionWeatherDialog = true }
            )
        }

        // Quick Access Actions Grid
        item {
            Text(
                text = "სწრაფი წვდომა",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            QuickActionsGrid(
                onStartHunt = {
                    if (activeHunt != null) {
                        viewModel.navigateTo(AppDestination.ACTIVE_HUNT)
                    } else {
                        showQuickStartDialog = true
                    }
                },
                onOpenMap = { viewModel.navigateTo(AppDestination.MAP) },
                onAddJournal = { viewModel.navigateTo(AppDestination.ACTIVE_HUNT) },
                onOpenEquipment = { viewModel.navigateTo(AppDestination.EQUIPMENT_INVENTORY) },
                onOpenSpecies = { viewModel.navigateTo(AppDestination.SPECIES_CATALOG) },
                onOpenSafety = { viewModel.navigateTo(AppDestination.SAFETY_AND_RULES) }
            )
        }

        // Legal and Official Season Regulations Banner
        item {
            OfficialRegulationsCard(
                onReadMore = { viewModel.navigateTo(AppDestination.SAFETY_AND_RULES) }
            )
        }

        // Recent Hunting Trips Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ბოლო ნადირობები",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { viewModel.navigateTo(AppDestination.JOURNAL) }
                ) {
                    Text(
                        text = "ყველა (${allTrips.size})",
                        color = AccentGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (allTrips.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Default.MenuBook,
                    title = "დღიური ცარიელია",
                    description = "დააჭირეთ „ნადირობის დაწყებას“ თქვენი პირველი გასვლის ჩასაწერად."
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    allTrips.take(2).forEach { trip ->
                        TripSummaryCard(
                            trip = trip,
                            onClick = { viewModel.navigateTo(AppDestination.JOURNAL) }
                        )
                    }
                }
            }
        }

        // Favorite Hunting Spots Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "რჩეული ლოკაციები",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { viewModel.navigateTo(AppDestination.MAP) }
                ) {
                    Text(
                        text = "რუკაზე ნახვა",
                        color = AccentGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (favoriteSpots.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Default.Place,
                    title = "რჩეული ადგილები არ არის",
                    description = "გახსენით რუკა და მონიშნეთ თქვენი სანადირო წერტილები ვარსკვლავით."
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favoriteSpots) { spot ->
                        FavoriteSpotChipCard(
                            spot = spot,
                            onClick = { viewModel.navigateTo(AppDestination.MAP) }
                        )
                    }
                }
            }
        }
    }

    if (showQuickStartDialog) {
        QuickStartHuntDialog(
            currentWeather = currentWeather,
            onDismiss = { showQuickStartDialog = false },
            onStart = { species, location, hunters, equipment ->
                viewModel.startQuickHunt(
                    targetSpecies = species,
                    locationName = location,
                    hunterCount = hunters,
                    equipment = equipment
                )
                showQuickStartDialog = false
            }
        )
    }

    if (showRegionWeatherDialog) {
        RegionWeatherSelectorDialog(
            currentRegion = currentWeather.locationName,
            onDismiss = { showRegionWeatherDialog = false },
            onSelect = { region, temp, cond, wind, windDir, rain ->
                viewModel.setRegionWeather(region, temp, cond, wind, windDir, rain)
                showRegionWeatherDialog = false
            }
        )
    }
}

@Composable
private fun HeroHeaderCard(
    userName: String,
    todayDate: String,
    onStartHunt: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = ForestDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.monadire_hero_mountains),
                contentDescription = "Caucasian Mountains",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            // Gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                ForestDark.copy(alpha = 0.85f),
                                ForestDark
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = todayDate,
                    color = AccentGoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "მოგესალმებით, $userName",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "საქართველოს სანადირო სავარგულები მზად არის",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onStartHunt,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGold,
                        contentColor = ForestBlack
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("home_quick_start_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ნადირობის დაწყება (10 წამში)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherConditionDashboard(
    weather: WeatherInfo,
    conditionScore: HuntingConditionScore,
    onLocationClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ForestSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Location & Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLocationClick() }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = weather.locationName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "რეგიონის შეცვლა",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                ConditionScoreBadge(score = conditionScore)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Weather Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${weather.temperatureC}°",
                            color = TextPrimary,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "  (იგრძნობა ${weather.feelsLikeC}°)",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Text(
                        text = weather.condition,
                        color = TextGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ForestSurfaceVariant,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ნადირობის პირობები",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                        Text(
                            text = conditionScore.labelKa,
                            color = AccentGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = ForestCardBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Weather Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherMetricItem(
                    icon = Icons.Default.Air,
                    label = "ქარი",
                    value = "${weather.windKmh} კმ/სთ (${weather.windDirection.take(6)})"
                )
                WeatherMetricItem(
                    icon = Icons.Default.WaterDrop,
                    label = "წვიმა",
                    value = "${weather.rainProbabilityPercent}%"
                )
                WeatherMetricItem(
                    icon = Icons.Default.Compress,
                    label = "წნევა",
                    value = "${weather.pressureHpa} hPa"
                )
                WeatherMetricItem(
                    icon = Icons.Default.WbTwilight,
                    label = "მზის ჩასვლა",
                    value = weather.sunsetTime
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Informational Disclaimer
            Text(
                text = "* საინფორმაციო შეფასება: პირობების ინდექსი გამოითვლება მეტეოროლოგიური პარამეტრებით და არ იძლევა ნადირობის გარანტიას.",
                color = TextMuted,
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun WeatherMetricItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HuntingGreenLight,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp
        )
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun QuickActionsGrid(
    onStartHunt: () -> Unit,
    onOpenMap: () -> Unit,
    onAddJournal: () -> Unit,
    onOpenEquipment: () -> Unit,
    onOpenSpecies: () -> Unit,
    onOpenSafety: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton(
                title = "ნადირობის დაწყება",
                subtitle = "სწრაფი სტარტი",
                icon = Icons.Default.TrackChanges,
                accentColor = AccentGold,
                modifier = Modifier.weight(1f),
                testTag = "btn_start_hunt",
                onClick = onStartHunt
            )
            QuickActionButton(
                title = "რუკის გახსნა",
                subtitle = "GPS და წერტილები",
                icon = Icons.Default.Map,
                accentColor = HuntingGreenLight,
                modifier = Modifier.weight(1f),
                testTag = "btn_open_map",
                onClick = onOpenMap
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton(
                title = "ჟურნალში ჩაწერა",
                subtitle = "ნადირობის დღიური",
                icon = Icons.Default.MenuBook,
                accentColor = EarthSand,
                modifier = Modifier.weight(1f),
                testTag = "btn_journal",
                onClick = onAddJournal
            )
            QuickActionButton(
                title = "ეკიპირება & ჩეკლისტი",
                subtitle = "სიები & არსენალი",
                icon = Icons.Default.Checklist,
                accentColor = AccentGoldLight,
                modifier = Modifier.weight(1f),
                testTag = "btn_equipment",
                onClick = onOpenEquipment
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton(
                title = "სახეობების ცნობარი",
                subtitle = "საქართველოს ფაუნა",
                icon = Icons.Default.Pets,
                accentColor = HuntingGreenLight,
                modifier = Modifier.weight(1f),
                testTag = "btn_species",
                onClick = onOpenSpecies
            )
            QuickActionButton(
                title = "უსაფრთხოება & სეზონი",
                subtitle = "ოფიციალური წესები",
                icon = Icons.Default.Gavel,
                accentColor = WarningOrange,
                modifier = Modifier.weight(1f),
                testTag = "btn_safety",
                onClick = onOpenSafety
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(1.dp, ForestCardBorder, RoundedCornerShape(14.dp))
            .testTag(testTag),
        color = ForestSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun OfficialRegulationsCard(
    onReadMore: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onReadMore),
        colors = CardDefaults.cardColors(containerColor = ForestSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AccentGold.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ნადირობის წესები და სეზონები",
                    color = AccentGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ოფიციალურ წყაროსთან გადამოწმება აუცილებელია. იხილეთ უსაფრთხოების ნორმები და ვადები.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TripSummaryCard(
    trip: HuntingTripEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = ForestSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (trip.isSuccessful) HuntingGreenDark else ForestSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (trip.isSuccessful) Icons.Default.CheckCircle else Icons.Default.RemoveCircleOutline,
                    contentDescription = null,
                    tint = if (trip.isSuccessful) HuntingGreenLight else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${trip.date} • ${trip.locationName} • ${trip.durationMinutes} წთ",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                if (trip.harvestCount > 0) {
                    Text(
                        text = "შედეგი: ${trip.harvestDetails}",
                        color = AccentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun FavoriteSpotChipCard(
    spot: HuntingSpotEntity,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(1.dp, ForestCardBorder, RoundedCornerShape(12.dp)),
        color = ForestSurface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = spot.category,
                    color = HuntingGreenLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = spot.name,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "სიმაღლე: ${spot.elevationMeters}მ",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        color = ForestSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuickStartHuntDialog(
    currentWeather: WeatherInfo,
    onDismiss: () -> Unit,
    onStart: (species: String, location: String, hunters: Int, equipment: String) -> Unit
) {
    var selectedSpecies by remember { mutableStateOf("მწყერი") }
    var location by remember { mutableStateOf(currentWeather.locationName) }
    var hunterCount by remember { mutableStateOf("1") }
    var selectedEquipment by remember { mutableStateOf("ორლულიანი თოფი (12/76)") }

    val commonSpecies = listOf("მწყერი", "ქედანი", "ტყის ქათამი", "გარეული იხვი", "ხოხობი", "გარეული ღორი", "კურდღელი")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ForestDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.TrackChanges, contentDescription = null, tint = AccentGold)
                Text(text = "ნადირობის დაწყება", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "სამიზნე სახეობა:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(commonSpecies) { sp ->
                        FilterChip(
                            selected = selectedSpecies == sp,
                            onClick = { selectedSpecies = sp },
                            label = { Text(sp, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentGold,
                                selectedLabelColor = ForestBlack,
                                containerColor = ForestSurface,
                                labelColor = TextPrimary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("ლოკაცია / რეგიონი") },
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
                    label = { Text("აღჭურვილობა") },
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("მონადირეთა რაოდენობა:", color = TextSecondary, fontSize = 12.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val c = (hunterCount.toIntOrNull() ?: 1)
                                if (c > 1) hunterCount = (c - 1).toString()
                            },
                            modifier = Modifier.size(32.dp).background(ForestSurface, CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = TextPrimary)
                        }
                        Text(hunterCount, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(
                            onClick = {
                                val c = (hunterCount.toIntOrNull() ?: 1)
                                hunterCount = (c + 1).toString()
                            },
                            modifier = Modifier.size(32.dp).background(ForestSurface, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = TextPrimary)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ForestSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = HuntingGreenLight, modifier = Modifier.size(16.dp))
                        Text(
                            text = "GPS კოორდინატები და ამინდი (${currentWeather.temperatureC}°C) ავტომატურად მიებმება.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = hunterCount.toIntOrNull() ?: 1
                    onStart(selectedSpecies, location, count, selectedEquipment)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = ForestBlack),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("დაწყება", fontWeight = FontWeight.Bold)
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
private fun RegionWeatherSelectorDialog(
    currentRegion: String,
    onDismiss: () -> Unit,
    onSelect: (region: String, temp: Int, condition: String, wind: Int, windDir: String, rain: Int) -> Unit
) {
    val regions = listOf(
        Triple("ბორჯომის ხეობა", Pair(18, "ნაწილობრივ ღრუბლიანი"), Triple(10, "ჩრდილო-დასავლეთი", 10)),
        Triple("საგარეჯოს ველები", Pair(23, "მზიანი, სუსტი ნიავი"), Triple(8, "აღმოსავლეთი", 5)),
        Triple("ყაზბეგის მთები", Pair(11, "გრილი, ნისლიანი"), Triple(18, "ჩრდილოეთი", 30)),
        Triple("ლაგოდეხის ნაკრძალი", Pair(21, "სტაბილური, წყნარი"), Triple(6, "სამხრეთ-აღმოსავლეთი", 15)),
        Triple("ვაშლოვანის დაცული ტერიტორია", Pair(26, "მშრალი და მზიანი"), Triple(12, "დასავლეთი", 0)),
        Triple("კოლხეთის დაბლობი (ფოთი)", Pair(22, "ტენიანი, მსუბუქი ნიავი"), Triple(14, "ზღვის ნიავი", 25)),
        Triple("მესტია / ზემო სვანეთი", Pair(13, "მთის ჰაერი, სუფთა"), Triple(15, "ჩრდილო-აღმოსავლეთი", 20))
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ForestDark,
        title = {
            Text(text = "სანადირო რეგიონის შერჩევა", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("აირჩიეთ ლოკაცია ამინდისა და პირობების განახლებისთვის:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                regions.forEach { (name, tempAndCond, windAndRain) ->
                    val (temp, cond) = tempAndCond
                    val (wind, windDir, rain) = windAndRain
                    val isSelected = currentRegion.contains(name.take(6))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onSelect(name, temp, cond, wind, windDir, rain)
                            }
                            .border(
                                1.dp,
                                if (isSelected) AccentGold else ForestCardBorder,
                                RoundedCornerShape(10.dp)
                            ),
                        color = if (isSelected) ForestSurfaceVariant else ForestSurface
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("$cond • $temp°C", color = TextSecondary, fontSize = 11.sp)
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("დახურვა", color = AccentGold)
            }
        }
    )
}

package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.HuntingSpotEntity
import com.example.ui.theme.*
import com.example.viewmodel.MonadireViewModel
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MonadireViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allSpots by viewModel.allSpots.collectAsState()
    val selectedCategory by viewModel.selectedSpotCategory.collectAsState()

    var selectedSpot by remember { mutableStateOf<HuntingSpotEntity?>(null) }
    var editingSpot by remember { mutableStateOf<HuntingSpotEntity?>(null) }
    var spotToDelete by remember { mutableStateOf<HuntingSpotEntity?>(null) }

    var showAddSpotDialog by remember { mutableStateOf(false) }
    var showSpotsListSheet by remember { mutableStateOf(false) }
    var showPrivacyInfoDialog by remember { mutableStateOf(false) }

    // Map Transformations
    var mapZoom by remember { mutableFloatStateOf(1.0f) }
    var mapOffset by remember { mutableStateOf(Offset.Zero) }

    // Hunter GPS Position (Defaults to Georgia Hunting Grounds - Sagarejo / Borjomi)
    var hunterLat by remember { mutableDoubleStateOf(41.7335) }
    var hunterLng by remember { mutableDoubleStateOf(45.3312) }
    var hunterElevation by remember { mutableIntStateOf(720) }
    var isGpsActive by remember { mutableStateOf(false) }
    var gpsAccuracyMeters by remember { mutableFloatStateOf(8.5f) }

    // Map Tap Location (for placing custom pins by tapping)
    var tappedMapCoordinate by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    // Distance Measurement Tool State
    var isMeasurementMode by remember { mutableStateOf(false) }
    val measurementPoints = remember { mutableStateListOf<Pair<Double, Double>>() }

    // Direct Active Navigation State
    var navigatingToSpot by remember { mutableStateOf<HuntingSpotEntity?>(null) }

    // Search query for spots
    var searchQuery by remember { mutableStateOf("") }

    // GPS Location Permission & Tracking Setup
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isGpsActive = true
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val lastKnown = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (lastKnown != null) {
                    hunterLat = lastKnown.latitude
                    hunterLng = lastKnown.longitude
                    hunterElevation = lastKnown.altitude.toInt().coerceAtLeast(300)
                    gpsAccuracyMeters = lastKnown.accuracy
                }
                Toast.makeText(context, "GPS ლოკაცია წარმატებით ჩაირთო", Toast.LENGTH_SHORT).show()
            } catch (e: SecurityException) {
                // Ignore
            }
        } else {
            Toast.makeText(context, "GPS ნებართვა არ არის მინიჭებული. გამოყენებულია სტანდარტული კოორდინატები", Toast.LENGTH_LONG).show()
        }
    }

    // Check GPS Permission on first composition
    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) {
            isGpsActive = true
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val lastKnown = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (lastKnown != null) {
                    hunterLat = lastKnown.latitude
                    hunterLng = lastKnown.longitude
                    hunterElevation = lastKnown.altitude.toInt().coerceAtLeast(300)
                    gpsAccuracyMeters = lastKnown.accuracy
                }
            } catch (e: SecurityException) {
                // Ignore
            }
        }
    }

    val categories = listOf("ყველა", "სანადირო ადგილი", "წყარო", "პარკინგი", "საფრთხე", "კარავი", "ტყე", "გზა", "რჩეულები")

    val filteredSpots = remember(allSpots, selectedCategory, searchQuery) {
        var list = allSpots
        if (selectedCategory == "რჩეულები") {
            list = list.filter { it.isFavorite }
        } else if (selectedCategory != "ყველა") {
            list = list.filter { it.category == selectedCategory }
        }
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.notes.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }
        list
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ForestBlack)
    ) {
        // Interactive Topographic Tactical Map Canvas
        TacticalInteractiveMapCanvas(
            spots = filteredSpots,
            selectedSpot = selectedSpot,
            navigatingSpot = navigatingToSpot,
            hunterLat = hunterLat,
            hunterLng = hunterLng,
            mapZoom = mapZoom,
            mapOffset = mapOffset,
            isMeasurementMode = isMeasurementMode,
            measurementPoints = measurementPoints,
            onTransform = { pan, zoom ->
                mapOffset += pan
                mapZoom = (mapZoom * zoom).coerceIn(0.4f, 4.0f)
            },
            onSpotClicked = { spot ->
                selectedSpot = spot
            },
            onMapTapped = { lat, lng ->
                if (isMeasurementMode) {
                    measurementPoints.add(Pair(lat, lng))
                } else {
                    tappedMapCoordinate = Pair(lat, lng)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top HUD & Controls Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            // Coordinate & GPS HUD Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, ForestCardBorder, RoundedCornerShape(12.dp)),
                color = ForestDark.copy(alpha = 0.94f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable {
                            if (!isGpsActive) {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else {
                                Toast.makeText(context, "GPS ლოკაცია აქტიურია (სიზუსტე: ±${gpsAccuracyMeters.toInt()}მ)", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isGpsActive) HuntingGreenPrimary.copy(alpha = 0.2f) else WarningOrange.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isGpsActive) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                                contentDescription = "GPS",
                                tint = if (isGpsActive) HuntingGreenLight else WarningOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(
                                text = String.format(java.util.Locale.US, "GPS: %.4f°N, %.4f°E", hunterLat, hunterLng),
                                color = TextPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isGpsActive) "აქტიურია (±${gpsAccuracyMeters.toInt()}მ) • ${hunterElevation}მ ზ.დ." else "დააჭირეთ GPS-ის გასააქტიურებლად",
                                color = if (isGpsActive) HuntingGreenLight else WarningOrange,
                                fontSize = 9.5.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { showSpotsListSheet = true },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "წერტილების სია",
                                tint = AccentGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { showPrivacyInfoDialog = true },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "კონფიდენციალურობა",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Category Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSpotCategory(cat) },
                        label = { Text(cat, fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (cat == "რჩეულები") Icons.Default.Star else getCategoryIcon(cat),
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = if (isSelected) ForestBlack else if (cat == "რჩეულები") AccentGold else getCategoryColor(cat)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentGold,
                            selectedLabelColor = ForestBlack,
                            containerColor = ForestDark.copy(alpha = 0.92f),
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

            // Measurement Mode Active Banner
            AnimatedVisibility(
                visible = isMeasurementMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val totalDistKm = remember(measurementPoints) {
                    var dist = 0.0
                    if (measurementPoints.size >= 2) {
                        for (i in 0 until measurementPoints.size - 1) {
                            dist += calculateDistance(
                                measurementPoints[i].first, measurementPoints[i].second,
                                measurementPoints[i + 1].first, measurementPoints[i + 1].second
                            )
                        }
                    } else if (measurementPoints.size == 1) {
                        dist = calculateDistance(hunterLat, hunterLng, measurementPoints[0].first, measurementPoints[0].second)
                    }
                    dist
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AccentGold.copy(alpha = 0.95f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Straighten, contentDescription = null, tint = ForestBlack, modifier = Modifier.size(20.dp))
                            Column {
                                Text("მანძილის საზომი რეჟიმი", color = ForestBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                val desc = if (measurementPoints.isEmpty()) {
                                    "შეეხეთ რუკას წერტილების დასამატებლად"
                                } else if (measurementPoints.size == 1) {
                                    String.format(java.util.Locale.US, "GPS-იდან: %.2f კმ (~%d წთ ფეხით)", totalDistKm, (totalDistKm / 4.0 * 60).toInt())
                                } else {
                                    String.format(java.util.Locale.US, "%d წერტილი: %.2f კმ (~%d წთ)", measurementPoints.size, totalDistKm, (totalDistKm / 4.0 * 60).toInt())
                                }
                                Text(desc, color = ForestBlack.copy(alpha = 0.85f), fontSize = 10.5.sp)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (measurementPoints.isNotEmpty()) {
                                IconButton(
                                    onClick = { measurementPoints.clear() },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "გასუფთავება", tint = ForestBlack, modifier = Modifier.size(18.dp))
                                }
                            }
                            IconButton(
                                onClick = {
                                    isMeasurementMode = false
                                    measurementPoints.clear()
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "გამოსვლა", tint = ForestBlack, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Direct Navigation Active Banner
            AnimatedVisibility(
                visible = navigatingToSpot != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (navigatingToSpot != null) {
                    val target = navigatingToSpot!!
                    val distanceKm = calculateDistance(hunterLat, hunterLng, target.latitude, target.longitude)
                    val bearing = calculateBearing(hunterLat, hunterLng, target.latitude, target.longitude)
                    val elevDiff = target.elevationMeters - hunterElevation

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = HuntingGreenDark.copy(alpha = 0.96f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HuntingGreenLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(HuntingGreenPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = null,
                                        tint = ForestBlack,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .rotate(bearing.toFloat())
                                    )
                                }

                                Column {
                                    Text(
                                        text = "მიმართულება: ${target.name}",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = if (distanceKm < 1.0) "${(distanceKm * 1000).toInt()} მ" else String.format(java.util.Locale.US, "%.2f კმ", distanceKm),
                                            color = AccentGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        )
                                        Text(
                                            text = "აზიმუტი $bearing°",
                                            color = HuntingGreenLight,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = if (elevDiff >= 0) "+${elevDiff}მ" else "${elevDiff}მ",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        openExternalMapNavigation(context, target.latitude, target.longitude, target.name)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = "რუკა", tint = AccentGold, modifier = Modifier.size(20.dp))
                                }
                                IconButton(
                                    onClick = { navigatingToSpot = null },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "შეწყვეტა", tint = AlertRed, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Buttons on Right Side
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Zoom In
            FloatingActionButton(
                onClick = { mapZoom = (mapZoom * 1.3f).coerceAtMost(4.0f) },
                containerColor = ForestDark.copy(alpha = 0.92f),
                contentColor = TextPrimary,
                modifier = Modifier.size(42.dp).testTag("map_zoom_in")
            ) {
                Icon(Icons.Default.Add, contentDescription = "გადიდება", modifier = Modifier.size(20.dp))
            }

            // Zoom Out
            FloatingActionButton(
                onClick = { mapZoom = (mapZoom / 1.3f).coerceAtLeast(0.4f) },
                containerColor = ForestDark.copy(alpha = 0.92f),
                contentColor = TextPrimary,
                modifier = Modifier.size(42.dp).testTag("map_zoom_out")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "დაპატარავება", modifier = Modifier.size(20.dp))
            }

            // Re-center on Hunter GPS
            FloatingActionButton(
                onClick = {
                    mapOffset = Offset.Zero
                    mapZoom = 1.0f
                    Toast.makeText(context, "რუკა ორიენტირებულია თქვენს GPS პოზიციაზე", Toast.LENGTH_SHORT).show()
                },
                containerColor = ForestDark.copy(alpha = 0.92f),
                contentColor = AccentGold,
                modifier = Modifier.size(42.dp).testTag("map_center")
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "ჩემი პოზიცია", modifier = Modifier.size(20.dp))
            }

            // Toggle Distance Measurement Tool
            FloatingActionButton(
                onClick = {
                    isMeasurementMode = !isMeasurementMode
                    if (isMeasurementMode) {
                        measurementPoints.clear()
                        Toast.makeText(context, "მანძილის საზომი ჩაირთო: შეეხეთ რუკას წერტილების გასაზომად", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = if (isMeasurementMode) AccentGold else ForestDark.copy(alpha = 0.92f),
                contentColor = if (isMeasurementMode) ForestBlack else AccentGold,
                modifier = Modifier.size(42.dp).testTag("map_measure_tool")
            ) {
                Icon(Icons.Default.Straighten, contentDescription = "მანძილის გაზომვა", modifier = Modifier.size(20.dp))
            }

            // Add Spot Pin
            FloatingActionButton(
                onClick = {
                    tappedMapCoordinate = null
                    showAddSpotDialog = true
                },
                containerColor = AccentGold,
                contentColor = ForestBlack,
                modifier = Modifier.size(50.dp).testTag("map_add_spot")
            ) {
                Icon(Icons.Default.AddLocationAlt, contentDescription = "წერტილის დამატება", modifier = Modifier.size(26.dp))
            }
        }

        // Tapped Coordinate Quick Marker Creation Bar
        if (tappedMapCoordinate != null && !isMeasurementMode && selectedSpot == null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 14.dp, end = 14.dp, bottom = 80.dp),
                shape = RoundedCornerShape(14.dp),
                color = ForestDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.PinDrop, contentDescription = null, tint = AccentGold)
                        Column {
                            Text("მონიშნული წერტილი რუკაზე", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            Text(
                                String.format(java.util.Locale.US, "%.4f°N, %.4f°E", tappedMapCoordinate!!.first, tappedMapCoordinate!!.second),
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                showAddSpotDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = ForestBlack),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("შენახვა", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { tappedMapCoordinate = null }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "დახურვა", tint = TextSecondary)
                        }
                    }
                }
            }
        }

        // Bottom Selected Spot Detail Card
        if (selectedSpot != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 14.dp, end = 14.dp, bottom = 80.dp)
            ) {
                SpotDetailFloatingCard(
                    spot = selectedSpot!!,
                    hunterLat = hunterLat,
                    hunterLng = hunterLng,
                    hunterElevation = hunterElevation,
                    onFavoriteToggle = {
                        viewModel.toggleSpotFavorite(selectedSpot!!)
                        selectedSpot = selectedSpot!!.copy(isFavorite = !selectedSpot!!.isFavorite)
                    },
                    onEdit = {
                        editingSpot = selectedSpot
                    },
                    onDelete = {
                        spotToDelete = selectedSpot
                    },
                    onNavigate = {
                        navigatingToSpot = selectedSpot
                        selectedSpot = null
                    },
                    onExternalNavigate = {
                        openExternalMapNavigation(context, selectedSpot!!.latitude, selectedSpot!!.longitude, selectedSpot!!.name)
                    },
                    onClose = { selectedSpot = null }
                )
            }
        }
    }

    // Add Spot Dialog
    if (showAddSpotDialog) {
        val defaultLat = tappedMapCoordinate?.first ?: hunterLat
        val defaultLng = tappedMapCoordinate?.second ?: hunterLng

        AddOrEditHuntingSpotDialog(
            spotToEdit = null,
            defaultLat = defaultLat,
            defaultLng = defaultLng,
            defaultElevation = hunterElevation,
            onDismiss = {
                showAddSpotDialog = false
                tappedMapCoordinate = null
            },
            onSave = { name, cat, lat, lng, elev, notes, isFav ->
                viewModel.addSpot(name, cat, lat, lng, elev, notes)
                showAddSpotDialog = false
                tappedMapCoordinate = null
                Toast.makeText(context, "სანადირო წერტილი შენახულია ლოკალურად", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Edit Spot Dialog
    if (editingSpot != null) {
        AddOrEditHuntingSpotDialog(
            spotToEdit = editingSpot,
            defaultLat = editingSpot!!.latitude,
            defaultLng = editingSpot!!.longitude,
            defaultElevation = editingSpot!!.elevationMeters,
            onDismiss = { editingSpot = null },
            onSave = { name, cat, lat, lng, elev, notes, isFav ->
                val updated = editingSpot!!.copy(
                    name = name,
                    category = cat,
                    latitude = lat,
                    longitude = lng,
                    elevationMeters = elev,
                    notes = notes,
                    isFavorite = isFav
                )
                viewModel.updateSpot(updated)
                if (selectedSpot?.id == updated.id) {
                    selectedSpot = updated
                }
                editingSpot = null
                Toast.makeText(context, "წერტილი წარმატებით განახლდა", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Confirmation Dialog
    if (spotToDelete != null) {
        AlertDialog(
            onDismissRequest = { spotToDelete = null },
            containerColor = ForestDark,
            title = {
                Text("წერტილის წაშლა", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "ნამდვილად გსურთ წაშალოთ წერტილი \"${spotToDelete!!.name}\"? ეს მოქმედება შეუქცევადია.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSpot(spotToDelete!!.id)
                        if (selectedSpot?.id == spotToDelete!!.id) {
                            selectedSpot = null
                        }
                        if (navigatingToSpot?.id == spotToDelete!!.id) {
                            navigatingToSpot = null
                        }
                        spotToDelete = null
                        Toast.makeText(context, "წერტილი წაიშალა", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("წაშლა", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { spotToDelete = null }) {
                    Text("გაუქმება", color = TextSecondary)
                }
            }
        )
    }

    // Spots List Bottom Sheet
    if (showSpotsListSheet) {
        SpotsListModalBottomSheet(
            spots = allSpots,
            hunterLat = hunterLat,
            hunterLng = hunterLng,
            onSelectSpot = { spot ->
                selectedSpot = spot
                showSpotsListSheet = false
            },
            onNavigate = { spot ->
                navigatingToSpot = spot
                showSpotsListSheet = false
            },
            onDelete = { spot ->
                spotToDelete = spot
            },
            onDismiss = { showSpotsListSheet = false }
        )
    }

    // Privacy Info Dialog
    if (showPrivacyInfoDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyInfoDialog = false },
            containerColor = ForestDark,
            icon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = AccentGold, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("ლოკალური კონფიდენციალურობა", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "თქვენი სანადირო წერტილები, წყაროები და პირადი ჩანიშვნები ინახება მხოლოდ თქვენს ტელეფონში (Room Local Database).",
                        color = TextSecondary,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    )
                    Text(
                        "• არანაირი გარე სერვერული სინქრონიზაცია მონადირის ნებართვის გარეშე.\n• სრული ოფლაინ ფუნქციონალი მთაში ინტერნეტის არარსებობისას.\n• წერტილები ხელმისაწვდომია მხოლოდ ამ მოწყობილობაზე.",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = ForestBlack),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("გასაგებია", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun TacticalInteractiveMapCanvas(
    spots: List<HuntingSpotEntity>,
    selectedSpot: HuntingSpotEntity?,
    navigatingSpot: HuntingSpotEntity?,
    hunterLat: Double,
    hunterLng: Double,
    mapZoom: Float,
    mapOffset: Offset,
    isMeasurementMode: Boolean,
    measurementPoints: List<Pair<Double, Double>>,
    onTransform: (pan: Offset, zoom: Float) -> Unit,
    onSpotClicked: (HuntingSpotEntity) -> Unit,
    onMapTapped: (lat: Double, lng: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation for hunter radar & danger zones
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    onTransform(pan, zoom)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // Approximate lat/lng from tap offset
                    val centerScreenX = size.width / 2f + mapOffset.x
                    val centerScreenY = size.height / 2f + mapOffset.y

                    val dLng = (offset.x - centerScreenX) / (8000f * mapZoom)
                    val dLat = -(offset.y - centerScreenY) / (8000f * mapZoom)

                    val tappedLat = hunterLat + dLat
                    val tappedLng = hunterLng + dLng

                    onMapTapped(tappedLat, tappedLng)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f + mapOffset.x
            val centerY = height / 2f + mapOffset.y

            // Draw Background Topo Grid
            drawRect(color = ForestDark)

            val gridSpacing = 60f * mapZoom
            var x = (mapOffset.x % gridSpacing)
            while (x < width) {
                drawLine(
                    color = ForestCardBorder.copy(alpha = 0.35f),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
                x += gridSpacing
            }

            var y = (mapOffset.y % gridSpacing)
            while (y < height) {
                drawLine(
                    color = ForestCardBorder.copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                y += gridSpacing
            }

            // Draw Topographic Elevation Contours (Georgian Mountain Ridges)
            val contourCount = 7
            for (i in 1..contourCount) {
                drawCircle(
                    color = HuntingGreenDark.copy(alpha = 0.35f),
                    radius = (75f * i * mapZoom),
                    center = Offset(centerX - 90f * mapZoom, centerY - 70f * mapZoom),
                    style = Stroke(width = 1.4f)
                )

                drawCircle(
                    color = EarthKhaki.copy(alpha = 0.25f),
                    radius = (55f * i * mapZoom),
                    center = Offset(centerX + 130f * mapZoom, centerY + 90f * mapZoom),
                    style = Stroke(width = 1.2f)
                )
            }

            // Draw River/Stream Path (e.g. Alazani / Iori tributary)
            val riverPath = Path().apply {
                moveTo(0f, centerY + 180f * mapZoom)
                quadraticTo(
                    centerX - 60f * mapZoom,
                    centerY + 70f * mapZoom,
                    centerX + 70f * mapZoom,
                    centerY - 90f * mapZoom
                )
                lineTo(width, centerY - 210f * mapZoom)
            }
            drawPath(
                path = riverPath,
                color = MapWater.copy(alpha = 0.75f),
                style = Stroke(width = 4.5f * mapZoom)
            )

            // Draw Danger Zone Radiation Halos for any danger spots
            spots.filter { it.category == "საფრთხე" }.forEach { dangerSpot ->
                val dLat = (dangerSpot.latitude - hunterLat) * 8000f * mapZoom
                val dLng = (dangerSpot.longitude - hunterLng) * 8000f * mapZoom
                val dangerCenter = Offset(centerX + dLng.toFloat(), centerY - dLat.toFloat())

                // Hazard warning perimeter
                drawCircle(
                    color = AlertRed.copy(alpha = 0.15f),
                    radius = 45f * mapZoom,
                    center = dangerCenter
                )
                drawCircle(
                    color = AlertRed.copy(alpha = 0.4f * (1f - pulseRadius)),
                    radius = (45f + 25f * pulseRadius) * mapZoom,
                    center = dangerCenter,
                    style = Stroke(width = 1.5f)
                )
                drawCircle(
                    color = AlertRed,
                    radius = 45f * mapZoom,
                    center = dangerCenter,
                    style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
                )
            }

            // Draw Measurement Polyline
            if (isMeasurementMode && measurementPoints.isNotEmpty()) {
                val polyPath = Path()
                measurementPoints.forEachIndexed { index, pt ->
                    val ptDLat = (pt.first - hunterLat) * 8000f * mapZoom
                    val ptDLng = (pt.second - hunterLng) * 8000f * mapZoom
                    val ptOffset = Offset(centerX + ptDLng.toFloat(), centerY - ptDLat.toFloat())

                    if (index == 0) {
                        polyPath.moveTo(ptOffset.x, ptOffset.y)
                    } else {
                        polyPath.lineTo(ptOffset.x, ptOffset.y)
                    }

                    // Waypoint Circle
                    drawCircle(
                        color = AccentGold,
                        radius = 6f * mapZoom,
                        center = ptOffset
                    )
                    drawCircle(
                        color = ForestBlack,
                        radius = 3f * mapZoom,
                        center = ptOffset
                    )
                }

                if (measurementPoints.size == 1) {
                    // Connect hunter position to single waypoint
                    val pt0 = measurementPoints[0]
                    val pt0DLat = (pt0.first - hunterLat) * 8000f * mapZoom
                    val pt0DLng = (pt0.second - hunterLng) * 8000f * mapZoom
                    val pt0Offset = Offset(centerX + pt0DLng.toFloat(), centerY - pt0DLat.toFloat())

                    drawLine(
                        color = AccentGold,
                        start = Offset(centerX, centerY),
                        end = pt0Offset,
                        strokeWidth = 2.5f * mapZoom,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    )
                } else if (measurementPoints.size >= 2) {
                    drawPath(
                        path = polyPath,
                        color = AccentGold,
                        style = Stroke(width = 3f * mapZoom, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f))
                    )
                }
            }

            // Draw Navigation Vector Line (from Hunter to Navigating Spot)
            if (navigatingSpot != null) {
                val targetDLat = (navigatingSpot.latitude - hunterLat) * 8000f * mapZoom
                val targetDLng = (navigatingSpot.longitude - hunterLng) * 8000f * mapZoom
                val targetOffset = Offset(centerX + targetDLng.toFloat(), centerY - targetDLat.toFloat())

                drawLine(
                    color = HuntingGreenLight,
                    start = Offset(centerX, centerY),
                    end = targetOffset,
                    strokeWidth = 3f * mapZoom,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                )
            }

            // Draw Hunter Location Marker (Pulsing Radar Circle)
            drawCircle(
                color = HuntingGreenPrimary.copy(alpha = 0.25f * (1f - pulseRadius)),
                radius = (30f + 25f * pulseRadius) * mapZoom,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = HuntingGreenPrimary.copy(alpha = 0.35f),
                radius = 20f * mapZoom,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = AccentGold,
                radius = 7f * mapZoom,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = ForestBlack,
                radius = 3.5f * mapZoom,
                center = Offset(centerX, centerY)
            )
        }

        // Overlay Interactive Spot Pin Markers
        spots.forEach { spot ->
            val dLat = (spot.latitude - hunterLat) * 8000f * mapZoom
            val dLng = (spot.longitude - hunterLng) * 8000f * mapZoom

            val pinX = (mapOffset.x + dLng).toFloat()
            val pinY = (mapOffset.y - dLat).toFloat()

            val isSelected = selectedSpot?.id == spot.id
            val isNavigating = navigatingSpot?.id == spot.id

            Box(
                modifier = Modifier
                    .offset(
                        x = (180.dp + (pinX / 3).dp).coerceIn((-100).dp, 500.dp),
                        y = (340.dp + (pinY / 3).dp).coerceIn((-100).dp, 750.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSpotClicked(spot) }
                    .background(
                        if (isNavigating) HuntingGreenPrimary
                        else if (isSelected) AccentGold
                        else getCategoryColor(spot.category)
                    )
                    .border(
                        1.2.dp,
                        if (isSelected || isNavigating) Color.White else ForestBlack.copy(alpha = 0.6f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = getCategoryIcon(spot.category),
                        contentDescription = null,
                        tint = if (isSelected || isNavigating) ForestBlack else TextPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = spot.name,
                        color = if (isSelected || isNavigating) ForestBlack else TextPrimary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (spot.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isSelected || isNavigating) ForestBlack else AccentGold,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpotDetailFloatingCard(
    spot: HuntingSpotEntity,
    hunterLat: Double,
    hunterLng: Double,
    hunterElevation: Int,
    onFavoriteToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNavigate: () -> Unit,
    onExternalNavigate: () -> Unit,
    onClose: () -> Unit
) {
    val distanceKm = remember(spot, hunterLat, hunterLng) {
        calculateDistance(hunterLat, hunterLng, spot.latitude, spot.longitude)
    }
    val bearingDeg = remember(spot, hunterLat, hunterLng) {
        calculateBearing(hunterLat, hunterLng, spot.latitude, spot.longitude)
    }
    val elevDiff = spot.elevationMeters - hunterElevation
    val hikingMins = (distanceKm / 3.8 * 60).toInt().coerceAtLeast(1)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = ForestDark),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, if (spot.isFavorite) AccentGold else ForestCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(getCategoryColor(spot.category).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(spot.category),
                            contentDescription = null,
                            tint = getCategoryColor(spot.category),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = spot.name,
                            color = TextPrimary,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = spot.category,
                                color = getCategoryColor(spot.category),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "• GPS: ${String.format(java.util.Locale.US, "%.3f, %.3f", spot.latitude, spot.longitude)}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (spot.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "რჩეული",
                            tint = if (spot.isFavorite) AccentGold else TextSecondary
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "რედაქტირება",
                            tint = AccentGold
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "წაშლა",
                            tint = AlertRed
                        )
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "დახურვა",
                            tint = TextSecondary
                        )
                    }
                }
            }

            if (spot.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ForestSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = spot.notes,
                        color = TextSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ForestCardBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("მანძილი", color = TextSecondary, fontSize = 9.5.sp)
                    Text(
                        text = if (distanceKm < 1.0) "${(distanceKm * 1000).toInt()} მ" else String.format(java.util.Locale.US, "%.1f კმ", distanceKm),
                        color = TextPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text("აზიმუტი", color = TextSecondary, fontSize = 9.5.sp)
                    Text(
                        text = "$bearingDeg°",
                        color = HuntingGreenLight,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text("დრო ფეხით", color = TextSecondary, fontSize = 9.5.sp)
                    Text(
                        text = "~$hikingMins წთ",
                        color = AccentGold,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text("სიმაღლე", color = TextSecondary, fontSize = 9.5.sp)
                    Text(
                        text = "${spot.elevationMeters}მ",
                        color = TextPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons (In-App Direct Compass Navigation & External Maps App)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNavigate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HuntingGreenPrimary,
                        contentColor = ForestBlack
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.2f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("პირდაპირი გეზი", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onExternalNavigate,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AccentGold
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(0.9f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("რუკაზე", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AddOrEditHuntingSpotDialog(
    spotToEdit: HuntingSpotEntity?,
    defaultLat: Double,
    defaultLng: Double,
    defaultElevation: Int,
    onDismiss: () -> Unit,
    onSave: (name: String, cat: String, lat: Double, lng: Double, elev: Int, notes: String, isFav: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(spotToEdit?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(spotToEdit?.category ?: "სანადირო ადგილი") }
    var latStr by remember { mutableStateOf(String.format(java.util.Locale.US, "%.5f", spotToEdit?.latitude ?: defaultLat)) }
    var lngStr by remember { mutableStateOf(String.format(java.util.Locale.US, "%.5f", spotToEdit?.longitude ?: defaultLng)) }
    var elevationStr by remember { mutableStateOf((spotToEdit?.elevationMeters ?: defaultElevation).toString()) }
    var notes by remember { mutableStateOf(spotToEdit?.notes ?: "") }
    var isFavorite by remember { mutableStateOf(spotToEdit?.isFavorite ?: false) }

    val spotCategories = listOf(
        "სანადირო ადგილი", "წყარო", "პარკინგი", "საფრთხე", "კარავი", "ტყე", "გზა", "სხვა"
    )

    val isEditing = spotToEdit != null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ForestDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.EditLocation else Icons.Default.AddLocationAlt,
                    contentDescription = null,
                    tint = AccentGold
                )
                Text(
                    text = if (isEditing) "წერტილის რედაქტირება" else "ახალი სანადირო წერტილი",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("წერტილის სახელი") },
                        placeholder = { Text("მაგ. მწყრის ველი, ალგეთის წყარო...") },
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

                item {
                    Text("კატეგორია:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(spotCategories) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 10.5.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getCategoryIcon(cat),
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = if (isSelected) ForestBlack else getCategoryColor(cat)
                                    )
                                },
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

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = latStr,
                            onValueChange = { latStr = it },
                            label = { Text("განედი (Lat)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGold,
                                unfocusedBorderColor = ForestCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = lngStr,
                            onValueChange = { lngStr = it },
                            label = { Text("გრძედი (Lng)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGold,
                                unfocusedBorderColor = ForestCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = elevationStr,
                        onValueChange = { elevationStr = it },
                        label = { Text("სიმაღლე (მეტრი ზ.დ.)") },
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

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("შენიშვნები, რჩევები და უსაფრთხოება") },
                        placeholder = { Text("მაგ. დილით კარგი გადაფრენაა, მანქანით მისადგომია...") },
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

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isFavorite = !isFavorite }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = isFavorite,
                            onCheckedChange = { isFavorite = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AccentGold,
                                checkmarkColor = ForestBlack
                            )
                        )
                        Text("დამატება რჩეულებში", color = TextPrimary, fontSize = 12.sp)
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ForestSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = AccentGold, modifier = Modifier.size(15.dp))
                            Text(
                                text = "ინახება მხოლოდ თქვენს ლოკალურ მოწყობილობაზე (Room DB)",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val lat = latStr.toDoubleOrNull() ?: defaultLat
                        val lng = lngStr.toDoubleOrNull() ?: defaultLng
                        val elev = elevationStr.toIntOrNull() ?: defaultElevation
                        onSave(name, selectedCategory, lat, lng, elev, notes, isFavorite)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = ForestBlack),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isEditing) "განახლება" else "შენახვა", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("გაუქმება", color = TextSecondary)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpotsListModalBottomSheet(
    spots: List<HuntingSpotEntity>,
    hunterLat: Double,
    hunterLng: Double,
    onSelectSpot: (HuntingSpotEntity) -> Unit,
    onNavigate: (HuntingSpotEntity) -> Unit,
    onDelete: (HuntingSpotEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var filterCategory by remember { mutableStateOf("ყველა") }
    val categories = listOf("ყველა", "სანადირო ადგილი", "წყარო", "პარკინგი", "საფრთხე", "კარავი", "რჩეულები")

    val displayedSpots = remember(spots, filterCategory) {
        if (filterCategory == "რჩეულები") spots.filter { it.isFavorite }
        else if (filterCategory == "ყველა") spots
        else spots.filter { it.category == filterCategory }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ForestDark,
        dragHandle = { BottomSheetDefaults.DragHandle(color = AccentGold) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "შენახული სანადირო წერტილები (${displayedSpots.size})",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ForestSurfaceVariant
                ) {
                    Text(
                        text = "ლოკალური",
                        color = AccentGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { cat ->
                    val isSelected = filterCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { filterCategory = cat },
                        label = { Text(cat, fontSize = 10.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentGold,
                            selectedLabelColor = ForestBlack,
                            containerColor = ForestSurface,
                            labelColor = TextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (displayedSpots.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ამ კატეგორიაში წერტილები არ არის", color = TextSecondary, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxHeight(0.6f)
                ) {
                    items(displayedSpots, key = { it.id }) { spot ->
                        val distKm = calculateDistance(hunterLat, hunterLng, spot.latitude, spot.longitude)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSpot(spot) },
                            colors = CardDefaults.cardColors(containerColor = ForestSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (spot.isFavorite) AccentGold else ForestCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(spot.category),
                                        contentDescription = null,
                                        tint = getCategoryColor(spot.category),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = spot.name,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = spot.category,
                                                color = getCategoryColor(spot.category),
                                                fontSize = 10.5.sp
                                            )
                                            Text(
                                                text = "• ${String.format(java.util.Locale.US, "%.1f კმ", distKm)}",
                                                color = TextMuted,
                                                fontSize = 10.5.sp
                                            )
                                            Text(
                                                text = "• ${spot.elevationMeters}მ",
                                                color = TextMuted,
                                                fontSize = 10.5.sp
                                            )
                                        }
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { onNavigate(spot) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.Navigation, contentDescription = "გეზი", tint = HuntingGreenLight, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { onDelete(spot) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "წაშლა", tint = AlertRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "სანადირო ადგილი" -> Icons.Default.TrackChanges
        "წყარო" -> Icons.Default.WaterDrop
        "პარკინგი" -> Icons.Default.LocalParking
        "საფრთხე" -> Icons.Default.Warning
        "კარავი" -> Icons.Default.Cabin
        "ტყე" -> Icons.Default.Park
        "გზა" -> Icons.AutoMirrored.Filled.AltRoute
        else -> Icons.Default.Place
    }
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        "სანადირო ადგილი" -> AccentGold
        "წყარო" -> InfoBlue
        "პარკინგი" -> TextSecondary
        "საფრთხე" -> AlertRed
        "კარავი" -> WarningOrange
        "ტყე" -> HuntingGreenLight
        "გზა" -> EarthSand
        else -> TextMuted
    }
}

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // Radius of earth in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
    val dLon = Math.toRadians(lon2 - lon1)
    val y = sin(dLon) * cos(Math.toRadians(lat2))
    val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) -
            sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(dLon)
    var brng = Math.toDegrees(atan2(y, x))
    if (brng < 0) brng += 360.0
    return brng.toInt()
}

fun openExternalMapNavigation(context: Context, latitude: Double, longitude: Double, label: String) {
    try {
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            webIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(webIntent)
        } catch (ex: Exception) {
            Toast.makeText(context, "რუკის აპლიკაციის გახსნა ვერ მოხერხდა", Toast.LENGTH_SHORT).show()
        }
    }
}
